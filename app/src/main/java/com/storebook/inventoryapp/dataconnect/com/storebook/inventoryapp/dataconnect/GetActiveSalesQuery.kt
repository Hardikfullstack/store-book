
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

public interface GetActiveSalesQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
        StorebookConnectorConnector,
        GetActiveSalesQuery.Data,
        GetActiveSalesQuery.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val storeId: String,
        val type: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val limit: com.google.firebase.dataconnect.OptionalVariable<Int?>,
        val offset: com.google.firebase.dataconnect.OptionalVariable<Int?>,
        val searchTerm: com.google.firebase.dataconnect.OptionalVariable<String?>,
        val orderByTimestamp: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
        val orderByCustomerName: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
        val orderByTotalAmount: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    ) {
        @kotlin.DslMarker public annotation class BuilderDsl

        @BuilderDsl
        public interface Builder {
            public var storeId: String
            public var type: String?
            public var limit: Int?
            public var offset: Int?
            public var searchTerm: String?
            public var orderByTimestamp: OrderDirection?
            public var orderByCustomerName: OrderDirection?
            public var orderByTotalAmount: OrderDirection?
        }

        public companion object {
            @Suppress("NAME_SHADOWING")
            public fun build(
                storeId: String,
                block_: Builder.() -> Unit,
            ): Variables {
                var storeId = storeId
                var type: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var limit: com.google.firebase.dataconnect.OptionalVariable<Int?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var offset: com.google.firebase.dataconnect.OptionalVariable<Int?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var searchTerm: com.google.firebase.dataconnect.OptionalVariable<String?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var orderByTimestamp: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var orderByCustomerName: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined
                var orderByTotalAmount: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                    com.google.firebase.dataconnect.OptionalVariable.Undefined

                return object : Builder {
                    override var storeId: String
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            storeId = value_
                        }

                    override var type: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            type =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var limit: Int?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            limit =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var offset: Int?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            offset =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var searchTerm: String?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            searchTerm =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var orderByTimestamp: OrderDirection?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            orderByTimestamp =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var orderByCustomerName: OrderDirection?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            orderByCustomerName =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }

                    override var orderByTotalAmount: OrderDirection?
                        get() = throw UnsupportedOperationException("getting builder values is not supported")
                        set(value_) {
                            orderByTotalAmount =
                                com.google.firebase.dataconnect.OptionalVariable
                                    .Value(value_)
                        }
                }.apply(block_)
                    .let {
                        Variables(
                            storeId = storeId, type = type, limit = limit, offset = offset, searchTerm = searchTerm, orderByTimestamp = orderByTimestamp, orderByCustomerName = orderByCustomerName, orderByTotalAmount = orderByTotalAmount,
                        )
                    }
            }
        }
    }

    @kotlinx.serialization.Serializable
    public data class Data(
        val sales: List<SalesItem>,
    ) {
        @kotlinx.serialization.Serializable
        public data class SalesItem(
            val id: String,
            val timestamp: Double,
            val totalAmount: Double,
            val discountAmount: Double,
            val customerName: String?,
            val type: String,
            val notes: String?,
            val updatedAt: Double,
        )
    }

    public companion object {
        public val operationName: String = "GetActiveSales"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun GetActiveSalesQuery.ref(
    storeId: String,
    block_: GetActiveSalesQuery.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.QueryRef<
    GetActiveSalesQuery.Data,
    GetActiveSalesQuery.Variables,
> =
    ref(
        GetActiveSalesQuery.Variables.build(
            storeId = storeId,
            block_,
        ),
    )

public suspend fun GetActiveSalesQuery.execute(
    storeId: String,
    block_: GetActiveSalesQuery.Variables.Builder.() -> Unit,
): com.google.firebase.dataconnect.QueryResult<
    GetActiveSalesQuery.Data,
    GetActiveSalesQuery.Variables,
> =
    ref(
        storeId = storeId,
        block_,
    ).execute()

public fun GetActiveSalesQuery.flow(
    storeId: String,
    block_: GetActiveSalesQuery.Variables.Builder.() -> Unit,
): kotlinx.coroutines.flow.Flow<GetActiveSalesQuery.Data> =
    ref(
        storeId = storeId,
        block_,
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
