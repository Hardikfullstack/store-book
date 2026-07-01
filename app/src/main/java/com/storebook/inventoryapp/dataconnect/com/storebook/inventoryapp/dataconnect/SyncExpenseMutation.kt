
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



public interface SyncExpenseMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      StorebookConnectorConnector,
      SyncExpenseMutation.Data,
      SyncExpenseMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id:
    String,
    val storeId:
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
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val supplierPhone:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val isDeleted:
    Boolean,
    val updatedAt:
    Double
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var id: String
        public var storeId: String
        public var type: String
        public var description: String
        public var amount: Double
        public var timestamp: Double
        public var supplierName: String?
        public var supplierPhone: String?
        public var isDeleted: Boolean
        public var updatedAt: Double
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          id: String,storeId: String,type: String,description: String,amount: Double,timestamp: Double,isDeleted: Boolean,updatedAt: Double,
          block_: Builder.() -> Unit
        ): Variables {
          var id= id
            var storeId= storeId
            var type= type
            var description= description
            var amount= amount
            var timestamp= timestamp
            var supplierName: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var supplierPhone: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var isDeleted= isDeleted
            var updatedAt= updatedAt
            

          return object : Builder {
            override var id: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { id = value_ }
              
            override var storeId: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { storeId = value_ }
              
            override var type: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { type = value_ }
              
            override var description: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { description = value_ }
              
            override var amount: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { amount = value_ }
              
            override var timestamp: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { timestamp = value_ }
              
            override var supplierName: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { supplierName = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var supplierPhone: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { supplierPhone = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var isDeleted: Boolean
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { isDeleted = value_ }
              
            override var updatedAt: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { updatedAt = value_ }
              
            
          }.apply(block_)
          .let {
            Variables(
              id=id,storeId=storeId,type=type,description=description,amount=amount,timestamp=timestamp,supplierName=supplierName,supplierPhone=supplierPhone,isDeleted=isDeleted,updatedAt=updatedAt,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  @kotlinx.serialization.SerialName("expenseEntry_upsert")
    val key:
    ExpenseEntryKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncExpense"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncExpenseMutation.ref(
  
    id: String,storeId: String,type: String,description: String,amount: Double,timestamp: Double,isDeleted: Boolean,updatedAt: Double,
  
    block_: SyncExpenseMutation.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.MutationRef<
    SyncExpenseMutation.Data,
    SyncExpenseMutation.Variables
  > =
  ref(
    
      SyncExpenseMutation.Variables.build(
        id=id,storeId=storeId,type=type,description=description,amount=amount,timestamp=timestamp,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
      )
    
  )

public suspend fun SyncExpenseMutation.execute(
  
    id: String,storeId: String,type: String,description: String,amount: Double,timestamp: Double,isDeleted: Boolean,updatedAt: Double,
  
    block_: SyncExpenseMutation.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.MutationResult<
    SyncExpenseMutation.Data,
    SyncExpenseMutation.Variables
  > =
  ref(
    
      id=id,storeId=storeId,type=type,description=description,amount=amount,timestamp=timestamp,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
    
  ).execute()



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
