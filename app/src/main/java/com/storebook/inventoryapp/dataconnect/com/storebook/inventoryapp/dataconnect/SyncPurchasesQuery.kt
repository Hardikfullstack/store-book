
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


public interface SyncPurchasesQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      SyncPurchasesQuery.Data,
      SyncPurchasesQuery.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val storeId:
    String,
    val lastSync:
    Double
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val purchases:
    List<PurchasesItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class PurchasesItem(
  
    val id:
    String,
    val storeId:
    String,
    val supplierId:
    String,
    val supplierName:
    String,
    val totalAmount:
    Double,
    val taxAmount:
    Double,
    val type:
    String,
    val timestamp:
    Double,
    val notes:
    String?,
    val isDeleted:
    Boolean,
    val updatedAt:
    Double
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncPurchases"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncPurchasesQuery.ref(
  
    storeId: String,lastSync: Double,
  
  
): com.google.firebase.dataconnect.QueryRef<
    SyncPurchasesQuery.Data,
    SyncPurchasesQuery.Variables
  > =
  ref(
    
      SyncPurchasesQuery.Variables(
        storeId=storeId,lastSync=lastSync,
  
      )
    
  )

public suspend fun SyncPurchasesQuery.execute(
  
    storeId: String,lastSync: Double,
  
  
  ): com.google.firebase.dataconnect.QueryResult<
    SyncPurchasesQuery.Data,
    SyncPurchasesQuery.Variables
  > =
  ref(
    
      storeId=storeId,lastSync=lastSync,
  
    
  ).execute()


  public fun SyncPurchasesQuery.flow(
    
      storeId: String,lastSync: Double,
  
    
    ): kotlinx.coroutines.flow.Flow<SyncPurchasesQuery.Data> =
    ref(
        
          storeId=storeId,lastSync=lastSync,
  
        
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
