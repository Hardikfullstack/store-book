
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


public interface SyncItemsQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      SyncItemsQuery.Data,
      SyncItemsQuery.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val lastSync:
    Int
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val items:
    List<ItemsItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class ItemsItem(
  
    val id:
    String,
    val name:
    String,
    val quantity:
    Double,
    val unit:
    String,
    val buyPrice:
    Double,
    val sellPrice:
    Double,
    val lowStockThreshold:
    Double,
    val category:
    String,
    val photoPath:
    String?,
    val hsnCode:
    String?,
    val isDeleted:
    Boolean,
    val updatedAt:
    Int
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncItems"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncItemsQuery.ref(
  
    lastSync: Int,
  
  
): com.google.firebase.dataconnect.QueryRef<
    SyncItemsQuery.Data,
    SyncItemsQuery.Variables
  > =
  ref(
    
      SyncItemsQuery.Variables(
        lastSync=lastSync,
  
      )
    
  )

public suspend fun SyncItemsQuery.execute(
  
    lastSync: Int,
  
  
  ): com.google.firebase.dataconnect.QueryResult<
    SyncItemsQuery.Data,
    SyncItemsQuery.Variables
  > =
  ref(
    
      lastSync=lastSync,
  
    
  ).execute()


  public fun SyncItemsQuery.flow(
    
      lastSync: Int,
  
    
    ): kotlinx.coroutines.flow.Flow<SyncItemsQuery.Data> =
    ref(
        
          lastSync=lastSync,
  
        
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
