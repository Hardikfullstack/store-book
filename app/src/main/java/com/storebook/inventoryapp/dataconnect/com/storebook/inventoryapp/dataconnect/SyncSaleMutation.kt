
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



public interface SyncSaleMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      StorebookConnectorConnector,
      SyncSaleMutation.Data,
      SyncSaleMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id:
    String,
    val storeId:
    String,
    val timestamp:
    Double,
    val totalAmount:
    Double,
    val discountAmount:
    Double,
    val customerName:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val customerGstin:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val businessGstin:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val customerAddress:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val businessAddress:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val type:
    String,
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
        public var timestamp: Double
        public var totalAmount: Double
        public var discountAmount: Double
        public var customerName: String?
        public var customerGstin: String?
        public var businessGstin: String?
        public var customerAddress: String?
        public var businessAddress: String?
        public var type: String
        public var notes: String?
        public var isDeleted: Boolean
        public var updatedAt: Double
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          id: String,storeId: String,timestamp: Double,totalAmount: Double,discountAmount: Double,type: String,isDeleted: Boolean,updatedAt: Double,
          block_: Builder.() -> Unit
        ): Variables {
          var id= id
            var storeId= storeId
            var timestamp= timestamp
            var totalAmount= totalAmount
            var discountAmount= discountAmount
            var customerName: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var customerGstin: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var businessGstin: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var customerAddress: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var businessAddress: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var type= type
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
              
            override var timestamp: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { timestamp = value_ }
              
            override var totalAmount: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { totalAmount = value_ }
              
            override var discountAmount: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { discountAmount = value_ }
              
            override var customerName: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { customerName = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var customerGstin: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { customerGstin = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var businessGstin: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { businessGstin = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var customerAddress: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { customerAddress = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var businessAddress: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { businessAddress = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var type: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { type = value_ }
              
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
              id=id,storeId=storeId,timestamp=timestamp,totalAmount=totalAmount,discountAmount=discountAmount,customerName=customerName,customerGstin=customerGstin,businessGstin=businessGstin,customerAddress=customerAddress,businessAddress=businessAddress,type=type,notes=notes,isDeleted=isDeleted,updatedAt=updatedAt,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  @kotlinx.serialization.SerialName("sale_upsert")
    val key:
    SaleKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncSale"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncSaleMutation.ref(
  
    id: String,storeId: String,timestamp: Double,totalAmount: Double,discountAmount: Double,type: String,isDeleted: Boolean,updatedAt: Double,
  
    block_: SyncSaleMutation.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.MutationRef<
    SyncSaleMutation.Data,
    SyncSaleMutation.Variables
  > =
  ref(
    
      SyncSaleMutation.Variables.build(
        id=id,storeId=storeId,timestamp=timestamp,totalAmount=totalAmount,discountAmount=discountAmount,type=type,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
      )
    
  )

public suspend fun SyncSaleMutation.execute(
  
    id: String,storeId: String,timestamp: Double,totalAmount: Double,discountAmount: Double,type: String,isDeleted: Boolean,updatedAt: Double,
  
    block_: SyncSaleMutation.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.MutationResult<
    SyncSaleMutation.Data,
    SyncSaleMutation.Variables
  > =
  ref(
    
      id=id,storeId=storeId,timestamp=timestamp,totalAmount=totalAmount,discountAmount=discountAmount,type=type,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
    
  ).execute()



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
