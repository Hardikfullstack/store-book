
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



public interface SoftDeleteExpenseMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      StorebookConnectorConnector,
      SoftDeleteExpenseMutation.Data,
      SoftDeleteExpenseMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id:
    String,
    val updatedAt:
    Double
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  @kotlinx.serialization.SerialName("expenseEntry_update")
    val key:
    ExpenseEntryKey?
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "SoftDeleteExpense"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun SoftDeleteExpenseMutation.ref(
  
    id: String,updatedAt: Double,
  
  
): com.google.firebase.dataconnect.MutationRef<
    SoftDeleteExpenseMutation.Data,
    SoftDeleteExpenseMutation.Variables
  > =
  ref(
    
      SoftDeleteExpenseMutation.Variables(
        id=id,updatedAt=updatedAt,
  
      )
    
  )

public suspend fun SoftDeleteExpenseMutation.execute(
  
    id: String,updatedAt: Double,
  
  
  ): com.google.firebase.dataconnect.MutationResult<
    SoftDeleteExpenseMutation.Data,
    SoftDeleteExpenseMutation.Variables
  > =
  ref(
    
      id=id,updatedAt=updatedAt,
  
    
  ).execute()



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
