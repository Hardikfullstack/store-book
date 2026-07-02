
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


public interface SyncSuppliersQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      SyncSuppliersQuery.Data,
      SyncSuppliersQuery.Variables
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
  
    val suppliers:
    List<SuppliersItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class SuppliersItem(
  
    val id:
    String,
    val storeId:
    String,
    val name:
    String,
    val phone:
    String?,
    val gstin:
    String?,
    val address:
    String?,
    val isDeleted:
    Boolean,
    val updatedAt:
    Double
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncSuppliers"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncSuppliersQuery.ref(
  
    storeId: String,lastSync: Double,
  
  
): com.google.firebase.dataconnect.QueryRef<
    SyncSuppliersQuery.Data,
    SyncSuppliersQuery.Variables
  > =
  ref(
    
      SyncSuppliersQuery.Variables(
        storeId=storeId,lastSync=lastSync,
  
      )
    
  )

public suspend fun SyncSuppliersQuery.execute(
  
    storeId: String,lastSync: Double,
  
  
  ): com.google.firebase.dataconnect.QueryResult<
    SyncSuppliersQuery.Data,
    SyncSuppliersQuery.Variables
  > =
  ref(
    
      storeId=storeId,lastSync=lastSync,
  
    
  ).execute()


  public fun SyncSuppliersQuery.flow(
    
      storeId: String,lastSync: Double,
  
    
    ): kotlinx.coroutines.flow.Flow<SyncSuppliersQuery.Data> =
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
