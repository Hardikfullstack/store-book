
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

public interface UpsertPromoCodeMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        UpsertPromoCodeMutation.Data,
        UpsertPromoCodeMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val code: String,
        val discountPercent: com.google.firebase.dataconnect.OptionalVariable<Double?>,
        val discountAmount: com.google.firebase.dataconnect.OptionalVariable<Double?>,
        val maxUses: com.google.firebase.dataconnect.OptionalVariable<Double?>,
        val expiresAt: com.google.firebase.dataconnect.OptionalVariable<Double?>,
        val isActive: Boolean,
    ) {
        @kotlin.DslMarker public annotation class BuilderDsl

        @BuilderDsl
        public interface Builder {
            public var id: String
            public var code: String
            public var discountPercent: Double?
            public var discountAmount: Double?
            public var maxUses: Double?
            public var expiresAt: Double?
            public var isActive: Boolean
        }

        public companion object {
            @Suppress("NAME_SHADOWING")
            public fun build(
                id: String,
                code: String,
                isActive: Boolean,
                block_: Builder.() -> Unit,
            ): Variables {
                var id = id
                var code = code
                var discountPercent: com.google.firebase.dataconnect.OptionalVariable<Double?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var discountAmount: com.google.firebase.dataconnect.OptionalVariable<Double?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var maxUses: com.google.firebase.dataconnect.OptionalVariable<Double?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var expiresAt: com.google.firebase.dataconnect.OptionalVariable<Double?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var isActive = isActive

                return object : Builder {
                    override var id: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            id = value_
                        }

                    override var code: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            code = value_
                        }

                    override var discountPercent: Double?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            discountPercent =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var discountAmount: Double?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            discountAmount =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var maxUses: Double?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            maxUses =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var expiresAt: Double?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            expiresAt =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var isActive: Boolean
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            isActive = value_
                        }
                }.apply(block_)
                    .let {
                        Variables(
                            id = id, code = code, discountPercent = discountPercent, discountAmount = discountAmount, maxUses = maxUses, expiresAt = expiresAt, isActive = isActive,
                        )
                    }
            }
        }
    }

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("promoCode_upsert")
        val key: PromoCodeKey,
    )

    public companion object {
        public val operationName: String = "UpsertPromoCode"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun UpsertPromoCodeMutation.ref(
    id: String,
    code: String,
    isActive: Boolean,
    block_: UpsertPromoCodeMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationRef<
    UpsertPromoCodeMutation.Data,
    UpsertPromoCodeMutation.Variables,
> =
    ref(
        UpsertPromoCodeMutation.Variables.build(
            id = id, code = code, isActive = isActive,
            block_,
        ),
    )

public suspend fun UpsertPromoCodeMutation.execute(
    id: String,
    code: String,
    isActive: Boolean,
    block_: UpsertPromoCodeMutation.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.MutationResult<
    UpsertPromoCodeMutation.Data,
    UpsertPromoCodeMutation.Variables,
> =
    ref(
        id = id, code = code, isActive = isActive,
        block_,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
