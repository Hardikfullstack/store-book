package com.storebook.inventoryapp.shared.test.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.repository.CategoryRepository
import com.storebook.inventoryapp.shared.test.DatabaseTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CategoryRepositoryTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var repository: CategoryRepository

    @BeforeEach
    fun setup() {
        val (db, d) = DatabaseTestHelper.createDatabase()
        database = db
        driver = d as JdbcSqliteDriver
        repository = CategoryRepository(database)
    }

    @AfterEach
    fun teardown() {
        DatabaseTestHelper.dropDatabase(driver)
    }

    @Test
    fun `seedDefaultCategoriesIfEmpty populates default categories for grocery`() = runBlocking {
        repository.seedDefaultCategoriesIfEmpty("grocery", "store_123")
        val categories = repository.getCategoriesByBusinessType("grocery")

        assertTrue(categories.isNotEmpty())
        assertTrue(categories.any { it.name == "Pulses & Dals" })
        assertTrue(categories.any { it.name == "Edible Oils & Ghee" })

        // Seeding again should be idempotent and not create duplicates
        repository.seedDefaultCategoriesIfEmpty("grocery", "store_123")
        val categoriesAfterSecondSeed = repository.getCategoriesByBusinessType("grocery")
        assertEquals(categories.size, categoriesAfterSecondSeed.size)
    }

    @Test
    fun `insertCategory creates local category and returns domain model`() = runBlocking {
        val created = repository.insertCategory(
            name = "Organic Millets",
            businessType = "grocery",
            storeId = "store_abc",
            cloudId = "uuid-1234",
            isSynced = true,
        )

        assertNotNull(created)
        assertEquals("Organic Millets", created.name)
        assertEquals("grocery", created.businessType)
        assertEquals("uuid-1234", created.cloudId)
        assertTrue(created.isSynced)

        val list = repository.getCategoriesByBusinessType("grocery")
        assertEquals(1, list.size)
        assertEquals("Organic Millets", list[0].name)
    }

    @Test
    fun `upsertCategoryRemote inserts or updates category from cloud`() = runBlocking {
        repository.upsertCategoryRemote(
            name = "Imported Cheese",
            businessType = "dairy_sweets",
            storeId = "store_xyz",
            cloudId = "cloud-uuid-777",
            isDeleted = false,
            updatedAt = 1000L,
        )

        val list = repository.getCategoriesByBusinessType("dairy_sweets")
        assertEquals(1, list.size)
        assertEquals("Imported Cheese", list[0].name)
        assertEquals("cloud-uuid-777", list[0].cloudId)
        assertTrue(list[0].isSynced)
    }
}
