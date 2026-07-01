
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


public interface GetAnnouncementsQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      StorebookConnectorConnector,
      GetAnnouncementsQuery.Data,
      Unit
    >
{
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val announcements:
    List<AnnouncementsItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class AnnouncementsItem(
  
    val id:
    String,
    val title:
    String,
    val message:
    String,
    val type:
    String,
    val isActive:
    Boolean,
    val createdAt:
    Double
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetAnnouncements"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Unit> =
      kotlinx.serialization.serializer()
  }
}

public fun GetAnnouncementsQuery.ref(
  
): com.google.firebase.dataconnect.QueryRef<
    GetAnnouncementsQuery.Data,
    Unit
  > =
  ref(
    
      Unit
    
  )

public suspend fun GetAnnouncementsQuery.execute(
  
  ): com.google.firebase.dataconnect.QueryResult<
    GetAnnouncementsQuery.Data,
    Unit
  > =
  ref(
    
  ).execute()


  public fun GetAnnouncementsQuery.flow(
    
    ): kotlinx.coroutines.flow.Flow<GetAnnouncementsQuery.Data> =
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
