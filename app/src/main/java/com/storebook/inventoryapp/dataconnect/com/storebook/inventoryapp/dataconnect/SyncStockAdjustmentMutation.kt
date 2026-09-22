@file:kotlin.Suppress(
    "KotlinRedundantDiagnosticSuppress",
    "LocalVariableName",
    "MayBeConstant",
    "RedundantVisibilityModifier",
    "RemoveEmptyClassBody",
    "SpellCheckingInspection",
    "LocalVariableName",
    "unused",
)

package com.storebook.inventoryapp.dataconnect

public interface SyncStockAdjustmentMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        SyncStockAdjustmentMutation.Data,
        SyncStockAdjustmentMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val storeId: String,
        val itemId: String,
        val itemName: String,
        val reason: String,
        val delta: Double,
        val timestamp: Double,
        val isDeleted: Boolean,
        val updatedAt: Double,
    )

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("stockAdjustment_upsert")
        val key: StockAdjustmentKey,
    )

    public companion object {
        public val operationName: String = "SyncStockAdjustment"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun SyncStockAdjustmentMutation.ref(
    id: String,
    storeId: String,
    itemId: String,
    itemName: String,
    reason: String,
    delta: Double,
    timestamp: Double,
    isDeleted: Boolean,
    updatedAt: Double,
): com.google.firebase.dataconnect.MutationRef<
    SyncStockAdjustmentMutation.Data,
    SyncStockAdjustmentMutation.Variables,
> =
    ref(
        SyncStockAdjustmentMutation.Variables(
            id = id,
            storeId = storeId,
            itemId = itemId,
            itemName = itemName,
            reason = reason,
            delta = delta,
            timestamp = timestamp,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        ),
    )

public suspend fun SyncStockAdjustmentMutation.execute(
    id: String,
    storeId: String,
    itemId: String,
    itemName: String,
    reason: String,
    delta: Double,
    timestamp: Double,
    isDeleted: Boolean,
    updatedAt: Double,
): com.google.firebase.dataconnect.MutationResult<
    SyncStockAdjustmentMutation.Data,
    SyncStockAdjustmentMutation.Variables,
> =
    ref(
        id = id,
        storeId = storeId,
        itemId = itemId,
        itemName = itemName,
        reason = reason,
        delta = delta,
        timestamp = timestamp,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
    ).execute()
