
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

public interface UpsertGlobalSettingMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        UpsertGlobalSettingMutation.Data,
        UpsertGlobalSettingMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val key: String,
        val value: String,
        val description: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val updatedAt: Double,
        val updatedBy: com.google.firebase.dataconnect.OptionalVariable<String?>,
    ) {
        @kotlin.DslMarker public annotation class BuilderDsl

        @BuilderDsl
        public interface Builder {
            public var id: String
            public var key: String
            public var value: String
            public var description: String?
            public var updatedAt: Double
            public var updatedBy: String?
        }

        public companion object {
            @Suppress("NAME_SHADOWING")
            public fun build(
                id: String,
                key: String,
                value: String,
                updatedAt: Double,
                block_: Builder.() -> Unit,
            ): Variables {
                var id = id
                var key = key
                var value = value
                var description: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var updatedAt = updatedAt
                var updatedBy: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined

                return object : Builder {
                    override var id: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            id = value_
                        }

                    override var key: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            key = value_
                        }

                    override var value: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            value = value_
                        }

                    override var description: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            description =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var updatedAt: Double
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            updatedAt = value_
                        }

                    override var updatedBy: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            updatedBy =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }
                }.apply(block_)
                    .let {
                        Variables(
                            id = id, key = key, value = value, description = description, updatedAt = updatedAt, updatedBy = updatedBy,
                        )
                    }
            }
        }
    }

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("globalSetting_upsert")
        val key: GlobalSettingKey,
    )

    public companion object {
        public val operationName: String = "UpsertGlobalSetting"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun UpsertGlobalSettingMutation.ref(
    id: String,
    key: String,
    value: String,
    updatedAt: Double,
    block_: UpsertGlobalSettingMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationRef<
    UpsertGlobalSettingMutation.Data,
    UpsertGlobalSettingMutation.Variables,
> =
    ref(
        UpsertGlobalSettingMutation.Variables.build(
            id = id, key = key, value = value, updatedAt = updatedAt,
            block_,
        ),
    )

public suspend fun UpsertGlobalSettingMutation.execute(
    id: String,
    key: String,
    value: String,
    updatedAt: Double,
    block_: UpsertGlobalSettingMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationResult<
    UpsertGlobalSettingMutation.Data,
    UpsertGlobalSettingMutation.Variables,
> =
    ref(
        id = id, key = key, value = value, updatedAt = updatedAt,
        block_,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
