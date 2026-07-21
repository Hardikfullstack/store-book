
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

public interface UpsertAnnouncementMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
        StorebookConnectorConnector,
        UpsertAnnouncementMutation.Data,
        UpsertAnnouncementMutation.Variables,
    > {
    @kotlinx.serialization.Serializable
    public data class Variables(
        val id: String,
        val title: String,
        val message: String,
        val type: String,
        val isActive: Boolean,
        val createdAt: Double,
    )

    @kotlinx.serialization.Serializable
    public data class Data(
        @kotlinx.serialization.SerialName("announcement_upsert")
        val key: AnnouncementKey,
    )

    public companion object {
        public val operationName: String = "UpsertAnnouncement"

        public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
            kotlinx.serialization.serializer()

        public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
            kotlinx.serialization.serializer()
    }
}

public fun UpsertAnnouncementMutation.ref(
    id: String,
    title: String,
    message: String,
    type: String,
    isActive: Boolean,
    createdAt: Double,
): com.google.firebase.dataconnect.MutationRef<
    UpsertAnnouncementMutation.Data,
    UpsertAnnouncementMutation.Variables,
> =
    ref(
        UpsertAnnouncementMutation.Variables(
            id = id, title = title, message = message, type = type, isActive = isActive, createdAt = createdAt,
        ),
    )

public suspend fun UpsertAnnouncementMutation.execute(
    id: String,
    title: String,
    message: String,
    type: String,
    isActive: Boolean,
    createdAt: Double,
): com.google.firebase.dataconnect.MutationResult<
    UpsertAnnouncementMutation.Data,
    UpsertAnnouncementMutation.Variables,
> =
    ref(
        id = id, title = title, message = message, type = type, isActive = isActive, createdAt = createdAt,
    ).execute()

// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR storebook-connector
