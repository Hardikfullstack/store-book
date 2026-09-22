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

import kotlinx.coroutines.flow.filterNotNull as _flow_filterNotNull
import kotlinx.coroutines.flow.map as _flow_map

public interface SyncStockAdjustmentsQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
        StorebookConnectorConnector,
        SyncStockAdjustmentsQuery.Data,
        SyncStockAdjustmentsQuery.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val storeId: String,
        val lastSync: Double,
    )

    @kotlinx.serialization.Serializable
    public data class Data(
        val stockAdjustments: List<StockAdjustmentsItem>,
    ) {
        @kotlinx.serialization.Serializable
        public data class StockAdjustmentsItem(
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
    }

    public companion object {
        public val operationName: String = "SyncStockAdjustments"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun SyncStockAdjustmentsQuery.ref(
    storeId: String,
    lastSync: Double,
): com.google.firebase.dataconnect.QueryRef<
    SyncStockAdjustmentsQuery.Data,
    SyncStockAdjustmentsQuery.Variables,
> =
    ref(
        SyncStockAdjustmentsQuery.Variables(
            storeId = storeId,
            lastSync = lastSync,
        ),
    )

public suspend fun SyncStockAdjustmentsQuery.execute(
    storeId: String,
    lastSync: Double,
): com.google.firebase.dataconnect.QueryResult<
    SyncStockAdjustmentsQuery.Data,
    SyncStockAdjustmentsQuery.Variables,
> =
    ref(
        storeId = storeId,
        lastSync = lastSync,
    ).execute()

public fun SyncStockAdjustmentsQuery.flow(
    storeId: String,
    lastSync: Double,
): kotlinx.coroutines.flow.Flow<SyncStockAdjustmentsQuery.Data> =
    ref(
        storeId = storeId,
        lastSync = lastSync,
    ).subscribe()
        .flow
        ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
        ._flow_filterNotNull()
        ._flow_map { it.data }
