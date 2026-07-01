
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

  
    public val getActiveExpenses: GetActiveExpensesQuery
  
    public val getActiveItems: GetActiveItemsQuery
  
    public val getActiveSales: GetActiveSalesQuery
  
    public val getActiveUdhaars: GetActiveUdhaarsQuery
  
    public val getUser: GetUserQuery
  
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
  
    public val syncUdhaar: SyncUdhaarMutation
  
    public val syncUdhaars: SyncUdhaarsQuery
  
    public val syncUser: SyncUserMutation
  

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
  
    override val getActiveExpenses by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetActiveExpensesQueryImpl(this)
    }
  
    override val getActiveItems by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetActiveItemsQueryImpl(this)
    }
  
    override val getActiveSales by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetActiveSalesQueryImpl(this)
    }
  
    override val getActiveUdhaars by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetActiveUdhaarsQueryImpl(this)
    }
  
    override val getUser by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetUserQueryImpl(this)
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
  
    override val syncUdhaar by lazy(LazyThreadSafetyMode.PUBLICATION) {
      SyncUdhaarMutationImpl(this)
    }
  
    override val syncUdhaars by lazy(LazyThreadSafetyMode.PUBLICATION) {
      SyncUdhaarsQueryImpl(this)
    }
  
    override val syncUser by lazy(LazyThreadSafetyMode.PUBLICATION) {
      SyncUserMutationImpl(this)
    }
  

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun operations(): List<com.google.firebase.dataconnect.generated.GeneratedOperation<StorebookConnectorConnector, *, *>> =
    queries() + mutations()

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun mutations(): List<com.google.firebase.dataconnect.generated.GeneratedMutation<StorebookConnectorConnector, *, *>> =
    listOf(
      softDeleteExpense,
        softDeleteItem,
        softDeleteSale,
        softDeleteUdhaar,
        syncExpense,
        syncItem,
        syncSale,
        syncSaleItem,
        syncUdhaar,
        syncUser,
        
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun queries(): List<com.google.firebase.dataconnect.generated.GeneratedQuery<StorebookConnectorConnector, *, *>> =
    listOf(
      getActiveExpenses,
        getActiveItems,
        getActiveSales,
        getActiveUdhaars,
        getUser,
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



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
