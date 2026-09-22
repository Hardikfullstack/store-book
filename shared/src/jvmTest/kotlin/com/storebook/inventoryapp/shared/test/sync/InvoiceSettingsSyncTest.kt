package com.storebook.inventoryapp.shared.test.sync

import app.cash.sqldelight.db.SqlDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * E56-S2 — invoice_settings delta sync lifecycle: insert -> get -> unsynced -> markSynced -> remote upsert.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvoiceSettingsSyncTest {

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
    fun insert_invoice_settings_returns_row_with_is_synced_zero() {
        database.storeBookQueries.insertOrUpdateInvoiceSettings(
            storeId = "store-1",
            shopName = "My Shop",
            shopAddress = "Hyderabad",
            shopGstin = null,
            logoPath = null,
            accentColor = "#ff0000",
            headerText = "INVOICE",
            footerText = null,
            bankDetails = null,
            terms = null,
            showGstBreakdown = 1L,
            templateStyle = "MINIMALIST",
            updatedAt = 1000
        )

        val row = database.storeBookQueries.getInvoiceSettingsByStoreId("store-1").executeAsOneOrNull()
        assertNotNull(row)
        assertEquals("My Shop", row!!.shop_name)
        assertEquals("#ff0000", row.accent_color)
        assertEquals(0, row.is_synced, "Must be marked unsynced on local insert")
    }

    @Test
    fun unsynced_query_returns_recently_inserted_settings() {
        database.storeBookQueries.insertOrUpdateInvoiceSettings(
            storeId = "store-1",
            shopName = "Shop A",
            shopAddress = null,
            shopGstin = null,
            logoPath = null,
            accentColor = null,
            headerText = null,
            footerText = null,
            bankDetails = null,
            terms = null,
            showGstBreakdown = null,
            templateStyle = "STANDARD_GST",
            updatedAt = 2000
        )

        val unsynced = database.storeBookQueries.getUnsyncedInvoiceSettings().executeAsList()
        assertEquals(1, unsynced.size)
        assertEquals("Shop A", unsynced[0].shop_name)
    }

    @Test
    fun mark_synced_sets_flag_and_cloud_id() {
        database.storeBookQueries.insertOrUpdateInvoiceSettings(
            storeId = "store-2",
            shopName = "Shop B",
            shopAddress = null,
            shopGstin = null,
            logoPath = null,
            accentColor = null,
            headerText = null,
            footerText = null,
            bankDetails = null,
            terms = null,
            showGstBreakdown = null,
            templateStyle = "STANDARD_GST",
            updatedAt = 3000
        )

        val unsyncedBefore = database.storeBookQueries.getUnsyncedInvoiceSettings().executeAsList()
        assertEquals(1, unsyncedBefore.size, "Should be unsynced before marking")

        val insertedRow = database.storeBookQueries.getInvoiceSettingsByStoreId("store-2").executeAsOneOrNull()
        assertNotNull(insertedRow)

        database.storeBookQueries.markInvoiceSettingsSynced(
            id = insertedRow!!.id,
            cloudId = "cloud-abc"
        )

        val unsyncedAfter = database.storeBookQueries.getUnsyncedInvoiceSettings().executeAsList()
        assertEquals(0, unsyncedAfter.size, "Must be empty after marking synced")

        val synced = database.storeBookQueries.getInvoiceSettingsByStoreId("store-2").executeAsOneOrNull()
        assertNotNull(synced)
        assertEquals(1, synced!!.is_synced)
        assertEquals("cloud-abc", synced.cloud_id)
    }

    @Test
    fun remote_upsert_overwrites_existing_and_sets_synced_to_one() {
        database.storeBookQueries.insertOrUpdateInvoiceSettings(
            storeId = "store-3",
            shopName = "Original Shop",
            shopAddress = null,
            shopGstin = null,
            logoPath = null,
            accentColor = null,
            headerText = null,
            footerText = null,
            bankDetails = null,
            terms = null,
            showGstBreakdown = null,
            templateStyle = "STANDARD_GST",
            updatedAt = 100
        )

        database.storeBookQueries.upsertInvoiceSettingsRemote(
            storeId = "store-3",
            shopName = "Remote Updated Shop",
            shopAddress = "Mumbai",
            shopGstin = "27AABCU9603R1ZM",
            logoPath = "https://example.com/logo.png",
            accentColor = "#00ff00",
            headerText = null,
            footerText = null,
            bankDetails = null,
            terms = null,
            showGstBreakdown = 0L,
            templateStyle = "THERMAL_80MM",
            cloudId = "remote-123",
            updatedAt = 5000
        )

        val row = database.storeBookQueries.getInvoiceSettingsByStoreId("store-3").executeAsOneOrNull()
        assertNotNull(row)
        assertEquals("Remote Updated Shop", row!!.shop_name)
        assertEquals(1, row.is_synced)
        assertEquals("remote-123", row.cloud_id, "Cloud ID must be set by remote upsert")
    }

    @Test
    fun get_invoice_settings_returns_null_for_unknown_store() {
        val row = database.storeBookQueries.getInvoiceSettingsByStoreId("nonexistent").executeAsOneOrNull()
        assertNull(row)
    }
}
