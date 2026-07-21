
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

public interface SyncPurchaseItemMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        SyncPurchaseItemMutation.Data,
        SyncPurchaseItemMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val storeId: String,
        val purchaseId: String,
        val itemId: String,
        val itemName: String,
        val quantity: Double,
        val unit: String,
        val buyPrice: Double,
        val isDeleted: Boolean,
        val updatedAt: Double,
    )

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("purchaseItemDetail_upsert")
        val key: PurchaseItemDetailKey,
    )

    public companion object {
        public val operationName: String = "SyncPurchaseItem"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun SyncPurchaseItemMutation.ref(
    id: String,
    storeId: String,
    purchaseId: String,
    itemId: String,
    itemName: String,
    quantity: Double,
    unit: String,
    buyPrice: Double,
    isDeleted: Boolean,
    updatedAt: Double,
): com.google.firebase.dataconnect.MutationRef<
    SyncPurchaseItemMutation.Data,
    SyncPurchaseItemMutation.Variables,
> =
    ref(
        SyncPurchaseItemMutation.Variables(
            id = id, storeId = storeId, purchaseId = purchaseId, itemId = itemId, itemName = itemName, quantity = quantity, unit = unit, buyPrice = buyPrice, isDeleted = isDeleted, updatedAt = updatedAt,
        ),
    )

public suspend fun SyncPurchaseItemMutation.execute(
    id: String,
    storeId: String,
    purchaseId: String,
    itemId: String,
    itemName: String,
    quantity: Double,
    unit: String,
    buyPrice: Double,
    isDeleted: Boolean,
    updatedAt: Double,
): com.google.firebase.dataconnect.MutationResult<
    SyncPurchaseItemMutation.Data,
    SyncPurchaseItemMutation.Variables,
> =
    ref(
        id = id, storeId = storeId, purchaseId = purchaseId, itemId = itemId, itemName = itemName, quantity = quantity, unit = unit, buyPrice = buyPrice, isDeleted = isDeleted, updatedAt = updatedAt,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
