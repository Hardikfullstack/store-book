package com.storebook.inventoryapp.data.sync

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.storebook.inventoryapp.BuildConfig
import com.storebook.inventoryapp.utils.EmulatorConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BatchSyncClient(
    private val okHttpClient: OkHttpClient = defaultHttpClient,
) {
    companion object {
        private const val TAG = "BatchSyncClient"
        private const val LOCATION = "us-east4"
        private const val SERVICE_ID = "new-storebook-service"
        private const val CONNECTOR_ID = "batch-connector"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        const val DEFAULT_BATCH_CHUNK_SIZE = 100

        private val defaultHttpClient: OkHttpClient by lazy {
            OkHttpClient
                .Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
        }

        val instance: BatchSyncClient by lazy { BatchSyncClient() }
    }

    private fun getEndpointUrl(): String {
        val projectId =
            try {
                FirebaseApp.getInstance().options.projectId ?: "new-storebook"
            } catch (_: Exception) {
                "new-storebook"
            }

        return if (EmulatorConfig.USE_EMULATOR && BuildConfig.DEBUG) {
            "http://${EmulatorConfig.EMULATOR_HOST}:${EmulatorConfig.DATA_CONNECT_PORT}/v1/projects/" +
                "$projectId/locations/$LOCATION/services/$SERVICE_ID/connectors/$CONNECTOR_ID:executeMutation"
        } else {
            "https://firebasedataconnect.googleapis.com/v1/projects/" +
                "$projectId/locations/$LOCATION/services/$SERVICE_ID/connectors/$CONNECTOR_ID:executeMutation"
        }
    }

    private suspend fun getAuthToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return suspendCancellableCoroutine { cont ->
            user
                .getIdToken(false)
                .addOnSuccessListener { result ->
                    cont.resume(result.token)
                }.addOnFailureListener { exception ->
                    cont.resumeWithException(exception)
                }
        }
    }

    suspend fun execute(
        operationName: String,
        variables: Map<String, Any?>,
    ): JSONObject =
        withContext(Dispatchers.IO) {
            val url = getEndpointUrl()
            val token = getAuthToken()

            val payload = JSONObject()
            payload.put("operationName", operationName)
            payload.put("variables", mapToJsonObject(variables))

            val body = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val requestBuilder =
                Request
                    .Builder()
                    .url(url)
                    .post(body)
                    .header("Content-Type", "application/json")

            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            Log.d(TAG, "Executing batch mutation $operationName to $url")

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = "Batch mutation $operationName failed HTTP ${response.code}: $responseBody"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }

            val jsonResponse =
                try {
                    JSONObject(responseBody)
                } catch (e: Exception) {
                    throw IOException("Failed to parse response for $operationName: $responseBody", e)
                }

            if (jsonResponse.has("errors")) {
                val errorsArray = jsonResponse.optJSONArray("errors")
                val firstError = errorsArray?.optJSONObject(0)?.optString("message") ?: "Unknown GraphQL error"
                val errorMsg = "Batch mutation $operationName returned GraphQL error: $firstError"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }

            jsonResponse
        }

    private fun mapToJsonObject(map: Map<String, Any?>): JSONObject {
        val json = JSONObject()
        for ((key, value) in map) {
            json.put(key, wrapJsonValue(value))
        }
        return json
    }

    private fun wrapJsonValue(value: Any?): Any =
        when (value) {
            null -> JSONObject.NULL
            is Map<*, *> -> {
                val obj = JSONObject()
                for ((k, v) in value) {
                    if (k is String) {
                        obj.put(k, wrapJsonValue(v))
                    }
                }
                obj
            }
            is List<*> -> {
                val arr = JSONArray()
                for (item in value) {
                    arr.put(wrapJsonValue(item))
                }
                arr
            }
            is Number, is Boolean, is String -> value
            else -> value.toString()
        }
}
