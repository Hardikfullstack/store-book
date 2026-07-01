
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


public interface GetPromoCodesQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      GetPromoCodesQuery.Data,
      Unit
    >
{
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val promoCodes:
    List<PromoCodesItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class PromoCodesItem(
  
    val id:
    String,
    val code:
    String,
    val discountPercent:
    Double?,
    val discountAmount:
    Double?,
    val maxUses:
    Double?,
    val currentUses:
    Double?,
    val expiresAt:
    Double?,
    val isActive:
    Boolean
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetPromoCodes"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Unit> =
      kotlinx.serialization.serializer()
  }
}

public fun GetPromoCodesQuery.ref(
  
): com.google.firebase.dataconnect.QueryRef<
    GetPromoCodesQuery.Data,
    Unit
  > =
  ref(
    
      Unit
    
  )

public suspend fun GetPromoCodesQuery.execute(
  
  ): com.google.firebase.dataconnect.QueryResult<
    GetPromoCodesQuery.Data,
    Unit
  > =
  ref(
    
  ).execute()


  public fun GetPromoCodesQuery.flow(
    
    ): kotlinx.coroutines.flow.Flow<GetPromoCodesQuery.Data> =
    ref(
        
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
