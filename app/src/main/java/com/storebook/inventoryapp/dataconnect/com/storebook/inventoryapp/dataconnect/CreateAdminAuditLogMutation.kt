
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

public interface CreateAdminAuditLogMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        CreateAdminAuditLogMutation.Data,
        CreateAdminAuditLogMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val adminId: String,
        val adminUsername: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val action: String,
        val targetId: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val details: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val timestamp: Double,
    ) {
        @kotlin.DslMarker public annotation class BuilderDsl

        @BuilderDsl
        public interface Builder {
            public var adminId: String
            public var adminUsername: String?
            public var action: String
            public var targetId: String?
            public var details: String?
            public var timestamp: Double
        }

        public companion object {
            @Suppress("NAME_SHADOWING")
            public fun build(
                adminId: String,
                action: String,
                timestamp: Double,
                block_: Builder.() -> Unit,
            ): Variables {
                var adminId = adminId
                var adminUsername: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var action = action
                var targetId: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var details: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var timestamp = timestamp

                return object : Builder {
                    override var adminId: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            adminId = value_
                        }

                    override var adminUsername: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            adminUsername =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var action: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            action = value_
                        }

                    override var targetId: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            targetId =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var details: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            details =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var timestamp: Double
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            timestamp = value_
                        }
                }.apply(block_)
                    .let {
                        Variables(
                            adminId = adminId, adminUsername = adminUsername, action = action, targetId = targetId, details = details, timestamp = timestamp,
                        )
                    }
            }
        }
    }

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("adminAuditLog_insert")
        val key: AdminAuditLogKey,
    )

    public companion object {
        public val operationName: String = "CreateAdminAuditLog"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun CreateAdminAuditLogMutation.ref(
    adminId: String,
    action: String,
    timestamp: Double,
    block_: CreateAdminAuditLogMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationRef<
    CreateAdminAuditLogMutation.Data,
    CreateAdminAuditLogMutation.Variables,
> =
    ref(
        CreateAdminAuditLogMutation.Variables.build(
            adminId = adminId, action = action, timestamp = timestamp,
            block_,
        ),
    )

public suspend fun CreateAdminAuditLogMutation.execute(
    adminId: String,
    action: String,
    timestamp: Double,
    block_: CreateAdminAuditLogMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationResult<
    CreateAdminAuditLogMutation.Data,
    CreateAdminAuditLogMutation.Variables,
> =
    ref(
        adminId = adminId, action = action, timestamp = timestamp,
        block_,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
