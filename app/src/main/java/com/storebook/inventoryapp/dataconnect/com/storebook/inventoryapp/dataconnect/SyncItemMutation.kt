
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



public interface SyncItemMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      StorebookConnectorConnector,
      SyncItemMutation.Data,
      SyncItemMutation.Variables
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
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val hsnCode:
    com.google.firebase.dataconnect.OptionalVariable<String?>,
    val isDeleted:
    Boolean,
    val updatedAt:
    Int
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var id: String
        public var storeId: String
        public var name: String
        public var quantity: Double
        public var unit: String
        public var buyPrice: Double
        public var sellPrice: Double
        public var lowStockThreshold: Double
        public var category: String
        public var photoPath: String?
        public var hsnCode: String?
        public var isDeleted: Boolean
        public var updatedAt: Int
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          id: String,storeId: String,name: String,quantity: Double,unit: String,buyPrice: Double,sellPrice: Double,lowStockThreshold: Double,category: String,isDeleted: Boolean,updatedAt: Int,
          block_: Builder.() -> Unit
        ): Variables {
          var id= id
            var storeId= storeId
            var name= name
            var quantity= quantity
            var unit= unit
            var buyPrice= buyPrice
            var sellPrice= sellPrice
            var lowStockThreshold= lowStockThreshold
            var category= category
            var photoPath: com.google.firebase.dataconnect.OptionalVariable<String?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            var hsnCode: com.google.firebase.dataconnect.OptionalVariable<String?> =
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
              
            override var quantity: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { quantity = value_ }
              
            override var unit: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { unit = value_ }
              
            override var buyPrice: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { buyPrice = value_ }
              
            override var sellPrice: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { sellPrice = value_ }
              
            override var lowStockThreshold: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { lowStockThreshold = value_ }
              
            override var category: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { category = value_ }
              
            override var photoPath: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { photoPath = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var hsnCode: String?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { hsnCode = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            override var isDeleted: Boolean
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { isDeleted = value_ }
              
            override var updatedAt: Int
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { updatedAt = value_ }
              
            
          }.apply(block_)
          .let {
            Variables(
              id=id,storeId=storeId,name=name,quantity=quantity,unit=unit,buyPrice=buyPrice,sellPrice=sellPrice,lowStockThreshold=lowStockThreshold,category=category,photoPath=photoPath,hsnCode=hsnCode,isDeleted=isDeleted,updatedAt=updatedAt,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  @kotlinx.serialization.SerialName("item_upsert")
    val key:
    ItemKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncItem"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncItemMutation.ref(
  
    id: String,storeId: String,name: String,quantity: Double,unit: String,buyPrice: Double,sellPrice: Double,lowStockThreshold: Double,category: String,isDeleted: Boolean,updatedAt: Int,
  
    block_: SyncItemMutation.Variables.Builder.() -> Unit
  
): com.google.firebase.dataconnect.MutationRef<
    SyncItemMutation.Data,
    SyncItemMutation.Variables
  > =
  ref(
    
      SyncItemMutation.Variables.build(
        id=id,storeId=storeId,name=name,quantity=quantity,unit=unit,buyPrice=buyPrice,sellPrice=sellPrice,lowStockThreshold=lowStockThreshold,category=category,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
      )
    
  )

public suspend fun SyncItemMutation.execute(
  
    id: String,storeId: String,name: String,quantity: Double,unit: String,buyPrice: Double,sellPrice: Double,lowStockThreshold: Double,category: String,isDeleted: Boolean,updatedAt: Int,
  
    block_: SyncItemMutation.Variables.Builder.() -> Unit
  
  ): com.google.firebase.dataconnect.MutationResult<
    SyncItemMutation.Data,
    SyncItemMutation.Variables
  > =
  ref(
    
      id=id,storeId=storeId,name=name,quantity=quantity,unit=unit,buyPrice=buyPrice,sellPrice=sellPrice,lowStockThreshold=lowStockThreshold,category=category,isDeleted=isDeleted,updatedAt=updatedAt,
  
    block_
    
  ).execute()



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
