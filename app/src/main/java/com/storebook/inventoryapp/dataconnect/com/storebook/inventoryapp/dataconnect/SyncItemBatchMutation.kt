
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

public interface SyncItemBatchMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        SyncItemBatchMutation.Data,
        SyncItemBatchMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val storeId: String,
        val itemId: String,
        val batchNumber: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val expiryDate: com.google.firebase.dataconnect.OptionalVariable<Double?>,
        val quantity: Double,
        val costPrice: Double,
        val timestamp: Double,
        val notes: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val isDeleted: Boolean,
        val updatedAt: Double,
    ) {
        @kotlin.DslMarker public annotation class BuilderDsl

        @BuilderDsl
        public interface Builder {
            public var id: String
            public var storeId: String
            public var itemId: String
            public var batchNumber: String?
            public var expiryDate: Double?
            public var quantity: Double
            public var costPrice: Double
            public var timestamp: Double
            public var notes: String?
            public var isDeleted: Boolean
            public var updatedAt: Double
        }

        public companion object {
            @Suppress("NAME_SHADOWING")
            public fun build(
                id: String,
                storeId: String,
                itemId: String,
                quantity: Double,
                costPrice: Double,
                timestamp: Double,
                isDeleted: Boolean,
                updatedAt: Double,
                block_: Builder.() -> Unit,
            ): Variables {
                var id = id
                var storeId = storeId
                var itemId = itemId
                var batchNumber: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var expiryDate: com.google.firebase.dataconnect.OptionalVariable<Double?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var quantity = quantity
                var costPrice = costPrice
                var timestamp = timestamp
                var notes: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var isDeleted = isDeleted
                var updatedAt = updatedAt

                return object : Builder {
                    override var id: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            id = value_
                        }

                    override var storeId: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            storeId = value_
                        }

                    override var itemId: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            itemId = value_
                        }

                    override var batchNumber: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            batchNumber =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var expiryDate: Double?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            expiryDate =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var quantity: Double
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            quantity = value_
                        }

                    override var costPrice: Double
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            costPrice = value_
                        }

                    override var timestamp: Double
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            timestamp = value_
                        }

                    override var notes: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            notes =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var isDeleted: Boolean
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            isDeleted = value_
                        }

                    override var updatedAt: Double
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            updatedAt = value_
                        }
                }.apply(block_)
                    .let {
                        Variables(
                            id = id, storeId = storeId, itemId = itemId, batchNumber = batchNumber, expiryDate = expiryDate, quantity = quantity, costPrice = costPrice, timestamp = timestamp, notes = notes, isDeleted = isDeleted, updatedAt = updatedAt,
                        )
                    }
            }
        }
    }

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("itemBatch_upsert")
        val key: ItemBatchKey,
    )

    public companion object {
        public val operationName: String = "SyncItemBatch"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun SyncItemBatchMutation.ref(
    id: String,
    storeId: String,
    itemId: String,
    quantity: Double,
    costPrice: Double,
    timestamp: Double,
    isDeleted: Boolean,
    updatedAt: Double,
    block_: SyncItemBatchMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationRef<
    SyncItemBatchMutation.Data,
    SyncItemBatchMutation.Variables,
> =
    ref(
        SyncItemBatchMutation.Variables.build(
            id = id, storeId = storeId, itemId = itemId, quantity = quantity, costPrice = costPrice, timestamp = timestamp, isDeleted = isDeleted, updatedAt = updatedAt,
            block_,
        ),
    )

public suspend fun SyncItemBatchMutation.execute(
    id: String,
    storeId: String,
    itemId: String,
    quantity: Double,
    costPrice: Double,
    timestamp: Double,
    isDeleted: Boolean,
    updatedAt: Double,
    block_: SyncItemBatchMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationResult<
    SyncItemBatchMutation.Data,
    SyncItemBatchMutation.Variables,
> =
    ref(
        id = id, storeId = storeId, itemId = itemId, quantity = quantity, costPrice = costPrice, timestamp = timestamp, isDeleted = isDeleted, updatedAt = updatedAt,
        block_,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
