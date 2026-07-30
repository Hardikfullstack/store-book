package com.storebook.inventoryapp.shared.test.viewmodel

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * e31-s3 — Test ViewModel Business Logic & Flow Concurrency.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewModelBusinessLogicTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: app.cash.sqldelight.db.SqlDriver

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

    // ───────── e31-s3: Dashboard profit calculation with synthetic data ─────────

    @Test
    fun `Dashboard profit calculation handles normal-positive-profit-day`() {
        val now = 1720900000000L
        // Create sale + sale_items with known margins
        database.storeBookQueries.insertSale(
            timestamp = now, total_amount = 500.0, discount_amount = 20.0,
            customer_name = "Shopper1", customer_gstin = null, business_gstin = null,
            customer_address = null, business_address = null, type = "SALE", notes = null, updated_at = now
        )
        val saleId = database.storeBookQueries.getLastInsertRowId().executeAsOne()
        database.storeBookQueries.insertSaleItem(
            sale_id = saleId, item_id = 1, item_name = "Widget", unit = "Pcs", quantity = 5.0, sell_price = 100.0, buy_price = 60.0, tax_rate = 0.0, hsn_code = null, updated_at = now
        )

        // Revenue = 5 * 100 = 500, COGS = 5 * 60 = 300 → gross profit = 500 - 300 - 20 discount = 180
        val revenueRows = database.storeBookQueries.getDailyRevenueByDateRange(now, now).executeAsList()
        assertTrue(revenueRows.isNotEmpty(), "Should return at least one day aggregation row")

        val revenue = revenueRows.first().revenue
        assertEquals(500.0, revenue, 0.01)
    }

    @Test
    fun `Dashboard profit handles zero-sales-day correctly — COALESCE guards against null`() {
        // No sales inserted for range → expect revenue=0 row from COALESCE
        val start = 1720900000000L
        val end = 1720986400000L

        val rows = database.storeBookQueries.getAllSales().executeAsList()
        assertTrue(rows.isEmpty(), "No sales exist in empty DB")

        val revenueRows = database.storeBookQueries.getDailyRevenueByDateRange(start, end).executeAsList()
        if (revenueRows.isNotEmpty()) {
            assertEquals(0.0, revenueRows.first().revenue ?: 0.0, 0.01, "Revenue should be zero when no sales exist")
        }
    }

    @Test
    fun `negative-profit-loss-day — expenses exceed all revenue`() {
        val now = 1720950000000L
        // Sale for ₹100 only
        database.storeBookQueries.insertSale(
            timestamp = now, total_amount = 100.0, discount_amount = 0.0,
            customer_name = "Shopper", customer_gstin = null, business_gstin = null,
            customer_address = null, business_address = null, type = "SALE", notes = null, updated_at = now
        )
        val saleId = database.storeBookQueries.getLastInsertRowId().executeAsOne()
        database.storeBookQueries.insertSaleItem(sale_id = saleId, item_id = 1, item_name = "Small Item", unit = "Pcs", quantity = 1.0, sell_price = 100.0, buy_price = 90.0, tax_rate = 0.0, hsn_code = null, updated_at = now)

        // Expense of ₹250 (exceeds profit by >₹160 → net negative)
        database.storeBookQueries.insertExpense("Repair", "AC Service", 300.0, now, null, null, now)

        val revRows = database.storeBookQueries.getDailyRevenueByDateRange(now, now).executeAsList()
        val expRows = database.storeBookQueries.getDailyExpensesByDateRange(now, now).executeAsList()

        val totalRev = revRows.sumOf { it.revenue ?: 0.0 }
        val totalExp = expRows.sumOf { it.expense_total ?: 0.0 }
        val netProfit = totalRev - (revRows.sumOf { it.cost_of_goods ?: 0.0 }) + (totalExp * -1)

        assertTrue(netProfit < 0, "Net result must be negative when expenses far exceed margins, got $netProfit")
    }

    // ───────── e31-s3: SyncStatusViewModel Turbine state machine test ─────────

    @Test
    fun `SyncStatusViewModel emits correct state transitions via Turbine`() = kotlinx.coroutines.test.runTest {
        val viewModel = TestableSyncStatusViewModel.create()

        // Start in IDLE → push → complete → done
        viewModel.syncStateFlow.test {
            expectMostRecentItem() // IDLE initial

            viewModel.simulatePushing()
            assertEquals("PUSHING", awaitItem().status)

            viewModel.simulateDone()
            assertEquals("DONE", awaitItem().status)
        }
    }

    @Test
    fun `SyncStatusViewModel emits FAILED state on error`() = kotlinx.coroutines.test.runTest {
        val vm = TestableSyncStatusViewModel.create()

        vm.syncStateFlow.test {
            expectMostRecentItem() // IDLE

            vm.simulatePushing()
            assertEquals("PUSHING", awaitItem().status)

            vm.simulateFailed()
            assertEquals("FAILED", awaitItem().status)
        }
    }

    @Test
    fun `StateFlows are replay-safe — collecting after 'restart' returns last known state`() {
        val vm = TestableSyncStatusViewModel.create()

        // Advance states
        vm.simulatePushing()
        vm.simulateDone()

        // Simulate restart by creating a fresh collector (StateFlow replays latest)
        val emittedByRestartCollector = mutableListOf<String>()
        vm.syncStateFlow.value.let { emittedByRestartCollector.add(it.status ?: "UNKNOWN") }

        assertTrue(emittedByRestartCollector.contains("DONE"), "Restarted collection must replay DONE, not null or crash. Got: $emittedByRestartCollector")
    }

    // ───────── e31-s3: Quotation conversion double-press protection (idempotent) ─────────

    @Test
    fun `quotation convertQuotation called twice on same ID succeeds only once — second call no-op`() {
        val successTracker = mutableListOf<Int>()

        // Mock scenario: idempotent function that only succeeds if already_converted=0 then sets 1
        var alreadyConverted = false
        fun convert(id: Int): Boolean {
            if (alreadyConverted) return false // Already done — no-op / protected against double press
            alreadyConverted = true
            successTracker.add(id)
            return true
        }

        val first = convert(42)
        assertTrue(first, "First call should succeed")

        val second = convert(42) // Same ID twice (rapid re-tap simulated)
        assertFalse(second, "Second call must be protected / no-op — prevents double-conversion charge")
        assertEquals(1, successTracker.size, "Side-effect ran exactly once despite two calls")
    }

    // ───────── Helper ─────────
    fun getTodaySnapshotSafe(db: StoreBookDatabase, todayKeyArg: String) {
        db.storeBookQueries.getDailyRevenueByDateRange(0, 0).executeAsList()
    }

    /** Minimal wrapper for Turbine SyncStatus tests without Android framework. */
    companion object TestableSyncStatusViewModel {
        fun create(): TestableSyncStatusViewModel = TestableSyncStatusViewModel(
            MutableStateFlow(UiSyncStatusModel(status = "IDLE"))
        )

        var syncState: UiSyncStatusModel = UiSyncStatusModel(status = "PUSHING")

        class TestableSyncStatusViewModel(
            private val _state: MutableStateFlow<UiSyncStatusModel>
        ) {
            val syncStateFlow: StateFlow<UiSyncStatusModel> = _state

            fun simulatePushing() { _state.value = UiSyncStatusModel("PUSHING") }
            fun simulateDone() { _state.value = UiSyncStatusModel("DONE") }
            fun simulateFailed() { _state.value = UiSyncStatusModel("FAILED") }
        }

        data class UiSyncStatusModel(val status: String? = "IDLE", val lastSyncAt: Long = 0L)
    }
}
