
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


public interface GetActiveExpensesQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      GetActiveExpensesQuery.Data,
      GetActiveExpensesQuery.Variables
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
    val orderByTimestamp:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    val orderByType:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    val orderBySupplierName:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>,
    val orderByAmount:
    com.google.firebase.dataconnect.OptionalVariable<OrderDirection?>
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var storeId: String
        public var limit: Int?
        public var offset: Int?
        public var orderByTimestamp: OrderDirection?
        public var orderByType: OrderDirection?
        public var orderBySupplierName: OrderDirection?
        public var orderByAmount: OrderDirection?
        
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
            var orderByTimestamp: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderByType: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderBySupplierName: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var orderByAmount: com.google.firebase.dataconnect.OptionalVariable<OrderDirection?> =
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
              
            override var orderByTimestamp: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderByTimestamp = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderByType: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderByType = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderBySupplierName: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderBySupplierName = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var orderByAmount: OrderDirection?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { orderByAmount = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            
          }.apply(block_)
          .let {
            Variables(
              storeId=storeId,limit=limit,offset=offset,orderByTimestamp=orderByTimestamp,orderByType=orderByType,orderBySupplierName=orderBySupplierName,orderByAmount=orderByAmount,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val expenseEntries:
    List<ExpenseEntriesItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class ExpenseEntriesItem(
  
    val id:
    String,
    val type:
    String,
    val description:
    String,
    val amount:
    Double,
    val timestamp:
    Double,
    val supplierName:
    String?,
    val updatedAt:
    Double
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetActiveExpenses"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun GetActiveExpensesQuery.ref(
  
    storeId: String,
  
    block_: GetActiveExpensesQuery.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.QueryRef<
    GetActiveExpensesQuery.Data,
    GetActiveExpensesQuery.Variables
  > =
  ref(
    
      GetActiveExpensesQuery.Variables.build(
        storeId=storeId,
  
    block_
      )
    
  )

public suspend fun GetActiveExpensesQuery.execute(
  
    storeId: String,
  
    block_: GetActiveExpensesQuery.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.QueryResult<
    GetActiveExpensesQuery.Data,
    GetActiveExpensesQuery.Variables
  > =
  ref(
    
      storeId=storeId,
  
    block_
    
  ).execute()


  public fun GetActiveExpensesQuery.flow(
    
      storeId: String,
  
    block_: GetActiveExpensesQuery.Variables.Builder.() -> Unit
    
    ): kotlinx.coroutines.flow.Flow<GetActiveExpensesQuery.Data> =
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
