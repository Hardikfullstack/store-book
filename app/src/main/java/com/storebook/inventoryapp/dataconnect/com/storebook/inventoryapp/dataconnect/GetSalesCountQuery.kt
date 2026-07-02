
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


public interface GetSalesCountQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      GetSalesCountQuery.Data,
      GetSalesCountQuery.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val storeId:
    String,
    val type:
    com.google.firebase.dataconnect.OptionalVariable<String?>
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var storeId: String
        public var type: String?
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          storeId: String,
          block_: Builder.() -> Unit
        ): Variables {
          var storeId= storeId
            var type: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            

          return object : Builder {
            override var storeId: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { storeId = value_ }
              
            override var type: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { type = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            
          }.apply(block_)
          .let {
            Variables(
              storeId=storeId,type=type,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val sales:
    List<SalesItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class SalesItem(
  
    val id:
    String
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetSalesCount"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun GetSalesCountQuery.ref(
  
    storeId: String,
  
    block_: GetSalesCountQuery.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.QueryRef<
    GetSalesCountQuery.Data,
    GetSalesCountQuery.Variables
  > =
  ref(
    
      GetSalesCountQuery.Variables.build(
        storeId=storeId,
  
    block_
      )
    
  )

public suspend fun GetSalesCountQuery.execute(
  
    storeId: String,
  
    block_: GetSalesCountQuery.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.QueryResult<
    GetSalesCountQuery.Data,
    GetSalesCountQuery.Variables
  > =
  ref(
    
      storeId=storeId,
  
    block_
    
  ).execute()


  public fun GetSalesCountQuery.flow(
    
      storeId: String,
  
    block_: GetSalesCountQuery.Variables.Builder.() -> Unit
    
    ): kotlinx.coroutines.flow.Flow<GetSalesCountQuery.Data> =
    ref(
        
          storeId=storeId,
  
    block_
        
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
