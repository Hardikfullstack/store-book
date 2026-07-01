
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



public interface SyncUserMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      StorebookConnectorConnector,
      SyncUserMutation.Data,
      SyncUserMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id:
    String,
    val phoneNumber:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val createdAt:
    Double,
    val role:
    String,
    val stores:
    com.google.firebase.dataconnect.OptionalVariable<List<String>?>,
    val storeId:
    com.google.firebase.dataconnect.OptionalVariable<String?>
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var id: String
        public var phoneNumber: String?
        public var createdAt: Double
        public var role: String
        public var stores: List<String>?
        public var storeId: String?
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          id: String,createdAt: Double,role: String,
          block_: Builder.() -> Unit
        ): Variables {
          var id= id
            var phoneNumber: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var createdAt= createdAt
            var role= role
            var stores: com.google.firebase.dataconnect.OptionalVariable<List<String>?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var storeId: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            

          return object : Builder {
            override var id: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { id = value_ }
              
            override var phoneNumber: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { phoneNumber = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var createdAt: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { createdAt = value_ }
              
            override var role: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { role = value_ }
              
            override var stores: List<String>?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { stores = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var storeId: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { storeId = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            
          }.apply(block_)
          .let {
            Variables(
              id=id,phoneNumber=phoneNumber,createdAt=createdAt,role=role,stores=stores,storeId=storeId,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  @kotlinx.serialization.SerialName("user_upsert")
    val key:
    UserKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncUser"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncUserMutation.ref(
  
    id: String,createdAt: Double,role: String,
  
    block_: SyncUserMutation.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.MutationRef<
    SyncUserMutation.Data,
    SyncUserMutation.Variables
  > =
  ref(
    
      SyncUserMutation.Variables.build(
        id=id,createdAt=createdAt,role=role,
  
    block_
      )
    
  )

public suspend fun SyncUserMutation.execute(
  
    id: String,createdAt: Double,role: String,
  
    block_: SyncUserMutation.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.MutationResult<
    SyncUserMutation.Data,
    SyncUserMutation.Variables
  > =
  ref(
    
      id=id,createdAt=createdAt,role=role,
  
    block_
    
  ).execute()



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
