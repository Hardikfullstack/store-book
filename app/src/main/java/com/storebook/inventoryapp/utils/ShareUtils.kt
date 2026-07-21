package com.storebook.inventoryapp.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {
    fun sharePdf(
        context: Context,
        file: File,
    ) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        sharePdfUri(context, uri)
    }

    fun sharePdfUri(
        context: Context,
        uri: android.net.Uri,
    ) {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        val chooserIntent =
            Intent.createChooser(intent, "Share PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(chooserIntent)
    }

    fun openPdf(
        context: Context,
        file: File,
    ) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            // Handle no PDF viewer installed
            android.widget.Toast
                .makeText(
                    context,
                    "No PDF viewer found",
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun openFolder(
        context: Context,
        file: File,
    ) {
        try {
            val parentFile = file.parentFile ?: file
            val parentPath = parentFile.absolutePath

            // Try using DocumentsContract for system file manager
            val relativePath =
                if (parentPath.contains("/storage/emulated/0/")) {
                    parentPath.substringAfter("/storage/emulated/0/")
                } else {
                    null
                }

            if (relativePath != null) {
                val folderUri =
                    android.net.Uri.parse(
                        "content://com.android.externalstorage.documents/document/primary:${relativePath.replace(
                            "/",
                            "%2F",
                        )}",
                    )
                val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(folderUri, "vnd.android.document/directory")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                context.startActivity(intent)
            } else {
                // Fallback to FileProvider + generic explorer intents
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", parentFile)
                val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "vnd.android.document/directory")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            try {
                // Secondary fallback: Try opening as a generic resource folder
                val parentFile = file.parentFile ?: file
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", parentFile)
                val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "resource/folder")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Final fallback: try just FileProvider URI with */*
                try {
                    val parentFile = file.parentFile ?: file
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", parentFile)
                    val intent =
                        Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "*/*")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    context.startActivity(intent)
                } catch (e3: Exception) {
                    android.widget.Toast
                        .makeText(
                            context,
                            "No file manager found",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }
}
