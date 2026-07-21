
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

public interface CreateUserMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        CreateUserMutation.Data,
        CreateUserMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val role: String,
        val createdAt: Double,
        val storeId: String,
        val canViewProfit: com.google.firebase.dataconnect.OptionalVariable<Boolean?>,
        val canDelete: com.google.firebase.dataconnect.OptionalVariable<Boolean?>,
    ) {
        @kotlin.DslMarker public annotation class BuilderDsl

        @BuilderDsl
        public interface Builder {
            public var id: String
            public var role: String
            public var createdAt: Double
            public var storeId: String
            public var canViewProfit: Boolean?
            public var canDelete: Boolean?
        }

        public companion object {
            @Suppress("NAME_SHADOWING")
            public fun build(
                id: String,
                role: String,
                createdAt: Double,
                storeId: String,
                block_: Builder.() -> Unit,
            ): Variables {
                var id = id
                var role = role
                var createdAt = createdAt
                var storeId = storeId
                var canViewProfit: com.google.firebase.dataconnect.OptionalVariable<Boolean?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var canDelete: com.google.firebase.dataconnect.OptionalVariable<Boolean?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined

                return object : Builder {
                    override var id: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            id = value_
                        }

                    override var role: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            role = value_
                        }

                    override var createdAt: Double
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            createdAt = value_
                        }

                    override var storeId: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            storeId = value_
                        }

                    override var canViewProfit: Boolean?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            canViewProfit =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var canDelete: Boolean?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            canDelete =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }
                }.apply(block_)
                    .let {
                        Variables(
                            id = id, role = role, createdAt = createdAt, storeId = storeId, canViewProfit = canViewProfit, canDelete = canDelete,
                        )
                    }
            }
        }
    }

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("user_upsert")
        val key: UserKey,
    )

    public companion object {
        public val operationName: String = "CreateUser"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun CreateUserMutation.ref(
    id: String,
    role: String,
    createdAt: Double,
    storeId: String,
    block_: CreateUserMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationRef<
    CreateUserMutation.Data,
    CreateUserMutation.Variables,
> =
    ref(
        CreateUserMutation.Variables.build(
            id = id, role = role, createdAt = createdAt, storeId = storeId,
            block_,
        ),
    )

public suspend fun CreateUserMutation.execute(
    id: String,
    role: String,
    createdAt: Double,
    storeId: String,
    block_: CreateUserMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationResult<
    CreateUserMutation.Data,
    CreateUserMutation.Variables,
> =
    ref(
        id = id, role = role, createdAt = createdAt, storeId = storeId,
        block_,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
