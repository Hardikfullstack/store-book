
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

public interface StorebookConnectorConnector : com.google.firebase.dataconnect.generated.GeneratedConnector<StorebookConnectorConnector> {
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
  
    public val getGlobalSettings: GetGlobalSettingsQuery
  
    public val getItemsCount: GetItemsCountQuery
  
    public val getPromoCodes: GetPromoCodesQuery
  
    public val getStoresPaginated: GetStoresPaginatedQuery
  
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
  
    public val syncItems: SyncItemsQuery
  
    public val syncSale: SyncSaleMutation
  
    public val syncSaleItem: SyncSaleItemMutation
  
    public val syncSaleItems: SyncSaleItemsQuery
  
    public val syncSales: SyncSalesQuery
  
    public val syncStore: SyncStoreMutation
  
    public val syncSupplier: SyncSupplierMutation
  
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
    public val config: com.google.firebase.dataconnect.ConnectorConfig = com.google.firebase.dataconnect.ConnectorConfig(
      connector = "storebook-connector",
      location = "us-central1",
      serviceId = "store-book",
    )

    public fun getInstance(
      dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect
    ):StorebookConnectorConnector = synchronized(instances) {
      instances.getOrPut(dataConnect) {
        StorebookConnectorConnectorImpl(dataConnect)
      }
    }

    private val instances = java.util.WeakHashMap<com.google.firebase.dataconnect.FirebaseDataConnect, StorebookConnectorConnectorImpl>()
  }
}

public val StorebookConnectorConnector.Companion.instance:StorebookConnectorConnector
  get() = getInstance(com.google.firebase.dataconnect.FirebaseDataConnect._fdcGetInstance(config))

public fun StorebookConnectorConnector.Companion.getInstance(
  settings: com.google.firebase.dataconnect.DataConnectSettings = com.google.firebase.dataconnect.DataConnectSettings()
):StorebookConnectorConnector =
  getInstance(com.google.firebase.dataconnect.FirebaseDataConnect._fdcGetInstance(config, settings))

public fun StorebookConnectorConnector.Companion.getInstance(
  app: com.google.firebase.FirebaseApp,
  settings: com.google.firebase.dataconnect.DataConnectSettings = com.google.firebase.dataconnect.DataConnectSettings()
):StorebookConnectorConnector =
  getInstance(com.google.firebase.dataconnect.FirebaseDataConnect._fdcGetInstance(app, config, settings))

private class StorebookConnectorConnectorImpl(
  override val dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect
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
  
    override val getGlobalSettings by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetGlobalSettingsQueryImpl(this)
    }
  
    override val getItemsCount by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetItemsCountQueryImpl(this)
    }
  
    override val getPromoCodes by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetPromoCodesQueryImpl(this)
    }
  
    override val getStoresPaginated by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetStoresPaginatedQueryImpl(this)
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
  
    override val syncItems by lazy(LazyThreadSafetyMode.PUBLICATION) {
      SyncItemsQueryImpl(this)
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
        getGlobalSettings,
        getItemsCount,
        getPromoCodes,
        getStoresPaginated,
        getUser,
        getUsersPaginated,
        syncExpenses,
        syncItems,
        syncSaleItems,
        syncSales,
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

  override fun toString(): String =
    "StorebookConnectorConnectorImpl(dataConnect=$dataConnect)"
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
  ) =
    StorebookConnectorConnectorGeneratedQueryImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun <NewVariables> withVariablesSerializer(
    variablesSerializer: kotlinx.serialization.SerializationStrategy<NewVariables>
  ) =
    StorebookConnectorConnectorGeneratedQueryImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun <NewData> withDataDeserializer(
    dataDeserializer: kotlinx.serialization.DeserializationStrategy<NewData>
  ) =
    StorebookConnectorConnectorGeneratedQueryImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun equals(other: Any?): Boolean =
    other is StorebookConnectorConnectorGeneratedQueryImpl<*,*> &&
    other.connector == connector &&
    other.operationName == operationName &&
    other.dataDeserializer == dataDeserializer &&
    other.variablesSerializer == variablesSerializer

  override fun hashCode(): Int =
    java.util.Objects.hash(
      "StorebookConnectorConnectorGeneratedQueryImpl",
      connector, operationName, dataDeserializer, variablesSerializer
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
  ) =
    StorebookConnectorConnectorGeneratedMutationImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun <NewVariables> withVariablesSerializer(
    variablesSerializer: kotlinx.serialization.SerializationStrategy<NewVariables>
  ) =
    StorebookConnectorConnectorGeneratedMutationImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun <NewData> withDataDeserializer(
    dataDeserializer: kotlinx.serialization.DeserializationStrategy<NewData>
  ) =
    StorebookConnectorConnectorGeneratedMutationImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun equals(other: Any?): Boolean =
    other is StorebookConnectorConnectorGeneratedMutationImpl<*,*> &&
    other.connector == connector &&
    other.operationName == operationName &&
    other.dataDeserializer == dataDeserializer &&
    other.variablesSerializer == variablesSerializer

  override fun hashCode(): Int =
    java.util.Objects.hash(
      "StorebookConnectorConnectorGeneratedMutationImpl",
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun toString(): String =
    "StorebookConnectorConnectorGeneratedMutationImpl(" +
    "operationName=$operationName, " +
    "dataDeserializer=$dataDeserializer, " +
    "variablesSerializer=$variablesSerializer, " +
    "connector=$connector)"
}



private class CreateAdminAuditLogMutationImpl(
  connector: StorebookConnectorConnector
):
  CreateAdminAuditLogMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      CreateAdminAuditLogMutation.Data,
      CreateAdminAuditLogMutation.Variables
  >(
    connector,
    CreateAdminAuditLogMutation.Companion.operationName,
    CreateAdminAuditLogMutation.Companion.dataDeserializer,
    CreateAdminAuditLogMutation.Companion.variablesSerializer,
  )


private class CreateUserMutationImpl(
  connector: StorebookConnectorConnector
):
  CreateUserMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      CreateUserMutation.Data,
      CreateUserMutation.Variables
  >(
    connector,
    CreateUserMutation.Companion.operationName,
    CreateUserMutation.Companion.dataDeserializer,
    CreateUserMutation.Companion.variablesSerializer,
  )


private class DeleteAnnouncementMutationImpl(
  connector: StorebookConnectorConnector
):
  DeleteAnnouncementMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      DeleteAnnouncementMutation.Data,
      DeleteAnnouncementMutation.Variables
  >(
    connector,
    DeleteAnnouncementMutation.Companion.operationName,
    DeleteAnnouncementMutation.Companion.dataDeserializer,
    DeleteAnnouncementMutation.Companion.variablesSerializer,
  )


private class DeletePromoCodeMutationImpl(
  connector: StorebookConnectorConnector
):
  DeletePromoCodeMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      DeletePromoCodeMutation.Data,
      DeletePromoCodeMutation.Variables
  >(
    connector,
    DeletePromoCodeMutation.Companion.operationName,
    DeletePromoCodeMutation.Companion.dataDeserializer,
    DeletePromoCodeMutation.Companion.variablesSerializer,
  )


private class GetActiveExpensesQueryImpl(
  connector: StorebookConnectorConnector
):
  GetActiveExpensesQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetActiveExpensesQuery.Data,
      GetActiveExpensesQuery.Variables
  >(
    connector,
    GetActiveExpensesQuery.Companion.operationName,
    GetActiveExpensesQuery.Companion.dataDeserializer,
    GetActiveExpensesQuery.Companion.variablesSerializer,
  )


private class GetActiveItemsQueryImpl(
  connector: StorebookConnectorConnector
):
  GetActiveItemsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetActiveItemsQuery.Data,
      GetActiveItemsQuery.Variables
  >(
    connector,
    GetActiveItemsQuery.Companion.operationName,
    GetActiveItemsQuery.Companion.dataDeserializer,
    GetActiveItemsQuery.Companion.variablesSerializer,
  )


private class GetActiveSaleItemsQueryImpl(
  connector: StorebookConnectorConnector
):
  GetActiveSaleItemsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetActiveSaleItemsQuery.Data,
      GetActiveSaleItemsQuery.Variables
  >(
    connector,
    GetActiveSaleItemsQuery.Companion.operationName,
    GetActiveSaleItemsQuery.Companion.dataDeserializer,
    GetActiveSaleItemsQuery.Companion.variablesSerializer,
  )


private class GetActiveSalesQueryImpl(
  connector: StorebookConnectorConnector
):
  GetActiveSalesQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetActiveSalesQuery.Data,
      GetActiveSalesQuery.Variables
  >(
    connector,
    GetActiveSalesQuery.Companion.operationName,
    GetActiveSalesQuery.Companion.dataDeserializer,
    GetActiveSalesQuery.Companion.variablesSerializer,
  )


private class GetActiveSuppliersQueryImpl(
  connector: StorebookConnectorConnector
):
  GetActiveSuppliersQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetActiveSuppliersQuery.Data,
      GetActiveSuppliersQuery.Variables
  >(
    connector,
    GetActiveSuppliersQuery.Companion.operationName,
    GetActiveSuppliersQuery.Companion.dataDeserializer,
    GetActiveSuppliersQuery.Companion.variablesSerializer,
  )


private class GetActiveUdhaarsQueryImpl(
  connector: StorebookConnectorConnector
):
  GetActiveUdhaarsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetActiveUdhaarsQuery.Data,
      GetActiveUdhaarsQuery.Variables
  >(
    connector,
    GetActiveUdhaarsQuery.Companion.operationName,
    GetActiveUdhaarsQuery.Companion.dataDeserializer,
    GetActiveUdhaarsQuery.Companion.variablesSerializer,
  )


private class GetAdminAuditLogsQueryImpl(
  connector: StorebookConnectorConnector
):
  GetAdminAuditLogsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetAdminAuditLogsQuery.Data,
      Unit
  >(
    connector,
    GetAdminAuditLogsQuery.Companion.operationName,
    GetAdminAuditLogsQuery.Companion.dataDeserializer,
    GetAdminAuditLogsQuery.Companion.variablesSerializer,
  )


private class GetAnnouncementsQueryImpl(
  connector: StorebookConnectorConnector
):
  GetAnnouncementsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetAnnouncementsQuery.Data,
      Unit
  >(
    connector,
    GetAnnouncementsQuery.Companion.operationName,
    GetAnnouncementsQuery.Companion.dataDeserializer,
    GetAnnouncementsQuery.Companion.variablesSerializer,
  )


private class GetGlobalSettingsQueryImpl(
  connector: StorebookConnectorConnector
):
  GetGlobalSettingsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetGlobalSettingsQuery.Data,
      Unit
  >(
    connector,
    GetGlobalSettingsQuery.Companion.operationName,
    GetGlobalSettingsQuery.Companion.dataDeserializer,
    GetGlobalSettingsQuery.Companion.variablesSerializer,
  )


private class GetItemsCountQueryImpl(
  connector: StorebookConnectorConnector
):
  GetItemsCountQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetItemsCountQuery.Data,
      GetItemsCountQuery.Variables
  >(
    connector,
    GetItemsCountQuery.Companion.operationName,
    GetItemsCountQuery.Companion.dataDeserializer,
    GetItemsCountQuery.Companion.variablesSerializer,
  )


private class GetPromoCodesQueryImpl(
  connector: StorebookConnectorConnector
):
  GetPromoCodesQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetPromoCodesQuery.Data,
      Unit
  >(
    connector,
    GetPromoCodesQuery.Companion.operationName,
    GetPromoCodesQuery.Companion.dataDeserializer,
    GetPromoCodesQuery.Companion.variablesSerializer,
  )


private class GetStoresPaginatedQueryImpl(
  connector: StorebookConnectorConnector
):
  GetStoresPaginatedQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetStoresPaginatedQuery.Data,
      Unit
  >(
    connector,
    GetStoresPaginatedQuery.Companion.operationName,
    GetStoresPaginatedQuery.Companion.dataDeserializer,
    GetStoresPaginatedQuery.Companion.variablesSerializer,
  )


private class GetUserQueryImpl(
  connector: StorebookConnectorConnector
):
  GetUserQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetUserQuery.Data,
      GetUserQuery.Variables
  >(
    connector,
    GetUserQuery.Companion.operationName,
    GetUserQuery.Companion.dataDeserializer,
    GetUserQuery.Companion.variablesSerializer,
  )


private class GetUsersPaginatedQueryImpl(
  connector: StorebookConnectorConnector
):
  GetUsersPaginatedQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetUsersPaginatedQuery.Data,
      Unit
  >(
    connector,
    GetUsersPaginatedQuery.Companion.operationName,
    GetUsersPaginatedQuery.Companion.dataDeserializer,
    GetUsersPaginatedQuery.Companion.variablesSerializer,
  )


private class PurgeStoreMutationImpl(
  connector: StorebookConnectorConnector
):
  PurgeStoreMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      PurgeStoreMutation.Data,
      PurgeStoreMutation.Variables
  >(
    connector,
    PurgeStoreMutation.Companion.operationName,
    PurgeStoreMutation.Companion.dataDeserializer,
    PurgeStoreMutation.Companion.variablesSerializer,
  )


private class SoftDeleteExpenseMutationImpl(
  connector: StorebookConnectorConnector
):
  SoftDeleteExpenseMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SoftDeleteExpenseMutation.Data,
      SoftDeleteExpenseMutation.Variables
  >(
    connector,
    SoftDeleteExpenseMutation.Companion.operationName,
    SoftDeleteExpenseMutation.Companion.dataDeserializer,
    SoftDeleteExpenseMutation.Companion.variablesSerializer,
  )


private class SoftDeleteItemMutationImpl(
  connector: StorebookConnectorConnector
):
  SoftDeleteItemMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SoftDeleteItemMutation.Data,
      SoftDeleteItemMutation.Variables
  >(
    connector,
    SoftDeleteItemMutation.Companion.operationName,
    SoftDeleteItemMutation.Companion.dataDeserializer,
    SoftDeleteItemMutation.Companion.variablesSerializer,
  )


private class SoftDeleteSaleMutationImpl(
  connector: StorebookConnectorConnector
):
  SoftDeleteSaleMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SoftDeleteSaleMutation.Data,
      SoftDeleteSaleMutation.Variables
  >(
    connector,
    SoftDeleteSaleMutation.Companion.operationName,
    SoftDeleteSaleMutation.Companion.dataDeserializer,
    SoftDeleteSaleMutation.Companion.variablesSerializer,
  )


private class SoftDeleteUdhaarMutationImpl(
  connector: StorebookConnectorConnector
):
  SoftDeleteUdhaarMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SoftDeleteUdhaarMutation.Data,
      SoftDeleteUdhaarMutation.Variables
  >(
    connector,
    SoftDeleteUdhaarMutation.Companion.operationName,
    SoftDeleteUdhaarMutation.Companion.dataDeserializer,
    SoftDeleteUdhaarMutation.Companion.variablesSerializer,
  )


private class SyncExpenseMutationImpl(
  connector: StorebookConnectorConnector
):
  SyncExpenseMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SyncExpenseMutation.Data,
      SyncExpenseMutation.Variables
  >(
    connector,
    SyncExpenseMutation.Companion.operationName,
    SyncExpenseMutation.Companion.dataDeserializer,
    SyncExpenseMutation.Companion.variablesSerializer,
  )


private class SyncExpensesQueryImpl(
  connector: StorebookConnectorConnector
):
  SyncExpensesQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      SyncExpensesQuery.Data,
      SyncExpensesQuery.Variables
  >(
    connector,
    SyncExpensesQuery.Companion.operationName,
    SyncExpensesQuery.Companion.dataDeserializer,
    SyncExpensesQuery.Companion.variablesSerializer,
  )


private class SyncItemMutationImpl(
  connector: StorebookConnectorConnector
):
  SyncItemMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SyncItemMutation.Data,
      SyncItemMutation.Variables
  >(
    connector,
    SyncItemMutation.Companion.operationName,
    SyncItemMutation.Companion.dataDeserializer,
    SyncItemMutation.Companion.variablesSerializer,
  )


private class SyncItemsQueryImpl(
  connector: StorebookConnectorConnector
):
  SyncItemsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      SyncItemsQuery.Data,
      SyncItemsQuery.Variables
  >(
    connector,
    SyncItemsQuery.Companion.operationName,
    SyncItemsQuery.Companion.dataDeserializer,
    SyncItemsQuery.Companion.variablesSerializer,
  )


private class SyncSaleMutationImpl(
  connector: StorebookConnectorConnector
):
  SyncSaleMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SyncSaleMutation.Data,
      SyncSaleMutation.Variables
  >(
    connector,
    SyncSaleMutation.Companion.operationName,
    SyncSaleMutation.Companion.dataDeserializer,
    SyncSaleMutation.Companion.variablesSerializer,
  )


private class SyncSaleItemMutationImpl(
  connector: StorebookConnectorConnector
):
  SyncSaleItemMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SyncSaleItemMutation.Data,
      SyncSaleItemMutation.Variables
  >(
    connector,
    SyncSaleItemMutation.Companion.operationName,
    SyncSaleItemMutation.Companion.dataDeserializer,
    SyncSaleItemMutation.Companion.variablesSerializer,
  )


private class SyncSaleItemsQueryImpl(
  connector: StorebookConnectorConnector
):
  SyncSaleItemsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      SyncSaleItemsQuery.Data,
      SyncSaleItemsQuery.Variables
  >(
    connector,
    SyncSaleItemsQuery.Companion.operationName,
    SyncSaleItemsQuery.Companion.dataDeserializer,
    SyncSaleItemsQuery.Companion.variablesSerializer,
  )


private class SyncSalesQueryImpl(
  connector: StorebookConnectorConnector
):
  SyncSalesQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      SyncSalesQuery.Data,
      SyncSalesQuery.Variables
  >(
    connector,
    SyncSalesQuery.Companion.operationName,
    SyncSalesQuery.Companion.dataDeserializer,
    SyncSalesQuery.Companion.variablesSerializer,
  )


private class SyncStoreMutationImpl(
  connector: StorebookConnectorConnector
):
  SyncStoreMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SyncStoreMutation.Data,
      SyncStoreMutation.Variables
  >(
    connector,
    SyncStoreMutation.Companion.operationName,
    SyncStoreMutation.Companion.dataDeserializer,
    SyncStoreMutation.Companion.variablesSerializer,
  )


private class SyncSupplierMutationImpl(
  connector: StorebookConnectorConnector
):
  SyncSupplierMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SyncSupplierMutation.Data,
      SyncSupplierMutation.Variables
  >(
    connector,
    SyncSupplierMutation.Companion.operationName,
    SyncSupplierMutation.Companion.dataDeserializer,
    SyncSupplierMutation.Companion.variablesSerializer,
  )


private class SyncUdhaarMutationImpl(
  connector: StorebookConnectorConnector
):
  SyncUdhaarMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SyncUdhaarMutation.Data,
      SyncUdhaarMutation.Variables
  >(
    connector,
    SyncUdhaarMutation.Companion.operationName,
    SyncUdhaarMutation.Companion.dataDeserializer,
    SyncUdhaarMutation.Companion.variablesSerializer,
  )


private class SyncUdhaarsQueryImpl(
  connector: StorebookConnectorConnector
):
  SyncUdhaarsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      SyncUdhaarsQuery.Data,
      SyncUdhaarsQuery.Variables
  >(
    connector,
    SyncUdhaarsQuery.Companion.operationName,
    SyncUdhaarsQuery.Companion.dataDeserializer,
    SyncUdhaarsQuery.Companion.variablesSerializer,
  )


private class SyncUserMutationImpl(
  connector: StorebookConnectorConnector
):
  SyncUserMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      SyncUserMutation.Data,
      SyncUserMutation.Variables
  >(
    connector,
    SyncUserMutation.Companion.operationName,
    SyncUserMutation.Companion.dataDeserializer,
    SyncUserMutation.Companion.variablesSerializer,
  )


private class ToggleStoreStatusMutationImpl(
  connector: StorebookConnectorConnector
):
  ToggleStoreStatusMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      ToggleStoreStatusMutation.Data,
      ToggleStoreStatusMutation.Variables
  >(
    connector,
    ToggleStoreStatusMutation.Companion.operationName,
    ToggleStoreStatusMutation.Companion.dataDeserializer,
    ToggleStoreStatusMutation.Companion.variablesSerializer,
  )


private class UpdateStoreMutationImpl(
  connector: StorebookConnectorConnector
):
  UpdateStoreMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      UpdateStoreMutation.Data,
      UpdateStoreMutation.Variables
  >(
    connector,
    UpdateStoreMutation.Companion.operationName,
    UpdateStoreMutation.Companion.dataDeserializer,
    UpdateStoreMutation.Companion.variablesSerializer,
  )


private class UpdateUserMutationImpl(
  connector: StorebookConnectorConnector
):
  UpdateUserMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      UpdateUserMutation.Data,
      UpdateUserMutation.Variables
  >(
    connector,
    UpdateUserMutation.Companion.operationName,
    UpdateUserMutation.Companion.dataDeserializer,
    UpdateUserMutation.Companion.variablesSerializer,
  )


private class UpsertAnnouncementMutationImpl(
  connector: StorebookConnectorConnector
):
  UpsertAnnouncementMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      UpsertAnnouncementMutation.Data,
      UpsertAnnouncementMutation.Variables
  >(
    connector,
    UpsertAnnouncementMutation.Companion.operationName,
    UpsertAnnouncementMutation.Companion.dataDeserializer,
    UpsertAnnouncementMutation.Companion.variablesSerializer,
  )


private class UpsertGlobalSettingMutationImpl(
  connector: StorebookConnectorConnector
):
  UpsertGlobalSettingMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      UpsertGlobalSettingMutation.Data,
      UpsertGlobalSettingMutation.Variables
  >(
    connector,
    UpsertGlobalSettingMutation.Companion.operationName,
    UpsertGlobalSettingMutation.Companion.dataDeserializer,
    UpsertGlobalSettingMutation.Companion.variablesSerializer,
  )


private class UpsertPromoCodeMutationImpl(
  connector: StorebookConnectorConnector
):
  UpsertPromoCodeMutation,
  StorebookConnectorConnectorGeneratedMutationImpl<
      UpsertPromoCodeMutation.Data,
      UpsertPromoCodeMutation.Variables
  >(
    connector,
    UpsertPromoCodeMutation.Companion.operationName,
    UpsertPromoCodeMutation.Companion.dataDeserializer,
    UpsertPromoCodeMutation.Companion.variablesSerializer,
  )



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
