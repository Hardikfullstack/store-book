
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

  
    public val getActiveItems: GetActiveItemsQuery
  
    public val softDeleteItem: SoftDeleteItemMutation
  
    public val softDeleteSale: SoftDeleteSaleMutation
  
    public val syncItem: SyncItemMutation
  
    public val syncItems: SyncItemsQuery
  
    public val syncSale: SyncSaleMutation
  
    public val syncSaleItem: SyncSaleItemMutation
  
    public val syncSaleItems: SyncSaleItemsQuery
  
    public val syncSales: SyncSalesQuery
  

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
  
    override val getActiveItems by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetActiveItemsQueryImpl(this)
    }
  
    override val softDeleteItem by lazy(LazyThreadSafetyMode.PUBLICATION) {
      SoftDeleteItemMutationImpl(this)
    }
  
    override val softDeleteSale by lazy(LazyThreadSafetyMode.PUBLICATION) {
      SoftDeleteSaleMutationImpl(this)
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
  

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun operations(): List<com.google.firebase.dataconnect.generated.GeneratedOperation<StorebookConnectorConnector, *, *>> =
    queries() + mutations()

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun mutations(): List<com.google.firebase.dataconnect.generated.GeneratedMutation<StorebookConnectorConnector, *, *>> =
    listOf(
      softDeleteItem,
        softDeleteSale,
        syncItem,
        syncSale,
        syncSaleItem,
        
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun queries(): List<com.google.firebase.dataconnect.generated.GeneratedQuery<StorebookConnectorConnector, *, *>> =
    listOf(
      getActiveItems,
        syncItems,
        syncSaleItems,
        syncSales,
        
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



private class GetActiveItemsQueryImpl(
  connector: StorebookConnectorConnector
):
  GetActiveItemsQuery,
  StorebookConnectorConnectorGeneratedQueryImpl<
      GetActiveItemsQuery.Data,
      Unit
  >(
    connector,
    GetActiveItemsQuery.Companion.operationName,
    GetActiveItemsQuery.Companion.dataDeserializer,
    GetActiveItemsQuery.Companion.variablesSerializer,
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



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
