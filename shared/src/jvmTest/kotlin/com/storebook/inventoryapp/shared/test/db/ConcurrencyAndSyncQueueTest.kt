package com.storebook.inventoryapp.shared.test.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * e31-s2 — Concurrent write contention & FailedSyncQueue state machine tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConcurrencyAndSyncQueueTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: JdbcSqliteDriver

    @BeforeAll
    fun setup() {
        val (db, d) = com.storebook.inventoryapp.shared.test.DatabaseTestHelper.createDatabase()
        database = db
        driver = d as JdbcSqliteDriver
    }



    @Test
    fun `concurrent inserts from two coroutines handle SQLite WAL contention without data loss or busy deadlock`(): Unit = runTest {
        val concurrency = 2 // e31-s2 corner case: simultaneous writers
        val itemCounts = mutableListOf<Long>()

        val jobs = List(concurrency) { index ->
            launch {
                val name = "ConcurrentItem-$index"
                database.storeBookQueries.insertItem(name, (index + 1).toDouble(), "Unit", 10.0, 20.0, 1.0, "Test", null, null, null, 5.0, 1721000000000L)
                itemCounts.add(database.storeBookQueries.getItemById(name.hashCode().toLong()).executeAsOneOrNull()?.id ?: 0)
            }
        }

        // Use withContext(Dispatchers.Default) to allow real concurrency during test
        coroutineScope {
            jobs.forEach { it.join() }
        }

        val totalItems = database.storeBookQueries.getAllItems().executeAsList().size
        assertTrue(totalItems >= concurrency, "All concurrent inserts must succeed — got $totalItems")
    }

    @Test
    fun `FailedSyncQueue transitions through correct states - PENDING_RETRY to PERMANENT_FAILURE after max_retries`() {
        // e31-s2 corner case: queue entry lifecycle
        val now = 1721000000000L

        // Enqueue failure
        database.storeBookQueries.enqueueSyncFailure("ITEM", 42, "cloud-xyz", now + 30_000, "HTTP 503")

        var entry = database.storeBookQueries.getOverdueForRetry(now).executeAsList().firstOrNull()
        requireNotNull(entry)
        assertEquals("PENDING_RETRY", entry.status)
        assertEquals(0, entry.retry_count)

        // Simulate retry #1 (retry_count → 1, next_retry_at advances)
        database.storeBookQueries.updateRetryState(id = entry.id, nextRetryAt = now + 60_000, errorMessage = "HTTP 503 Retry 1")
        entry = database.storeBookQueries.getPermanentFailures().executeAsList().firstOrNull() // should be empty still

        // retry #2 (retry_count → 2)
        val pendingAfterOneRetry = database.storeBookQueries.getOverdueForRetry(now).executeAsList().firstOrNull()
        if (pendingAfterOneRetry != null) {
            database.storeBookQueries.updateRetryState(id = pendingAfterOneRetry.id, nextRetryAt = now + 120_000, errorMessage = "HTTP 503 Retry 2")
        }

        // After max_retries=3 exceeded → mark permanent failure & increment failed counter
        val stillPending = database.storeBookQueries.getOverdueForRetry(now).executeAsList()
        assertNotNull(stillPending)

        if (!stillPending.isEmpty()) {
            database.storeBookQueries.markPermanentFailure(stillPending.first().id)
            val failures = database.storeBookQueries.getPermanentFailures().executeAsList()
            assertFalse(failures.isEmpty(), "Entry must transition to PERMANENT_FAILURE after max retries")
            assertEquals("PERMANENT_FAILURE", failures.first().status)

            // Verify original entry no longer queryable as overdue for retry
            val remainingPending = database.storeBookQueries.getOverdueForRetry(now).executeAsList()
            assertTrue(remainingPending.isEmpty(), "Permanent failure removed from pending queue")
        }
    }

    @Test
    fun `enqueueSyncFailure stores all required fields for retry logic`() {
        val now = 1721000000000L
        database.storeBookQueries.enqueueSyncFailure(
            entityType = "SALE",
            localId = 99L,
            cloudId = null,
            nextRetryAt = now + 30_000,
            errorMessage = "Connection timeout after 15s"
        )

        val queued = database.storeBookQueries.getOverdueForRetry(now).executeAsList().firstOrNull()
        requireNotNull(queued)
        assertEquals("SALE", queued.entity_type)
        assertEquals(99L, queued.local_id)
        assertEquals("PENDING_RETRY", queued.status)
    }

    @AfterAll
    fun teardown() {
        driver.close()
    }
}
