
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



public interface SyncSupplierMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      StorebookConnectorConnector,
      SyncSupplierMutation.Data,
      SyncSupplierMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id:
    String,
    val storeId:
    String,
    val name:
    String,
    val phone:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val gstin:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val address:
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
        public var name: String
        public var phone: String?
        public var gstin: String?
        public var address: String?
        public var isDeleted: Boolean
        public var updatedAt: Double
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          id: String,storeId: String,name: String,isDeleted: Boolean,updatedAt: Double,
          block_: Builder.() -> Unit
        ): Variables {
          var id= id
            var storeId= storeId
            var name= name
            var phone: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var gstin: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var address: com.google.firebase.dataconnect.OptionalVariable<String?> =
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
              
            override var name: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { name = value_ }
              
            override var phone: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { phone = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var gstin: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { gstin = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var address: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { address = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var isDeleted: Boolean
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { isDeleted = value_ }
              
            override var updatedAt: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { updatedAt = value_ }
              
            
          }.apply(block_)
          .let {
            Variables(
              id=id,storeId=storeId,name=name,phone=phone,gstin=gstin,address=address,isDeleted=isDeleted,updatedAt=updatedAt,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  @kotlinx.serialization.SerialName("supplier_upsert")
    val key:
    SupplierKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncSupplier"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncSupplierMutation.ref(
  
    id: String,storeId: String,name: String,isDeleted: Boolean,updatedAt: Double,
  
    block_: SyncSupplierMutation.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.MutationRef<
    SyncSupplierMutation.Data,
    SyncSupplierMutation.Variables
  > =
  ref(
    
      SyncSupplierMutation.Variables.build(
        id=id,storeId=storeId,name=name,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
      )
    
  )

public suspend fun SyncSupplierMutation.execute(
  
    id: String,storeId: String,name: String,isDeleted: Boolean,updatedAt: Double,
  
    block_: SyncSupplierMutation.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.MutationResult<
    SyncSupplierMutation.Data,
    SyncSupplierMutation.Variables
  > =
  ref(
    
      id=id,storeId=storeId,name=name,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
    
  ).execute()



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
