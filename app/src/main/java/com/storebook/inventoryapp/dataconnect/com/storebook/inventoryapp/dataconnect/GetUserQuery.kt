
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


public interface GetUserQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      GetUserQuery.Data,
      GetUserQuery.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id:
    String
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val user:
    User?
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class User(
  
    val id:
    String,
    val phoneNumber:
    String?,
    val role:
    String,
    val stores:
    List<String>?,
    val storeId:
    String?
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetUser"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun GetUserQuery.ref(
  
    id: String,
  
  
): com.google.firebase.dataconnect.QueryRef<
    GetUserQuery.Data,
    GetUserQuery.Variables
  > =
  ref(
    
      GetUserQuery.Variables(
        id=id,
  
      )
    
  )

public suspend fun GetUserQuery.execute(
  
    id: String,
  
  
  ): com.google.firebase.dataconnect.QueryResult<
    GetUserQuery.Data,
    GetUserQuery.Variables
  > =
  ref(
    
      id=id,
  
    
  ).execute()


  public fun GetUserQuery.flow(
    
      id: String,
  
    
    ): kotlinx.coroutines.flow.Flow<GetUserQuery.Data> =
    ref(
        
          id=id,
  
        
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
