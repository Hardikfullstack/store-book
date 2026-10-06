package com.storebook.inventoryapp.shared.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long = 0L,
    val name: String,
    val businessType: String,
    val storeId: String? = null,
    val cloudId: String? = null,
    val isDefault: Boolean = false,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = 0L,
)
