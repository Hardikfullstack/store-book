
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

import com.google.firebase.dataconnect.getInstance as _fdcGetInstance

public interface StorebookConnectorConnector :
    com.google.firebase.dataconnect.generated.GeneratedConnector<StorebookConnectorConnector> {
    override val dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect

    public val createAdminAuditLog: CreateAdminAuditLogMutation

    public val createUser: CreateUserMutation

    public val deleteAnnouncement: DeleteAnnouncementMutation

    public val deletePromoCode: DeletePromoCodeMutation

    public val getActiveExpenses: GetActiveExpensesQuery

    public val getActiveItems: GetActiveItemsQuery

    public val getActiveSaleItems: GetActiveSaleItemsQuery

    public val getActiveSales: GetActiveSalesQuery

    public val getActiveSuppliers: GetActiveSuppliersQuery

    public val getActiveUdhaars: GetActiveUdhaarsQuery

    public val getAdminAuditLogs: GetAdminAuditLogsQuery

    public val getAnnouncements: GetAnnouncementsQuery

    public val getExpenseEntriesCount: GetExpenseEntriesCountQuery

    public val getGlobalSettings: GetGlobalSettingsQuery

    public val getItemsCount: GetItemsCountQuery

    public val getPromoCodes: GetPromoCodesQuery

    public val getSalesCount: GetSalesCountQuery

    public val getStore: GetStoreQuery

    public val getStoresPaginated: GetStoresPaginatedQuery

    public val getUdhaarEntriesCount: GetUdhaarEntriesCountQuery

    public val getUser: GetUserQuery

    public val getUsersPaginated: GetUsersPaginatedQuery

    public val purgeStore: PurgeStoreMutation

    public val softDeleteExpense: SoftDeleteExpenseMutation

    public val softDeleteItem: SoftDeleteItemMutation

    public val softDeleteSale: SoftDeleteSaleMutation

    public val softDeleteUdhaar: SoftDeleteUdhaarMutation

    public val syncExpense: SyncExpenseMutation

    public val syncExpenses: SyncExpensesQuery

    public val syncItem: SyncItemMutation

    public val syncItemBatch: SyncItemBatchMutation

    public val syncItemBatches: SyncItemBatchesQuery

    public val syncItems: SyncItemsQuery

    public val syncPurchase: SyncPurchaseMutation

    public val syncPurchaseItem: SyncPurchaseItemMutation

    public val syncPurchaseItems: SyncPurchaseItemsQuery

    public val syncPurchases: SyncPurchasesQuery

    public val syncSale: SyncSaleMutation

    public val syncSaleItem: SyncSaleItemMutation

    public val syncSaleItems: SyncSaleItemsQuery

    public val syncSales: SyncSalesQuery

    public val syncStore: SyncStoreMutation

    public val syncSupplier: SyncSupplierMutation

    public val syncSuppliers: SyncSuppliersQuery

    public val syncUdhaar: SyncUdhaarMutation

    public val syncUdhaars: SyncUdhaarsQuery

    public val syncUser: SyncUserMutation

    public val toggleStoreStatus: ToggleStoreStatusMutation

    public val updateStore: UpdateStoreMutation

    public val updateUser: UpdateUserMutation

    public val upsertAnnouncement: UpsertAnnouncementMutation

    public val upsertGlobalSetting: UpsertGlobalSettingMutation

    public val upsertPromoCode: UpsertPromoCodeMutation

    public companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        public val config: com.google.firebase.dataconnect.ConnectorConfig =
            com.google.firebase.dataconnect.ConnectorConfig(
                connector = "storebook-connector",
                location = "us-central1",
                serviceId = "store-book",
            )

        public fun getInstance(
            dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect,
        ): StorebookConnectorConnector =
            synchronized(instances) {
                instances.getOrPut(dataConnect) {
                    StorebookConnectorConnectorImpl(dataConnect)
                }
            }

        private val instances =
            java.util
                .WeakHashMap<com.google.firebase.dataconnect.FirebaseDataConnect, StorebookConnectorConnectorImpl>()
    }
}

public val StorebookConnectorConnector.Companion.instance: StorebookConnectorConnector
    get() =
        getInstance(
            com.google.firebase.dataconnect.FirebaseDataConnect
                ._fdcGetInstance(config),
        )

public fun StorebookConnectorConnector.Companion.getInstance(
    settings: com.google.firebase.dataconnect.DataConnectSettings =
        com.google.firebase.dataconnect
            .DataConnectSettings(),
): StorebookConnectorConnector =
    getInstance(
        com.google.firebase.dataconnect.FirebaseDataConnect
            ._fdcGetInstance(config, settings),
    )

public fun StorebookConnectorConnector.Companion.getInstance(
    app: com.google.firebase.FirebaseApp,
    settings: com.google.firebase.dataconnect.DataConnectSettings =
        com.google.firebase.dataconnect
            .DataConnectSettings(),
): StorebookConnectorConnector =
    getInstance(
        com.google.firebase.dataconnect.FirebaseDataConnect
            ._fdcGetInstance(app, config, settings),
    )

private class StorebookConnectorConnectorImpl(
    override val dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect,
) : StorebookConnectorConnector {
    override val createAdminAuditLog by lazy(LazyThreadSafetyMode.PUBLICATION) {
        CreateAdminAuditLogMutationImpl(this)
    }

    override val createUser by lazy(LazyThreadSafetyMode.PUBLICATION) {
        CreateUserMutationImpl(this)
    }

    override val deleteAnnouncement by lazy(LazyThreadSafetyMode.PUBLICATION) {
        DeleteAnnouncementMutationImpl(this)
    }

    override val deletePromoCode by lazy(LazyThreadSafetyMode.PUBLICATION) {
        DeletePromoCodeMutationImpl(this)
    }

    override val getActiveExpenses by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetActiveExpensesQueryImpl(this)
    }

    override val getActiveItems by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetActiveItemsQueryImpl(this)
    }

    override val getActiveSaleItems by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetActiveSaleItemsQueryImpl(this)
    }

    override val getActiveSales by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetActiveSalesQueryImpl(this)
    }

    override val getActiveSuppliers by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetActiveSuppliersQueryImpl(this)
    }

    override val getActiveUdhaars by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetActiveUdhaarsQueryImpl(this)
    }

    override val getAdminAuditLogs by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetAdminAuditLogsQueryImpl(this)
    }

    override val getAnnouncements by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetAnnouncementsQueryImpl(this)
    }

    override val getExpenseEntriesCount by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetExpenseEntriesCountQueryImpl(this)
    }

    override val getGlobalSettings by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetGlobalSettingsQueryImpl(this)
    }

    override val getItemsCount by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetItemsCountQueryImpl(this)
    }

    override val getPromoCodes by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetPromoCodesQueryImpl(this)
    }

    override val getSalesCount by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetSalesCountQueryImpl(this)
    }

    override val getStore by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetStoreQueryImpl(this)
    }

    override val getStoresPaginated by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetStoresPaginatedQueryImpl(this)
    }

    override val getUdhaarEntriesCount by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetUdhaarEntriesCountQueryImpl(this)
    }

    override val getUser by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetUserQueryImpl(this)
    }

    override val getUsersPaginated by lazy(LazyThreadSafetyMode.PUBLICATION) {
        GetUsersPaginatedQueryImpl(this)
    }

    override val purgeStore by lazy(LazyThreadSafetyMode.PUBLICATION) {
        PurgeStoreMutationImpl(this)
    }

    override val softDeleteExpense by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SoftDeleteExpenseMutationImpl(this)
    }

    override val softDeleteItem by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SoftDeleteItemMutationImpl(this)
    }

    override val softDeleteSale by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SoftDeleteSaleMutationImpl(this)
    }

    override val softDeleteUdhaar by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SoftDeleteUdhaarMutationImpl(this)
    }

    override val syncExpense by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncExpenseMutationImpl(this)
    }

    override val syncExpenses by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncExpensesQueryImpl(this)
    }

    override val syncItem by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncItemMutationImpl(this)
    }

    override val syncItemBatch by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncItemBatchMutationImpl(this)
    }

    override val syncItemBatches by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncItemBatchesQueryImpl(this)
    }

    override val syncItems by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncItemsQueryImpl(this)
    }

    override val syncPurchase by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncPurchaseMutationImpl(this)
    }

    override val syncPurchaseItem by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncPurchaseItemMutationImpl(this)
    }

    override val syncPurchaseItems by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncPurchaseItemsQueryImpl(this)
    }

    override val syncPurchases by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncPurchasesQueryImpl(this)
    }

    override val syncSale by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncSaleMutationImpl(this)
    }

    override val syncSaleItem by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncSaleItemMutationImpl(this)
    }

    override val syncSaleItems by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncSaleItemsQueryImpl(this)
    }

    override val syncSales by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncSalesQueryImpl(this)
    }

    override val syncStore by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncStoreMutationImpl(this)
    }

    override val syncSupplier by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncSupplierMutationImpl(this)
    }

    override val syncSuppliers by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncSuppliersQueryImpl(this)
    }

    override val syncUdhaar by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncUdhaarMutationImpl(this)
    }

    override val syncUdhaars by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncUdhaarsQueryImpl(this)
    }

    override val syncUser by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SyncUserMutationImpl(this)
    }

    override val toggleStoreStatus by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ToggleStoreStatusMutationImpl(this)
    }

    override val updateStore by lazy(LazyThreadSafetyMode.PUBLICATION) {
        UpdateStoreMutationImpl(this)
    }

    override val updateUser by lazy(LazyThreadSafetyMode.PUBLICATION) {
        UpdateUserMutationImpl(this)
    }

    override val upsertAnnouncement by lazy(LazyThreadSafetyMode.PUBLICATION) {
        UpsertAnnouncementMutationImpl(this)
    }

    override val upsertGlobalSetting by lazy(LazyThreadSafetyMode.PUBLICATION) {
        UpsertGlobalSettingMutationImpl(this)
    }

    override val upsertPromoCode by lazy(LazyThreadSafetyMode.PUBLICATION) {
        UpsertPromoCodeMutationImpl(this)
    }

    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun operations(): List<com.google.firebase.dataconnect.generated.GeneratedOperation<StorebookConnectorConnector, *, *>> =
        queries() + mutations()

    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun mutations(): List<com.google.firebase.dataconnect.generated.GeneratedMutation<StorebookConnectorConnector, *, *>> =
        listOf(
            createAdminAuditLog,
            createUser,
            deleteAnnouncement,
            deletePromoCode,
            purgeStore,
            softDeleteExpense,
            softDeleteItem,
            softDeleteSale,
            softDeleteUdhaar,
            syncExpense,
            syncItem,
            syncItemBatch,
            syncPurchase,
            syncPurchaseItem,
            syncSale,
            syncSaleItem,
            syncStore,
            syncSupplier,
            syncUdhaar,
            syncUser,
            toggleStoreStatus,
            updateStore,
            updateUser,
            upsertAnnouncement,
            upsertGlobalSetting,
            upsertPromoCode,
        )

    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun queries(): List<com.google.firebase.dataconnect.generated.GeneratedQuery<StorebookConnectorConnector, *, *>> =
        listOf(
            getActiveExpenses,
            getActiveItems,
            getActiveSaleItems,
            getActiveSales,
            getActiveSuppliers,
            getActiveUdhaars,
            getAdminAuditLogs,
            getAnnouncements,
            getExpenseEntriesCount,
            getGlobalSettings,
            getItemsCount,
            getPromoCodes,
            getSalesCount,
            getStore,
            getStoresPaginated,
            getUdhaarEntriesCount,
            getUser,
            getUsersPaginated,
            syncExpenses,
            syncItemBatches,
            syncItems,
            syncPurchaseItems,
            syncPurchases,
            syncSaleItems,
            syncSales,
            syncSuppliers,
            syncUdhaars,
        )

    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun copy(dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect) =
        StorebookConnectorConnectorImpl(dataConnect)

    override fun equals(other: Any?): Boolean =
        other is StorebookConnectorConnectorImpl &&
            other.dataConnect == dataConnect

    override fun hashCode(): Int =
        java.util.Objects.hash(
            "StorebookConnectorConnectorImpl",
            dataConnect,
        )

    override fun toString(): String = "StorebookConnectorConnectorImpl(dataConnect=$dataConnect)"
}

private open class StorebookConnectorConnectorGeneratedQueryImpl<Data, Variables>(
    override val connector: StorebookConnectorConnector,
    override val operationName: String,
    override val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data>,
    override val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables>,
) : com.google.firebase.dataconnect.generated.GeneratedQuery<StorebookConnectorConnector, Data, Variables> {
    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun copy(
        connector: StorebookConnectorConnector,
        operationName: String,
        dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data>,
        variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables>,
    ) = StorebookConnectorConnectorGeneratedQueryImpl(
        connector, operationName, dataDeserializer, variablesSerializer,
    )

    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun <NewVariables> withVariablesSerializer(
        variablesSerializer: kotlinx.serialization.SerializationStrategy<NewVariables>,
    ) = StorebookConnectorConnectorGeneratedQueryImpl(
        connector, operationName, dataDeserializer, variablesSerializer,
    )

    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun <NewData> withDataDeserializer(
        dataDeserializer: kotlinx.serialization.DeserializationStrategy<NewData>,
    ) = StorebookConnectorConnectorGeneratedQueryImpl(
        connector, operationName, dataDeserializer, variablesSerializer,
    )

    override fun equals(other: Any?): Boolean =
        other is StorebookConnectorConnectorGeneratedQueryImpl<*, *> &&
            other.connector == connector &&
            other.operationName == operationName &&
            other.dataDeserializer == dataDeserializer &&
            other.variablesSerializer == variablesSerializer

    override fun hashCode(): Int =
        java.util.Objects.hash(
            "StorebookConnectorConnectorGeneratedQueryImpl",
            connector, operationName, dataDeserializer, variablesSerializer,
        )

    override fun toString(): String =
        "StorebookConnectorConnectorGeneratedQueryImpl(" +
            "operationName=$operationName, " +
            "dataDeserializer=$dataDeserializer, " +
            "variablesSerializer=$variablesSerializer, " +
            "connector=$connector)"
}

private open class StorebookConnectorConnectorGeneratedMutationImpl<Data, Variables>(
    override val connector: StorebookConnectorConnector,
    override val operationName: String,
    override val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data>,
    override val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables>,
) : com.google.firebase.dataconnect.generated.GeneratedMutation<StorebookConnectorConnector, Data, Variables> {
    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun copy(
        connector: StorebookConnectorConnector,
        operationName: String,
        dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data>,
        variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables>,
    ) = StorebookConnectorConnectorGeneratedMutationImpl(
        connector, operationName, dataDeserializer, variablesSerializer,
    )

    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun <NewVariables> withVariablesSerializer(
        variablesSerializer: kotlinx.serialization.SerializationStrategy<NewVariables>,
    ) = StorebookConnectorConnectorGeneratedMutationImpl(
        connector, operationName, dataDeserializer, variablesSerializer,
    )

    @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
    override fun <NewData> withDataDeserializer(
        dataDeserializer: kotlinx.serialization.DeserializationStrategy<NewData>,
    ) = StorebookConnectorConnectorGeneratedMutationImpl(
        connector, operationName, dataDeserializer, variablesSerializer,
    )

    override fun equals(other: Any?): Boolean =
        other is StorebookConnectorConnectorGeneratedMutationImpl<*, *> &&
            other.connector == connector &&
            other.operationName == operationName &&
            other.dataDeserializer == dataDeserializer &&
            other.variablesSerializer == variablesSerializer

    override fun hashCode(): Int =
        java.util.Objects.hash(
            "StorebookConnectorConnectorGeneratedMutationImpl",
            connector, operationName, dataDeserializer, variablesSerializer,
        )

    override fun toString(): String =
        "StorebookConnectorConnectorGeneratedMutationImpl(" +
            "operationName=$operationName, " +
            "dataDeserializer=$dataDeserializer, " +
            "variablesSerializer=$variablesSerializer, " +
            "connector=$connector)"
}

private class CreateAdminAuditLogMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        CreateAdminAuditLogMutation.Data,
        CreateAdminAuditLogMutation.Variables,
    >(
        connector,
        CreateAdminAuditLogMutation.Companion.operationName,
        CreateAdminAuditLogMutation.Companion.dataDeserializer,
        CreateAdminAuditLogMutation.Companion.variablesSerializer,
    ),
    CreateAdminAuditLogMutation

private class CreateUserMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        CreateUserMutation.Data,
        CreateUserMutation.Variables,
    >(
        connector,
        CreateUserMutation.Companion.operationName,
        CreateUserMutation.Companion.dataDeserializer,
        CreateUserMutation.Companion.variablesSerializer,
    ),
    CreateUserMutation

private class DeleteAnnouncementMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        DeleteAnnouncementMutation.Data,
        DeleteAnnouncementMutation.Variables,
    >(
        connector,
        DeleteAnnouncementMutation.Companion.operationName,
        DeleteAnnouncementMutation.Companion.dataDeserializer,
        DeleteAnnouncementMutation.Companion.variablesSerializer,
    ),
    DeleteAnnouncementMutation

private class DeletePromoCodeMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        DeletePromoCodeMutation.Data,
        DeletePromoCodeMutation.Variables,
    >(
        connector,
        DeletePromoCodeMutation.Companion.operationName,
        DeletePromoCodeMutation.Companion.dataDeserializer,
        DeletePromoCodeMutation.Companion.variablesSerializer,
    ),
    DeletePromoCodeMutation

private class GetActiveExpensesQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetActiveExpensesQuery.Data,
        GetActiveExpensesQuery.Variables,
    >(
        connector,
        GetActiveExpensesQuery.Companion.operationName,
        GetActiveExpensesQuery.Companion.dataDeserializer,
        GetActiveExpensesQuery.Companion.variablesSerializer,
    ),
    GetActiveExpensesQuery

private class GetActiveItemsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetActiveItemsQuery.Data,
        GetActiveItemsQuery.Variables,
    >(
        connector,
        GetActiveItemsQuery.Companion.operationName,
        GetActiveItemsQuery.Companion.dataDeserializer,
        GetActiveItemsQuery.Companion.variablesSerializer,
    ),
    GetActiveItemsQuery

private class GetActiveSaleItemsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetActiveSaleItemsQuery.Data,
        GetActiveSaleItemsQuery.Variables,
    >(
        connector,
        GetActiveSaleItemsQuery.Companion.operationName,
        GetActiveSaleItemsQuery.Companion.dataDeserializer,
        GetActiveSaleItemsQuery.Companion.variablesSerializer,
    ),
    GetActiveSaleItemsQuery

private class GetActiveSalesQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetActiveSalesQuery.Data,
        GetActiveSalesQuery.Variables,
    >(
        connector,
        GetActiveSalesQuery.Companion.operationName,
        GetActiveSalesQuery.Companion.dataDeserializer,
        GetActiveSalesQuery.Companion.variablesSerializer,
    ),
    GetActiveSalesQuery

private class GetActiveSuppliersQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetActiveSuppliersQuery.Data,
        GetActiveSuppliersQuery.Variables,
    >(
        connector,
        GetActiveSuppliersQuery.Companion.operationName,
        GetActiveSuppliersQuery.Companion.dataDeserializer,
        GetActiveSuppliersQuery.Companion.variablesSerializer,
    ),
    GetActiveSuppliersQuery

private class GetActiveUdhaarsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetActiveUdhaarsQuery.Data,
        GetActiveUdhaarsQuery.Variables,
    >(
        connector,
        GetActiveUdhaarsQuery.Companion.operationName,
        GetActiveUdhaarsQuery.Companion.dataDeserializer,
        GetActiveUdhaarsQuery.Companion.variablesSerializer,
    ),
    GetActiveUdhaarsQuery

private class GetAdminAuditLogsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetAdminAuditLogsQuery.Data,
        Unit,
    >(
        connector,
        GetAdminAuditLogsQuery.Companion.operationName,
        GetAdminAuditLogsQuery.Companion.dataDeserializer,
        GetAdminAuditLogsQuery.Companion.variablesSerializer,
    ),
    GetAdminAuditLogsQuery

private class GetAnnouncementsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetAnnouncementsQuery.Data,
        Unit,
    >(
        connector,
        GetAnnouncementsQuery.Companion.operationName,
        GetAnnouncementsQuery.Companion.dataDeserializer,
        GetAnnouncementsQuery.Companion.variablesSerializer,
    ),
    GetAnnouncementsQuery

private class GetExpenseEntriesCountQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetExpenseEntriesCountQuery.Data,
        GetExpenseEntriesCountQuery.Variables,
    >(
        connector,
        GetExpenseEntriesCountQuery.Companion.operationName,
        GetExpenseEntriesCountQuery.Companion.dataDeserializer,
        GetExpenseEntriesCountQuery.Companion.variablesSerializer,
    ),
    GetExpenseEntriesCountQuery

private class GetGlobalSettingsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetGlobalSettingsQuery.Data,
        Unit,
    >(
        connector,
        GetGlobalSettingsQuery.Companion.operationName,
        GetGlobalSettingsQuery.Companion.dataDeserializer,
        GetGlobalSettingsQuery.Companion.variablesSerializer,
    ),
    GetGlobalSettingsQuery

private class GetItemsCountQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetItemsCountQuery.Data,
        GetItemsCountQuery.Variables,
    >(
        connector,
        GetItemsCountQuery.Companion.operationName,
        GetItemsCountQuery.Companion.dataDeserializer,
        GetItemsCountQuery.Companion.variablesSerializer,
    ),
    GetItemsCountQuery

private class GetPromoCodesQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetPromoCodesQuery.Data,
        Unit,
    >(
        connector,
        GetPromoCodesQuery.Companion.operationName,
        GetPromoCodesQuery.Companion.dataDeserializer,
        GetPromoCodesQuery.Companion.variablesSerializer,
    ),
    GetPromoCodesQuery

private class GetSalesCountQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetSalesCountQuery.Data,
        GetSalesCountQuery.Variables,
    >(
        connector,
        GetSalesCountQuery.Companion.operationName,
        GetSalesCountQuery.Companion.dataDeserializer,
        GetSalesCountQuery.Companion.variablesSerializer,
    ),
    GetSalesCountQuery

private class GetStoreQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetStoreQuery.Data,
        GetStoreQuery.Variables,
    >(
        connector,
        GetStoreQuery.Companion.operationName,
        GetStoreQuery.Companion.dataDeserializer,
        GetStoreQuery.Companion.variablesSerializer,
    ),
    GetStoreQuery

private class GetStoresPaginatedQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetStoresPaginatedQuery.Data,
        Unit,
    >(
        connector,
        GetStoresPaginatedQuery.Companion.operationName,
        GetStoresPaginatedQuery.Companion.dataDeserializer,
        GetStoresPaginatedQuery.Companion.variablesSerializer,
    ),
    GetStoresPaginatedQuery

private class GetUdhaarEntriesCountQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetUdhaarEntriesCountQuery.Data,
        GetUdhaarEntriesCountQuery.Variables,
    >(
        connector,
        GetUdhaarEntriesCountQuery.Companion.operationName,
        GetUdhaarEntriesCountQuery.Companion.dataDeserializer,
        GetUdhaarEntriesCountQuery.Companion.variablesSerializer,
    ),
    GetUdhaarEntriesCountQuery

private class GetUserQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetUserQuery.Data,
        GetUserQuery.Variables,
    >(
        connector,
        GetUserQuery.Companion.operationName,
        GetUserQuery.Companion.dataDeserializer,
        GetUserQuery.Companion.variablesSerializer,
    ),
    GetUserQuery

private class GetUsersPaginatedQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        GetUsersPaginatedQuery.Data,
        Unit,
    >(
        connector,
        GetUsersPaginatedQuery.Companion.operationName,
        GetUsersPaginatedQuery.Companion.dataDeserializer,
        GetUsersPaginatedQuery.Companion.variablesSerializer,
    ),
    GetUsersPaginatedQuery

private class PurgeStoreMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        PurgeStoreMutation.Data,
        PurgeStoreMutation.Variables,
    >(
        connector,
        PurgeStoreMutation.Companion.operationName,
        PurgeStoreMutation.Companion.dataDeserializer,
        PurgeStoreMutation.Companion.variablesSerializer,
    ),
    PurgeStoreMutation

private class SoftDeleteExpenseMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SoftDeleteExpenseMutation.Data,
        SoftDeleteExpenseMutation.Variables,
    >(
        connector,
        SoftDeleteExpenseMutation.Companion.operationName,
        SoftDeleteExpenseMutation.Companion.dataDeserializer,
        SoftDeleteExpenseMutation.Companion.variablesSerializer,
    ),
    SoftDeleteExpenseMutation

private class SoftDeleteItemMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SoftDeleteItemMutation.Data,
        SoftDeleteItemMutation.Variables,
    >(
        connector,
        SoftDeleteItemMutation.Companion.operationName,
        SoftDeleteItemMutation.Companion.dataDeserializer,
        SoftDeleteItemMutation.Companion.variablesSerializer,
    ),
    SoftDeleteItemMutation

private class SoftDeleteSaleMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SoftDeleteSaleMutation.Data,
        SoftDeleteSaleMutation.Variables,
    >(
        connector,
        SoftDeleteSaleMutation.Companion.operationName,
        SoftDeleteSaleMutation.Companion.dataDeserializer,
        SoftDeleteSaleMutation.Companion.variablesSerializer,
    ),
    SoftDeleteSaleMutation

private class SoftDeleteUdhaarMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SoftDeleteUdhaarMutation.Data,
        SoftDeleteUdhaarMutation.Variables,
    >(
        connector,
        SoftDeleteUdhaarMutation.Companion.operationName,
        SoftDeleteUdhaarMutation.Companion.dataDeserializer,
        SoftDeleteUdhaarMutation.Companion.variablesSerializer,
    ),
    SoftDeleteUdhaarMutation

private class SyncExpenseMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncExpenseMutation.Data,
        SyncExpenseMutation.Variables,
    >(
        connector,
        SyncExpenseMutation.Companion.operationName,
        SyncExpenseMutation.Companion.dataDeserializer,
        SyncExpenseMutation.Companion.variablesSerializer,
    ),
    SyncExpenseMutation

private class SyncExpensesQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncExpensesQuery.Data,
        SyncExpensesQuery.Variables,
    >(
        connector,
        SyncExpensesQuery.Companion.operationName,
        SyncExpensesQuery.Companion.dataDeserializer,
        SyncExpensesQuery.Companion.variablesSerializer,
    ),
    SyncExpensesQuery

private class SyncItemMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncItemMutation.Data,
        SyncItemMutation.Variables,
    >(
        connector,
        SyncItemMutation.Companion.operationName,
        SyncItemMutation.Companion.dataDeserializer,
        SyncItemMutation.Companion.variablesSerializer,
    ),
    SyncItemMutation

private class SyncItemBatchMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncItemBatchMutation.Data,
        SyncItemBatchMutation.Variables,
    >(
        connector,
        SyncItemBatchMutation.Companion.operationName,
        SyncItemBatchMutation.Companion.dataDeserializer,
        SyncItemBatchMutation.Companion.variablesSerializer,
    ),
    SyncItemBatchMutation

private class SyncItemBatchesQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncItemBatchesQuery.Data,
        SyncItemBatchesQuery.Variables,
    >(
        connector,
        SyncItemBatchesQuery.Companion.operationName,
        SyncItemBatchesQuery.Companion.dataDeserializer,
        SyncItemBatchesQuery.Companion.variablesSerializer,
    ),
    SyncItemBatchesQuery

private class SyncItemsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncItemsQuery.Data,
        SyncItemsQuery.Variables,
    >(
        connector,
        SyncItemsQuery.Companion.operationName,
        SyncItemsQuery.Companion.dataDeserializer,
        SyncItemsQuery.Companion.variablesSerializer,
    ),
    SyncItemsQuery

private class SyncPurchaseMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncPurchaseMutation.Data,
        SyncPurchaseMutation.Variables,
    >(
        connector,
        SyncPurchaseMutation.Companion.operationName,
        SyncPurchaseMutation.Companion.dataDeserializer,
        SyncPurchaseMutation.Companion.variablesSerializer,
    ),
    SyncPurchaseMutation

private class SyncPurchaseItemMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncPurchaseItemMutation.Data,
        SyncPurchaseItemMutation.Variables,
    >(
        connector,
        SyncPurchaseItemMutation.Companion.operationName,
        SyncPurchaseItemMutation.Companion.dataDeserializer,
        SyncPurchaseItemMutation.Companion.variablesSerializer,
    ),
    SyncPurchaseItemMutation

private class SyncPurchaseItemsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncPurchaseItemsQuery.Data,
        SyncPurchaseItemsQuery.Variables,
    >(
        connector,
        SyncPurchaseItemsQuery.Companion.operationName,
        SyncPurchaseItemsQuery.Companion.dataDeserializer,
        SyncPurchaseItemsQuery.Companion.variablesSerializer,
    ),
    SyncPurchaseItemsQuery

private class SyncPurchasesQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncPurchasesQuery.Data,
        SyncPurchasesQuery.Variables,
    >(
        connector,
        SyncPurchasesQuery.Companion.operationName,
        SyncPurchasesQuery.Companion.dataDeserializer,
        SyncPurchasesQuery.Companion.variablesSerializer,
    ),
    SyncPurchasesQuery

private class SyncSaleMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncSaleMutation.Data,
        SyncSaleMutation.Variables,
    >(
        connector,
        SyncSaleMutation.Companion.operationName,
        SyncSaleMutation.Companion.dataDeserializer,
        SyncSaleMutation.Companion.variablesSerializer,
    ),
    SyncSaleMutation

private class SyncSaleItemMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncSaleItemMutation.Data,
        SyncSaleItemMutation.Variables,
    >(
        connector,
        SyncSaleItemMutation.Companion.operationName,
        SyncSaleItemMutation.Companion.dataDeserializer,
        SyncSaleItemMutation.Companion.variablesSerializer,
    ),
    SyncSaleItemMutation

private class SyncSaleItemsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncSaleItemsQuery.Data,
        SyncSaleItemsQuery.Variables,
    >(
        connector,
        SyncSaleItemsQuery.Companion.operationName,
        SyncSaleItemsQuery.Companion.dataDeserializer,
        SyncSaleItemsQuery.Companion.variablesSerializer,
    ),
    SyncSaleItemsQuery

private class SyncSalesQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncSalesQuery.Data,
        SyncSalesQuery.Variables,
    >(
        connector,
        SyncSalesQuery.Companion.operationName,
        SyncSalesQuery.Companion.dataDeserializer,
        SyncSalesQuery.Companion.variablesSerializer,
    ),
    SyncSalesQuery

private class SyncStoreMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncStoreMutation.Data,
        SyncStoreMutation.Variables,
    >(
        connector,
        SyncStoreMutation.Companion.operationName,
        SyncStoreMutation.Companion.dataDeserializer,
        SyncStoreMutation.Companion.variablesSerializer,
    ),
    SyncStoreMutation

private class SyncSupplierMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncSupplierMutation.Data,
        SyncSupplierMutation.Variables,
    >(
        connector,
        SyncSupplierMutation.Companion.operationName,
        SyncSupplierMutation.Companion.dataDeserializer,
        SyncSupplierMutation.Companion.variablesSerializer,
    ),
    SyncSupplierMutation

private class SyncSuppliersQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncSuppliersQuery.Data,
        SyncSuppliersQuery.Variables,
    >(
        connector,
        SyncSuppliersQuery.Companion.operationName,
        SyncSuppliersQuery.Companion.dataDeserializer,
        SyncSuppliersQuery.Companion.variablesSerializer,
    ),
    SyncSuppliersQuery

private class SyncUdhaarMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncUdhaarMutation.Data,
        SyncUdhaarMutation.Variables,
    >(
        connector,
        SyncUdhaarMutation.Companion.operationName,
        SyncUdhaarMutation.Companion.dataDeserializer,
        SyncUdhaarMutation.Companion.variablesSerializer,
    ),
    SyncUdhaarMutation

private class SyncUdhaarsQueryImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedQueryImpl<
        SyncUdhaarsQuery.Data,
        SyncUdhaarsQuery.Variables,
    >(
        connector,
        SyncUdhaarsQuery.Companion.operationName,
        SyncUdhaarsQuery.Companion.dataDeserializer,
        SyncUdhaarsQuery.Companion.variablesSerializer,
    ),
    SyncUdhaarsQuery

private class SyncUserMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        SyncUserMutation.Data,
        SyncUserMutation.Variables,
    >(
        connector,
        SyncUserMutation.Companion.operationName,
        SyncUserMutation.Companion.dataDeserializer,
        SyncUserMutation.Companion.variablesSerializer,
    ),
    SyncUserMutation

private class ToggleStoreStatusMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        ToggleStoreStatusMutation.Data,
        ToggleStoreStatusMutation.Variables,
    >(
        connector,
        ToggleStoreStatusMutation.Companion.operationName,
        ToggleStoreStatusMutation.Companion.dataDeserializer,
        ToggleStoreStatusMutation.Companion.variablesSerializer,
    ),
    ToggleStoreStatusMutation

private class UpdateStoreMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        UpdateStoreMutation.Data,
        UpdateStoreMutation.Variables,
    >(
        connector,
        UpdateStoreMutation.Companion.operationName,
        UpdateStoreMutation.Companion.dataDeserializer,
        UpdateStoreMutation.Companion.variablesSerializer,
    ),
    UpdateStoreMutation

private class UpdateUserMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        UpdateUserMutation.Data,
        UpdateUserMutation.Variables,
    >(
        connector,
        UpdateUserMutation.Companion.operationName,
        UpdateUserMutation.Companion.dataDeserializer,
        UpdateUserMutation.Companion.variablesSerializer,
    ),
    UpdateUserMutation

private class UpsertAnnouncementMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        UpsertAnnouncementMutation.Data,
        UpsertAnnouncementMutation.Variables,
    >(
        connector,
        UpsertAnnouncementMutation.Companion.operationName,
        UpsertAnnouncementMutation.Companion.dataDeserializer,
        UpsertAnnouncementMutation.Companion.variablesSerializer,
    ),
    UpsertAnnouncementMutation

private class UpsertGlobalSettingMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        UpsertGlobalSettingMutation.Data,
        UpsertGlobalSettingMutation.Variables,
    >(
        connector,
        UpsertGlobalSettingMutation.Companion.operationName,
        UpsertGlobalSettingMutation.Companion.dataDeserializer,
        UpsertGlobalSettingMutation.Companion.variablesSerializer,
    ),
    UpsertGlobalSettingMutation

private class UpsertPromoCodeMutationImpl(
    connector: StorebookConnectorConnector,
) : StorebookConnectorConnectorGeneratedMutationImpl<
        UpsertPromoCodeMutation.Data,
        UpsertPromoCodeMutation.Variables,
    >(
        connector,
        UpsertPromoCodeMutation.Companion.operationName,
        UpsertPromoCodeMutation.Companion.dataDeserializer,
        UpsertPromoCodeMutation.Companion.variablesSerializer,
    ),
    UpsertPromoCodeMutation

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
