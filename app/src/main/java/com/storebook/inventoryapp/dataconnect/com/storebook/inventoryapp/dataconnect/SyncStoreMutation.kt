
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

public interface SyncStoreMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        SyncStoreMutation.Data,
        SyncStoreMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val name: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val isActive: com.google.firebase.dataconnect.OptionalVariable<Boolean?>,
        val isPremium: com.google.firebase.dataconnect.OptionalVariable<Boolean?>,
        val subscriptionExpiresAt: com.google.firebase.dataconnect.OptionalVariable<Double?>,
        val subscriptionPlatform: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val subscriptionId: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val subscriptionStatus: com.google.firebase.dataconnect.OptionalVariable<String?>,
    ) {
        @kotlin.DslMarker public annotation class BuilderDsl

        @BuilderDsl
        public interface Builder {
            public var id: String
            public var name: String?
            public var isActive: Boolean?
            public var isPremium: Boolean?
            public var subscriptionExpiresAt: Double?
            public var subscriptionPlatform: String?
            public var subscriptionId: String?
            public var subscriptionStatus: String?
        }

        public companion object {
            @Suppress("NAME_SHADOWING")
            public fun build(
                id: String,
                block_: Builder.() -> Unit,
            ): Variables {
                var id = id
                var name: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var isActive: com.google.firebase.dataconnect.OptionalVariable<Boolean?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var isPremium: com.google.firebase.dataconnect.OptionalVariable<Boolean?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var subscriptionExpiresAt: com.google.firebase.dataconnect.OptionalVariable<Double?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var subscriptionPlatform: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var subscriptionId: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var subscriptionStatus: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined

                return object : Builder {
                    override var id: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            id = value_
                        }

                    override var name: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            name =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var isActive: Boolean?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            isActive =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var isPremium: Boolean?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            isPremium =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var subscriptionExpiresAt: Double?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            subscriptionExpiresAt =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var subscriptionPlatform: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            subscriptionPlatform =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var subscriptionId: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            subscriptionId =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var subscriptionStatus: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            subscriptionStatus =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }
                }.apply(block_)
                    .let {
                        Variables(
                            id = id, name = name, isActive = isActive, isPremium = isPremium, subscriptionExpiresAt = subscriptionExpiresAt, subscriptionPlatform = subscriptionPlatform, subscriptionId = subscriptionId, subscriptionStatus = subscriptionStatus,
                        )
                    }
            }
        }
    }

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("store_upsert")
        val key: StoreKey,
    )

    public companion object {
        public val operationName: String = "SyncStore"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun SyncStoreMutation.ref(
    id: String,
    block_: SyncStoreMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationRef<
    SyncStoreMutation.Data,
    SyncStoreMutation.Variables,
> =
    ref(
        SyncStoreMutation.Variables.build(
            id = id,
            block_,
        ),
    )

public suspend fun SyncStoreMutation.execute(
    id: String,
    block_: SyncStoreMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationResult<
    SyncStoreMutation.Data,
    SyncStoreMutation.Variables,
> =
    ref(
        id = id,
        block_,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
