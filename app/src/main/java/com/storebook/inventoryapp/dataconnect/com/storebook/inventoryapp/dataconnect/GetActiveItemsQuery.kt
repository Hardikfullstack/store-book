
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


public interface GetActiveItemsQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      GetActiveItemsQuery.Data,
      GetActiveItemsQuery.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val storeId:
    String,
    val limit:
    com.google.firebase.dataconnect.OptionalVariable<Int?>,
    val offset:
    com.google.firebase.dataconnect.OptionalVariable<Int?>,
    val orderByName:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    val orderByQuantity:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    val orderByBuyPrice:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    val orderBySellPrice:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    val orderByCategory:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    val orderByUpdatedAt:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var storeId: String
        public var limit: Int?
        public var offset: Int?
        public var orderByName: OrderDirection?
        public var orderByQuantity: OrderDirection?
        public var orderByBuyPrice: OrderDirection?
        public var orderBySellPrice: OrderDirection?
        public var orderByCategory: OrderDirection?
        public var orderByUpdatedAt: OrderDirection?
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          storeId: String,
          block_: Builder.() -> Unit
        ): Variables {
          var storeId= storeId
            var limit: com.google.firebase.dataconnect.OptionalVariable<Int?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var offset: com.google.firebase.dataconnect.OptionalVariable<Int?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderByName: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderByQuantity: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderByBuyPrice: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderBySellPrice: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderByCategory: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderByUpdatedAt: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            

          return object : Builder {
            override var storeId: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { storeId = value_ }
              
            override var limit: Int?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { limit = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var offset: Int?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { offset = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderByName: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderByName = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderByQuantity: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderByQuantity = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderByBuyPrice: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderByBuyPrice = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderBySellPrice: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderBySellPrice = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderByCategory: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderByCategory = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderByUpdatedAt: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderByUpdatedAt = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            
          }.apply(block_)
          .let {
            Variables(
              storeId=storeId,limit=limit,offset=offset,orderByName=orderByName,orderByQuantity=orderByQuantity,orderByBuyPrice=orderByBuyPrice,orderBySellPrice=orderBySellPrice,orderByCategory=orderByCategory,orderByUpdatedAt=orderByUpdatedAt,
            )
          }
        }
      }
    
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
    Double
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetActiveItems"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun GetActiveItemsQuery.ref(
  
    storeId: String,
  
    block_: GetActiveItemsQuery.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.QueryRef<
    GetActiveItemsQuery.Data,
    GetActiveItemsQuery.Variables
  > =
  ref(
    
      GetActiveItemsQuery.Variables.build(
        storeId=storeId,
  
    block_
      )
    
  )

public suspend fun GetActiveItemsQuery.execute(
  
    storeId: String,
  
    block_: GetActiveItemsQuery.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.QueryResult<
    GetActiveItemsQuery.Data,
    GetActiveItemsQuery.Variables
  > =
  ref(
    
      storeId=storeId,
  
    block_
    
  ).execute()


  public fun GetActiveItemsQuery.flow(
    
      storeId: String,
  
    block_: GetActiveItemsQuery.Variables.Builder.() -> Unit
    
    ): kotlinx.coroutines.flow.Flow<GetActiveItemsQuery.Data> =
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
