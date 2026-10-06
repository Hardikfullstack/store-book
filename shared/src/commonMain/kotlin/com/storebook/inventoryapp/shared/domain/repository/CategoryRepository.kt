package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.Categories
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.models.Category
import com.storebook.inventoryapp.shared.domain.models.getDefaultCategoriesForBusinessType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val database: StoreBookDatabase,
) {
    companion object {
        private val _categoriesUpdated = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val categoriesUpdated = _categoriesUpdated.asSharedFlow()

        fun notifyUpdated() {
            _categoriesUpdated.tryEmit(Unit)
        }
    }

    private val queries = database.storeBookQueries

    suspend fun getCategoriesByBusinessType(businessType: String): List<Category> = withContext(Dispatchers.IO) {
        val list = queries.getCategoriesByBusinessType(businessType).executeAsList()
        list.map { it.toDomain() }
    }

    suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.IO) {
        queries.getAllCategories().executeAsList().map { it.toDomain() }
    }

    suspend fun seedDefaultCategoriesIfEmpty(businessType: String, storeId: String? = null) = withContext(Dispatchers.IO) {
        val defaults = getDefaultCategoriesForBusinessType(businessType)
        val existing = queries.getCategoriesByBusinessType(businessType).executeAsList()
            .map { it.name.lowercase().trim() }
            .toSet()
        val toInsert = defaults.filter { !existing.contains(it.lowercase().trim()) }
        if (toInsert.isNotEmpty()) {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            database.transaction {
                toInsert.forEach { defaultName ->
                    queries.insertCategory(
                        name = defaultName.trim(),
                        businessType = businessType,
                        storeId = storeId,
                        cloudId = null,
                        isDefault = 1L,
                        isSynced = 0L,
                        updatedAt = now,
                    )
                }
            }
            notifyUpdated()
        }
    }

    suspend fun insertCategory(
        name: String,
        businessType: String,
        storeId: String? = null,
        cloudId: String? = null,
        isDefault: Boolean = false,
        isSynced: Boolean = false,
        updatedAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
    ): Category = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        val existing = queries.getCategoryByNameAndBusinessType(businessType, trimmedName).executeAsOneOrNull()
        if (existing != null) {
            if (cloudId != null && existing.cloud_id == null) {
                queries.markCategorySynced(cloudId = cloudId, updatedAt = updatedAt, id = existing.id)
                notifyUpdated()
                return@withContext existing.toDomain().copy(cloudId = cloudId, isSynced = true)
            }
            return@withContext existing.toDomain()
        }

        database.transaction {
            queries.insertCategory(
                name = trimmedName,
                businessType = businessType,
                storeId = storeId,
                cloudId = cloudId,
                isDefault = if (isDefault) 1L else 0L,
                isSynced = if (isSynced) 1L else 0L,
                updatedAt = updatedAt,
            )
        }
        notifyUpdated()
        val inserted = queries.getCategoryByNameAndBusinessType(businessType, trimmedName).executeAsOne()
        inserted.toDomain()
    }

    suspend fun upsertCategoryRemote(
        name: String,
        businessType: String,
        storeId: String?,
        cloudId: String,
        isDefault: Boolean = false,
        isDeleted: Boolean,
        updatedAt: Long,
    ) = withContext(Dispatchers.IO) {
        database.transaction {
            queries.upsertCategoryRemote(
                cloudId = cloudId,
                businessType = businessType,
                name = name,
                storeId = storeId,
                isDefault = if (isDefault) 1L else 0L,
                isDeleted = if (isDeleted) 1L else 0L,
                updatedAt = updatedAt,
            )
        }
        notifyUpdated()
    }

    suspend fun getUnsyncedCategories(): List<Categories> = withContext(Dispatchers.IO) {
        queries.getUnsyncedCategories().executeAsList()
    }

    suspend fun markCategorySynced(id: Long, cloudId: String, updatedAt: Long) = withContext(Dispatchers.IO) {
        queries.markCategorySynced(cloudId = cloudId, updatedAt = updatedAt, id = id)
        notifyUpdated()
    }

    private fun Categories.toDomain(): Category = Category(
        id = id,
        name = name,
        businessType = business_type,
        storeId = store_id,
        cloudId = cloud_id,
        isDefault = is_default == 1L,
        isSynced = is_synced == 1L,
        isDeleted = is_deleted == 1L,
        updatedAt = updated_at,
    )
}
