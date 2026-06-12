package com.storebook.inventoryapp.utils
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class PdfFileMetadata(
    val thumbnail: Bitmap?,
    val pageCount: Int,
    val isPasswordProtected: Boolean,
    val isCompressed: Boolean = false,
    val isCorrupt: Boolean = false,
)

object PdfUtils {
    // Cache for 1/8th of available max memory for Bitmaps
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val thumbnailCache =
        object : LruCache<String, PdfFileMetadata>(cacheSize) {
            override fun sizeOf(
                key: String,
                value: PdfFileMetadata,
            ): Int {
                // Calculate size in KB
                val bitmapSize = value.thumbnail?.byteCount?.div(1024) ?: 0
                // Add a small constant for the other two fields
                return bitmapSize + 1
            }
        }

    suspend fun getPdfMetadataCached(file: File): PdfFileMetadata {
        val cacheKey = "${file.absolutePath}_${file.lastModified()}_${file.length()}"

        // Return from cache if available
        thumbnailCache.get(cacheKey)?.let { return it }

        return withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            var isProtected = false
            var pageCount = 0
            var thumbnail: Bitmap? = null
            var isCompressedFlag = false

            try {
                if (!file.exists()) return@withContext PdfFileMetadata(null, 0, false, false)

                // 1. Initial filename-based check (for speed & legacy)
                isCompressedFlag = file.nameWithoutExtension.contains("_compressed", ignoreCase = true)

                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)

                // If it opened successfully, it's not password protected
                isProtected = false
                pageCount = renderer.pageCount

                // Generate thumbnail if there are pages
                if (pageCount > 0) {
                    val page = renderer.openPage(0)
                    thumbnail =
                        Bitmap.createBitmap(
                            (page.width * 0.25).toInt(),
                            (page.height * 0.25).toInt(),
                            Bitmap.Config.ARGB_8888,
                        )
                    page.render(thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                }
                renderer.close()

                // 2. Check for hidden metadata marking (if not protected)
                if (!isProtected) {
                    try {
                        PDDocument.load(file, MemoryUsageSetting.setupTempFileOnly()).use { doc ->
                            val infoType = doc.documentInformation.getCustomMetadataValue("PDFlex-Type")
                            if (infoType == "Compressed") {
                                isCompressedFlag = true
                            }
                        }
                    } catch (e: Throwable) {
                        // Ignore, rely on filename
                    }
                }
            } catch (e: Throwable) {
                // Determine if failure was due to password protection
                val message = e.message?.lowercase() ?: ""
                isProtected = e is SecurityException || message.contains("password") || message.contains("protected")

                if (isProtected) {
                    // Try to get page count using fallback mechanism without needing password
                    // PDFBox fallback
                    try {
                        val document = PDDocument.load(file, "", MemoryUsageSetting.setupTempFileOnly())
                        pageCount = document.numberOfPages
                        document.close()
                    } catch (e2: Throwable) {
                        try {
                            file.inputStream().use { input ->
                                val matchBufferSize = 8192
                                val bytes = ByteArray(matchBufferSize)
                                var read = input.read(bytes)
                                while (read != -1) {
                                    val chunk = String(bytes, 0, read, Charsets.US_ASCII)
                                    if (chunk.contains("/Count")) {
                                        val regex = Regex("/Count\\s+(\\d+)")
                                        val match = regex.find(chunk)
                                        val count = match?.groupValues?.get(1)?.toIntOrNull()
                                        if (count != null && count > 0) {
                                            pageCount = count
                                            break
                                        }
                                    }
                                    read = input.read(bytes)
                                }
                            }
                        } catch (e3: Exception) {
                            // Ignore
                        }
                    }
                }
            } finally {
                try {
                    pfd?.close()
                } catch (e: Exception) {
                    // Ignore
                }
            }

            val result =
                PdfFileMetadata(
                    thumbnail = thumbnail,
                    pageCount = pageCount,
                    isPasswordProtected = isProtected,
                    isCompressed = isCompressedFlag,
                    isCorrupt = !isProtected && pageCount == 0 && file.length() > 0,
                )

            // Save to cache
            thumbnailCache.put(cacheKey, result)

            result
        }
    }

    suspend fun isPasswordProtected(file: File): Boolean =
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            try {
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                renderer.close()
                false
            } catch (e: Exception) {
                // PdfRenderer throws SecurityException if file is password protected
                val message = e.message?.lowercase() ?: ""
                e is SecurityException || message.contains("password") || message.contains("protected")
            } finally {
                pfd?.close()
            }
        }

    suspend fun isPasswordProtected(
        context: android.content.Context,
        uri: android.net.Uri,
    ): Boolean =
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            try {
                pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val renderer = PdfRenderer(pfd)
                    renderer.close()
                    false
                } else {
                    true
                }
            } catch (e: Exception) {
                val message = e.message?.lowercase() ?: ""
                e is SecurityException || message.contains("password") || message.contains("protected")
            } finally {
                pfd?.close()
            }
        }

    suspend fun getPdfThumbnail(file: File): Bitmap? =
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            try {
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(0)
                    val bitmap =
                        Bitmap.createBitmap(
                            (page.width * 0.5).toInt(),
                            (page.height * 0.5).toInt(),
                            Bitmap.Config.ARGB_8888,
                        )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderer.close()
                    bitmap
                } else {
                    renderer.close()
                    null
                }
            } catch (e: Exception) {
                null
            } finally {
                try {
                    pfd?.close()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

    fun ellipsizeMiddle(
        text: String,
        maxLength: Int = 30,
    ): String {
        if (text.length <= maxLength) return text
        val prefixLen = maxLength / 2
        val suffixLen = maxLength - prefixLen - 3
        if (suffixLen <= 0) return text.take(maxLength - 3) + "..."
        return text.take(prefixLen) + "..." + text.takeLast(suffixLen)
    }

    fun formatDisplayPath(
        path: String,
        includeFileName: Boolean = true,
    ): String {
        val file = File(path)
        val targetPath = if (includeFileName) path else (file.parent ?: "")
        val internalStoragePath =
            android.os.Environment
                .getExternalStorageDirectory()
                .absolutePath
        return if (targetPath.startsWith(internalStoragePath)) {
            targetPath.replace(internalStoragePath, "/Internal Storage")
        } else {
            targetPath
        }
    }

    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(
            "%.2f %s",
            size / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups],
        )
    }

    suspend fun getPageCount(file: File): Int {
        return withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            try {
                if (!file.exists()) return@withContext 0
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val count = renderer.pageCount
                renderer.close()
                return@withContext count
            } catch (e: Exception) {
                // Try PDFBox fallback
            } finally {
                pfd?.close()
            }

            try {
                // PDFBox fallback
                val document = PDDocument.load(file, "", MemoryUsageSetting.setupTempFileOnly())
                val count = document.numberOfPages
                document.close()
                return@withContext count
            } catch (e: Throwable) {
                // Last resort: search file structure for /Count (often visible even in encrypted PDFs)
                try {
                    file.inputStream().use { input ->
                        val bytes = ByteArray(matchBufferSize)
                        var read = input.read(bytes)
                        while (read != -1) {
                            val chunk = String(bytes, 0, read, Charsets.US_ASCII)
                            if (chunk.contains("/Count")) {
                                val regex = Regex("/Count\\s+(\\d+)")
                                val match = regex.find(chunk)
                                val count = match?.groupValues?.get(1)?.toIntOrNull()
                                if (count != null && count > 0) return@withContext count
                            }
                            read = input.read(bytes)
                        }
                    }
                } catch (e2: Exception) {
                    // Ignore
                }
            }
            0
        }
    }

    private const val matchBufferSize = 8192

    suspend fun getPageCount(
        context: android.content.Context,
        uri: android.net.Uri,
    ): Int {
        return withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            try {
                pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val renderer = PdfRenderer(pfd)
                    val count = renderer.pageCount
                    renderer.close()
                    return@withContext count
                }
            } catch (e: Exception) {
                // Try fallback
            } finally {
                pfd?.close()
            }

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val document = PDDocument.load(inputStream, "", MemoryUsageSetting.setupTempFileOnly())
                    val count = document.numberOfPages
                    document.close()
                    return@withContext count
                }
            } catch (e: Throwable) {
                // Last resort structural search
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val bytes = ByteArray(matchBufferSize)
                        var read = input.read(bytes)
                        while (read != -1) {
                            val chunk = String(bytes, 0, read, Charsets.US_ASCII)
                            if (chunk.contains("/Count")) {
                                val regex = Regex("/Count\\s+(\\d+)")
                                val match = regex.find(chunk)
                                val count = match?.groupValues?.get(1)?.toIntOrNull()
                                if (count != null && count > 0) return@withContext count
                            }
                            read = input.read(bytes)
                        }
                    }
                } catch (e2: Exception) {
                    // Ignore
                }
            }
            0
        }
    }
}
