
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

public interface SoftDeleteUdhaarMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        SoftDeleteUdhaarMutation.Data,
        SoftDeleteUdhaarMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val updatedAt: Double,
    )

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("udhaarEntry_update")
        val key: UdhaarEntryKey?,
    )

    public companion object {
        public val operationName: String = "SoftDeleteUdhaar"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun SoftDeleteUdhaarMutation.ref(
    id: String,
    updatedAt: Double,
): com.google.firebase.dataconnect.MutationRef<
    SoftDeleteUdhaarMutation.Data,
    SoftDeleteUdhaarMutation.Variables,
> =
    ref(
        SoftDeleteUdhaarMutation.Variables(
            id = id, updatedAt = updatedAt,
        ),
    )

public suspend fun SoftDeleteUdhaarMutation.execute(
    id: String,
    updatedAt: Double,
): com.google.firebase.dataconnect.MutationResult<
    SoftDeleteUdhaarMutation.Data,
    SoftDeleteUdhaarMutation.Variables,
> =
    ref(
        id = id, updatedAt = updatedAt,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
