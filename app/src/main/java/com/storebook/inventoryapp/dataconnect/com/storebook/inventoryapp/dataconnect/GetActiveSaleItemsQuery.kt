
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

public interface GetActiveSaleItemsQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
        StorebookConnectorConnector,
        GetActiveSaleItemsQuery.Data,
        GetActiveSaleItemsQuery.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val storeId: String,
    )

    @kotlinx.serialization.Serializable
    public data class Data(
        val saleItemDetails: List<SaleItemDetailsItem>,
    ) {
        @kotlinx.serialization.Serializable
        public data class SaleItemDetailsItem(
            val id: String,
            val saleId: String,
            val itemId: String,
            val itemName: String,
            val quantity: Double,
            val sellPrice: Double,
            val buyPrice: Double,
        )
    }

    public companion object {
        public val operationName: String = "GetActiveSaleItems"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun GetActiveSaleItemsQuery.ref(
    storeId: String,
): com.google.firebase.dataconnect.QueryRef<
    GetActiveSaleItemsQuery.Data,
    GetActiveSaleItemsQuery.Variables,
> =
    ref(
        GetActiveSaleItemsQuery.Variables(
            storeId = storeId,
        ),
    )

public suspend fun GetActiveSaleItemsQuery.execute(
    storeId: String,
): com.google.firebase.dataconnect.QueryResult<
    GetActiveSaleItemsQuery.Data,
    GetActiveSaleItemsQuery.Variables,
> =
    ref(
        storeId = storeId,
    ).execute()

public fun GetActiveSaleItemsQuery.flow(storeId: String): kotlinx.coroutines.flow.Flow<GetActiveSaleItemsQuery.Data> =
    ref(
        storeId = storeId,
    ).subscribe()
        .flow
        ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
        ._flow_filterNotNull()
        ._flow_map { it.data }

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
