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

public interface SyncCategoryMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        SyncCategoryMutation.Data,
        SyncCategoryMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val businessType: String,
        val storeId: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val name: String,
        val isDeleted: Boolean,
        val updatedAt: Double,
    )

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("category_upsert")
        val key: CategoryKey,
    )

    public companion object {
        public val operationName: String = "SyncCategory"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun SyncCategoryMutation.ref(
    id: String,
    businessType: String,
    name: String,
    isDeleted: Boolean,
    updatedAt: Double,
    storeId: String? = null,
): com.google.firebase.dataconnect.MutationRef<
    SyncCategoryMutation.Data,
    SyncCategoryMutation.Variables,
> =
    ref(
        SyncCategoryMutation.Variables(
            id = id,
            businessType = businessType,
            storeId = if (storeId != null) com.google.firebase.dataconnect.OptionalVariable.Value(storeId) else com.google.firebase.dataconnect.OptionalVariable.Undefined,
            name = name,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        ),
    )

public suspend fun SyncCategoryMutation.execute(
    id: String,
    businessType: String,
    name: String,
    isDeleted: Boolean,
    updatedAt: Double,
    storeId: String? = null,
): com.google.firebase.dataconnect.MutationResult<
    SyncCategoryMutation.Data,
    SyncCategoryMutation.Variables,
> =
    ref(
        id = id,
        businessType = businessType,
        name = name,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        storeId = storeId,
    ).execute()
