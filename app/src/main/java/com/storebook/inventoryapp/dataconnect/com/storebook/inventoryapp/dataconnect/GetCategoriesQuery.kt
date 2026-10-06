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

public interface GetCategoriesQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
        StorebookConnectorConnector,
        GetCategoriesQuery.Data,
        GetCategoriesQuery.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val businessType: String,
    )

    @kotlinx.serialization.Serializable
    public data class Data(
        val categories: List<CategoryItem>,
    ) {
        @kotlinx.serialization.Serializable
        public data class CategoryItem(
            val id: String,
            val businessType: String,
            val storeId: String? = null,
            val name: String,
            val isDeleted: Boolean,
            val updatedAt: Double,
        )
    }

    public companion object {
        public val operationName: String = "GetCategories"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun GetCategoriesQuery.ref(
    businessType: String,
): com.google.firebase.dataconnect.QueryRef<
    GetCategoriesQuery.Data,
    GetCategoriesQuery.Variables,
> =
    ref(
        GetCategoriesQuery.Variables(
            businessType = businessType,
        ),
    )

public suspend fun GetCategoriesQuery.execute(
    businessType: String,
): com.google.firebase.dataconnect.QueryResult<
    GetCategoriesQuery.Data,
    GetCategoriesQuery.Variables,
> =
    ref(
        businessType = businessType,
    ).execute()

public fun GetCategoriesQuery.flow(
    businessType: String,
): kotlinx.coroutines.flow.Flow<GetCategoriesQuery.Data> =
    ref(
        businessType = businessType,
    ).subscribe()
        .flow
        ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
        ._flow_filterNotNull()
        ._flow_map { it.data }
