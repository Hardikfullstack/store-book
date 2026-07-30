package com.storebook.inventoryapp.shared.test.sync

import app.cash.sqldelight.db.SqlDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * e37-s5 - FailedSyncQueue lifecycle: enqueue, dequeue, backoff, permanent failure.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FailedSyncQueueTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: SqlDriver

    @BeforeEach
    fun setup() {
        val (db, d) = com.storebook.inventoryapp.shared.test.DatabaseTestHelper.createDatabase()
        database = db
        driver = d
    }

    @AfterEach
    fun teardown() {
        com.storebook.inventoryapp.shared.test.DatabaseTestHelper.dropDatabase(driver)
    }

    @Test
    fun enqueue_and_dequeue_decrement_pending_count() {
        val now = System.currentTimeMillis()

        // Enqueue a future retry - not yet overdue
        database.storeBookQueries.enqueueSyncFailure(
            entityType = "ITEM", localId = 1, cloudId = null,
            nextRetryAt = now + 30000, errorMessage = "Network timeout"
        )

        val countBefore = database.storeBookQueries.getPendingFailureCount().executeAsOneOrNull()
        assertEquals(1L, countBefore, "Must enqueue exactly 1 pending failure")

        val overdueFuture = database.storeBookQueries.getOverdueForRetry(now).executeAsList()
        assertTrue(overdueFuture.isEmpty(), "next_retry_at is in future so not yet due")

        // Enqueue a past-due retry
        database.storeBookQueries.enqueueSyncFailure(
            entityType = "SALE", localId = 2, cloudId = null,
            nextRetryAt = now - 1000, errorMessage = "Old failure"
        )

        var overdueList = database.storeBookQueries.getOverdueForRetry(now).executeAsList()
        assertEquals(1, overdueList.size, "Only the past-due entry should be returned")

        val failedId = overdueList[0].id
        database.storeBookQueries.dequeueFailedSyncById(failedId)

        val countAfter = database.storeBookQueries.getPendingFailureCount().executeAsOneOrNull()
        assertEquals(1L, countAfter, "One pending failure should remain after dequeue")

        val permanentFailures = database.storeBookQueries.getPermanentFailures().executeAsList()
        assertTrue(permanentFailures.isEmpty(), "No permanent failures yet")
    }

    @Test
    fun retry_state_update_increments_retry_count_and_pushes_backoff() {
        val now = System.currentTimeMillis()

        database.storeBookQueries.enqueueSyncFailure(
            entityType = "ITEM", localId = 5, cloudId = null,
            nextRetryAt = now - 5000, errorMessage = "First failure"
        )

        val overdueList = database.storeBookQueries.getOverdueForRetry(now).executeAsList()
        assertEquals(1, overdueList.size)
        val entryId = overdueList[0].id

        var rawEntry = database.storeBookQueries.getFailedSyncQueueById(entryId).executeAsOneOrNull()
        requireNotNull(rawEntry)
        assertEquals(0L, rawEntry.retry_count, "Initial retry count should be 0")

        // updateRetryState increments retry_count and pushes next_retry_at forward
        val newRetryAt = now + 60000
        database.storeBookQueries.updateRetryState(
            nextRetryAt = newRetryAt, errorMessage = "Retry #1 failed", id = entryId
        )

        rawEntry = database.storeBookQueries.getFailedSyncQueueById(entryId).executeAsOneOrNull()
        requireNotNull(rawEntry)
        assertEquals(1L, rawEntry.retry_count, "Retry count should increment to 1")
        assertTrue(rawEntry.next_retry_at > now, "next_retry_at should be in future (60s backoff)")

        val stillOverdue = database.storeBookQueries.getOverdueForRetry(now + 59000).executeAsList()
        assertTrue(stillOverdue.isEmpty(), "Not overdue until past the new next_retry_at")
    }

    @Test
    fun mark_permanent_failure_removes_from_overdue_list() {
        val now = System.currentTimeMillis()

        database.storeBookQueries.enqueueSyncFailure(
            entityType = "ITEM", localId = 9, cloudId = null,
            nextRetryAt = now - 100, errorMessage = "Failed too many times"
        )

        val overdueBefore = database.storeBookQueries.getOverdueForRetry(now).executeAsList()
        assertEquals(1, overdueBefore.size)
        val entryId = overdueBefore[0].id

        database.storeBookQueries.markPermanentFailure(entryId)

        val afterMark = database.storeBookQueries.getOverdueForRetry(now + 5000).executeAsList()
        assertTrue(afterMark.isEmpty(), "Permanently failed entries must not appear in overdue list")

        val permanentFails = database.storeBookQueries.getPermanentFailures().executeAsList()
        assertEquals(1, permanentFails.size)
        assertEquals("PERMANENT_FAILURE", permanentFails[0].status)
    }

    @Test
    fun increment_failed_mutation_count_increases_total() {
        val before = database.storeBookQueries.getSyncState().executeAsOneOrNull()
        requireNotNull(before)
        val originalCount = before.total_failed_mutations

        database.storeBookQueries.incrementFailedMutationCount()

        val after = database.storeBookQueries.getSyncState().executeAsOneOrNull()
        requireNotNull(after)
        assertEquals(originalCount + 1, after.total_failed_mutations)
    }
}
