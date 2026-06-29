
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



public interface SyncSaleItemMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      StorebookConnectorConnector,
      SyncSaleItemMutation.Data,
      SyncSaleItemMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id:
    String,
    val storeId:
    String,
    val saleId:
    String,
    val itemId:
    String,
    val itemName:
    String,
    val unit:
    String,
    val quantity:
    Double,
    val sellPrice:
    Double,
    val buyPrice:
    Double,
    val isDeleted:
    Boolean,
    val updatedAt:
    Int
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  @kotlinx.serialization.SerialName("saleItemDetail_upsert")
    val key:
    SaleItemDetailKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "SyncSaleItem"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SyncSaleItemMutation.ref(
  
    id: String,storeId: String,saleId: String,itemId: String,itemName: String,unit: String,quantity: Double,sellPrice: Double,buyPrice: Double,isDeleted: Boolean,updatedAt: Int,
  
  
): com.google.firebase.dataconnect.MutationRef<
    SyncSaleItemMutation.Data,
    SyncSaleItemMutation.Variables
  > =
  ref(
    
      SyncSaleItemMutation.Variables(
        id=id,storeId=storeId,saleId=saleId,itemId=itemId,itemName=itemName,unit=unit,quantity=quantity,sellPrice=sellPrice,buyPrice=buyPrice,isDeleted=isDeleted,updatedAt=updatedAt,
  
      )
    
  )

public suspend fun SyncSaleItemMutation.execute(
  
    id: String,storeId: String,saleId: String,itemId: String,itemName: String,unit: String,quantity: Double,sellPrice: Double,buyPrice: Double,isDeleted: Boolean,updatedAt: Int,
  
  
  ): com.google.firebase.dataconnect.MutationResult<
    SyncSaleItemMutation.Data,
    SyncSaleItemMutation.Variables
  > =
  ref(
    
      id=id,storeId=storeId,saleId=saleId,itemId=itemId,itemName=itemName,unit=unit,quantity=quantity,sellPrice=sellPrice,buyPrice=buyPrice,isDeleted=isDeleted,updatedAt=updatedAt,
  
    
  ).execute()



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
