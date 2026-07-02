
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



public interface SyncPurchaseMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      StorebookConnectorConnector,
      SyncPurchaseMutation.Data,
      SyncPurchaseMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
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
        public var supplierId: String
        public var supplierName: String
        public var totalAmount: Double
        public var taxAmount: Double
        public var type: String
        public var timestamp: Double
        public var notes: String?
        public var isDeleted: Boolean
        public var updatedAt: Double
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          id: String,storeId: String,supplierId: String,supplierName: String,totalAmount: Double,taxAmount: Double,type: String,timestamp: Double,isDeleted: Boolean,updatedAt: Double,
          block_: Builder.() -> Unit
        ): Variables {
          var id= id
            var storeId= storeId
            var supplierId= supplierId
            var supplierName= supplierName
            var totalAmount= totalAmount
            var taxAmount= taxAmount
            var type= type
            var timestamp= timestamp
            var notes: com.google.firebase.dataconnect.OptionalVariable<String?> =
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
              
            override var supplierId: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { supplierId = value_ }
              
            override var supplierName: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { supplierName = value_ }
              
            override var totalAmount: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { totalAmount = value_ }
              
            override var taxAmount: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { taxAmount = value_ }
              
            override var type: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { type = value_ }
              
            override var timestamp: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { timestamp = value_ }
              
            override var notes: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { notes = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var isDeleted: Boolean
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { isDeleted = value_ }
              
            override var updatedAt: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { updatedAt = value_ }
              
            
          }.apply(block_)
          .let {
            Variables(
              id=id,storeId=storeId,supplierId=supplierId,supplierName=supplierName,totalAmount=totalAmount,taxAmount=taxAmount,type=type,timestamp=timestamp,notes=notes,isDeleted=isDeleted,updatedAt=updatedAt,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  @kotlinx.serialization.SerialName("purchase_upsert")
    val key:
    PurchaseKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncPurchase"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncPurchaseMutation.ref(
  
    id: String,storeId: String,supplierId: String,supplierName: String,totalAmount: Double,taxAmount: Double,type: String,timestamp: Double,isDeleted: Boolean,updatedAt: Double,
  
    block_: SyncPurchaseMutation.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.MutationRef<
    SyncPurchaseMutation.Data,
    SyncPurchaseMutation.Variables
  > =
  ref(
    
      SyncPurchaseMutation.Variables.build(
        id=id,storeId=storeId,supplierId=supplierId,supplierName=supplierName,totalAmount=totalAmount,taxAmount=taxAmount,type=type,timestamp=timestamp,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
      )
    
  )

public suspend fun SyncPurchaseMutation.execute(
  
    id: String,storeId: String,supplierId: String,supplierName: String,totalAmount: Double,taxAmount: Double,type: String,timestamp: Double,isDeleted: Boolean,updatedAt: Double,
  
    block_: SyncPurchaseMutation.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.MutationResult<
    SyncPurchaseMutation.Data,
    SyncPurchaseMutation.Variables
  > =
  ref(
    
      id=id,storeId=storeId,supplierId=supplierId,supplierName=supplierName,totalAmount=totalAmount,taxAmount=taxAmount,type=type,timestamp=timestamp,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
    
  ).execute()



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
