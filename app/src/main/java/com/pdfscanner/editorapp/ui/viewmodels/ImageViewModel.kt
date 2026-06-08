package com.pdfscanner.editorapp.ui.viewmodels

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdfscanner.editorapp.R
import com.pdfscanner.editorapp.utils.PdfUtils
import com.pdfscanner.editorapp.utils.AnalyticsManager
import com.tom_roush.pdfbox.cos.COSBase
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.LayerUtility
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MediaImage(val id: Long, val uri: Uri, val name: String, val bucketName: String)

data class FolderInfo(val name: String, val count: Int, val thumbnailUri: Uri)

class ImageViewModel(application: Application) : AndroidViewModel(application) {
    private val _allImages = mutableListOf<MediaImage>()
    private val _images = MutableStateFlow<List<MediaImage>>(emptyList())
    val images: StateFlow<List<MediaImage>> = _images

    private val _folders = MutableStateFlow<List<FolderInfo>>(emptyList())
    val folders: StateFlow<List<FolderInfo>> = _folders

    private val prefs = application.getSharedPreferences("pdf_prefs", Context.MODE_PRIVATE)

    private val _favoritePdfPaths = MutableStateFlow<Set<String>>(emptySet())
    val favoritePdfPaths: StateFlow<Set<String>> = _favoritePdfPaths

    private val _priorityFilePath = MutableStateFlow<String?>(null)
    val priorityFilePath: StateFlow<String?> = _priorityFilePath.asStateFlow()

    fun setPriorityFile(path: String?) {
        _priorityFilePath.value = path
        if (path != null) {
            _recentPdfs.value = emptyList()
            _isLoadingPdfs.value = true
        }
    }

    private val _sortCriteria = MutableStateFlow(SortCriteria.DateModified)
    val sortCriteria: StateFlow<SortCriteria> = _sortCriteria

    private val _sortOrder = MutableStateFlow(SortOrder.Descending)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    private val _shouldFocusSearch = MutableStateFlow(false)
    val shouldFocusSearch: StateFlow<Boolean> = _shouldFocusSearch

    private val _importedPdfMap = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val _importedPdfPaths = MutableStateFlow<Set<String>>(emptySet())

    val importedPdfPaths: StateFlow<Set<String>> = _importedPdfPaths
    var isFromReorder by mutableStateOf(false)


    init {
        loadSortSettings()
        loadFavorites()
        loadImportedPaths()
        loadRecentPdfs()
    }

    private fun loadSortSettings() {
        val criteriaName = prefs.getString("sort_criteria", SortCriteria.DateCreated.name)
        val orderName = prefs.getString("sort_order", SortOrder.Descending.name)

        _sortCriteria.value = try {
            SortCriteria.valueOf(criteriaName ?: SortCriteria.DateCreated.name)
        } catch (e: Exception) {
            SortCriteria.DateCreated
        }
        
        _sortOrder.value = try {
            SortOrder.valueOf(orderName ?: SortOrder.Descending.name)
        } catch (e: Exception) {
            SortOrder.Descending
        }
    }


    private fun loadImportedPaths() {
        val mapStr = prefs.getString("imported_paths_map", "") ?: ""
        if (mapStr.isNotEmpty()) {
            val map = mutableMapOf<String, Long>()
            mapStr.split("|||").forEach { entry ->
                val parts = entry.split(";;;")
                if (parts.size == 2) {
                    map[parts[0]] = parts[1].toLongOrNull() ?: 0L
                }
            }
            _importedPdfMap.value = map
            _importedPdfPaths.value = map.keys
        } else {
            // Fallback for migration from older versions
            val pathsStr = prefs.getString("imported_paths_list", "") ?: ""
            val list = if (pathsStr.isEmpty()) emptyList() else pathsStr.split("|||")
            
            val map = mutableMapOf<String, Long>()
            val now = System.currentTimeMillis()
            list.forEachIndexed { index, path ->
                map[path] = now - (index * 1000L) 
            }
            _importedPdfMap.value = map
            _importedPdfPaths.value = map.keys
            saveImportedPdfMap(map)
        }
    }

    private fun saveImportedPdfMap(map: Map<String, Long>) {
        val str = map.entries.joinToString("|||") { "${it.key};;;${it.value}" }
        prefs.edit().putString("imported_paths_map", str).apply()
    }

    private fun addImportedPath(path: String) {
        val currentMap = _importedPdfMap.value.toMutableMap()
        currentMap[path] = System.currentTimeMillis()
        
        // Keep only top 50 recently imported files
        val limitedMap = currentMap.entries.sortedByDescending { it.value }
            .take(50)
            .associate { it.key to it.value }
            
        _importedPdfMap.value = limitedMap
        _importedPdfPaths.value = limitedMap.keys
        saveImportedPdfMap(limitedMap)
    }

    private fun removeImportedPath(path: String) {
        val currentMap = _importedPdfMap.value.toMutableMap()
        if (currentMap.containsKey(path)) {
            currentMap.remove(path)
            _importedPdfMap.value = currentMap
            _importedPdfPaths.value = currentMap.keys
            saveImportedPdfMap(currentMap)
        }
    }

    private fun replaceImportedPath(oldPath: String, newPath: String) {
        val currentMap = _importedPdfMap.value.toMutableMap()
        if (currentMap.containsKey(oldPath)) {
            val time = currentMap.remove(oldPath)!!
            currentMap[newPath] = time
            _importedPdfMap.value = currentMap
            _importedPdfPaths.value = currentMap.keys
            saveImportedPdfMap(currentMap)
        }
    }

    private fun saveSortSettings(criteria: SortCriteria, order: SortOrder) {
        prefs.edit().apply {
            putString("sort_criteria", criteria.name)
            putString("sort_order", order.name)
            apply()
        }
    }

    private fun loadFavorites() {
        val favs = prefs.getStringSet("favorite_pdfs", emptySet()) ?: emptySet()
        _favoritePdfPaths.value = favs
    }

    fun toggleFavorite(file: java.io.File) {
        val currentFavs = _favoritePdfPaths.value.toMutableSet()
        val path = file.absolutePath
        if (currentFavs.contains(path)) {
            currentFavs.remove(path)
        } else {
            currentFavs.add(path)
        }
        prefs.edit().putStringSet("favorite_pdfs", currentFavs).apply()
        _favoritePdfPaths.value = currentFavs
    }

    fun isFavorite(file: java.io.File): Boolean {
        return _favoritePdfPaths.value.contains(file.absolutePath)
    }

    private val _selectedImages = mutableStateListOf<MediaImage>()
    val selectedImages: List<MediaImage>
        get() = _selectedImages

    var currentFolder = mutableStateOf("All images")

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _previewPageIndex = MutableStateFlow(0)
    val previewPageIndex = _previewPageIndex.asStateFlow()

    fun updatePreviewPageIndex(index: Int) {
        _previewPageIndex.value = index
    }

    fun loadImages() {
        viewModelScope.launch {
            _isLoading.value = true
            val imageList =
                    withContext(Dispatchers.IO) {
                        val images = mutableListOf<MediaImage>()
                        val projection =
                                arrayOf(
                                        MediaStore.Images.Media._ID,
                                        MediaStore.Images.Media.DISPLAY_NAME,
                                        MediaStore.Images.Media.SIZE,
                                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                                )
                        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

                        getApplication<Application>()
                                .contentResolver
                                .query(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        projection,
                                        null,
                                        null,
                                        sortOrder
                                )
                                ?.use { cursor ->
                                    val idColumn =
                                            cursor.getColumnIndexOrThrow(
                                                    MediaStore.Images.Media._ID
                                            )
                                    val nameColumn =
                                            cursor.getColumnIndexOrThrow(
                                                    MediaStore.Images.Media.DISPLAY_NAME
                                            )
                                    val sizeColumn =
                                            cursor.getColumnIndexOrThrow(
                                                    MediaStore.Images.Media.SIZE
                                            )
                                    val bucketNameColumn =
                                            cursor.getColumnIndexOrThrow(
                                                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                                            )

                                    while (cursor.moveToNext()) {
                                        val id = cursor.getLong(idColumn)
                                        val name = cursor.getString(nameColumn)
                                        val size = cursor.getLong(sizeColumn)
                                        val bucketName =
                                                cursor.getString(bucketNameColumn) ?: "Unknown"

                                        if (size > 0) {
                                            val contentUri =
                                                    ContentUris.withAppendedId(
                                                            MediaStore.Images.Media
                                                                    .EXTERNAL_CONTENT_URI,
                                                            id
                                                    )
                                            images.add(MediaImage(id, contentUri, name, bucketName))
                                        }
                                    }
                                }
                        images
                    }
            _allImages.clear()
            _allImages.addAll(imageList)

            // Generate folder list
            val folderMap = mutableMapOf<String, Int>()
            val thumbnailMap = mutableMapOf<String, Uri>()

            imageList.forEach { image ->
                folderMap[image.bucketName] = folderMap.getOrDefault(image.bucketName, 0) + 1
                if (!thumbnailMap.containsKey(image.bucketName)) {
                    thumbnailMap[image.bucketName] = image.uri
                }
            }

            val folderList = mutableListOf<FolderInfo>()
            // "All images" item
            if (imageList.isNotEmpty()) {
                folderList.add(FolderInfo("All images", imageList.size, imageList[0].uri))
            }

            // Other folders
            folderMap.forEach { (name, count) ->
                folderList.add(FolderInfo(name, count, thumbnailMap[name]!!))
            }

            _folders.value = folderList

            filterImages(currentFolder.value)
            _isLoading.value = false
        }
    }

    private val _selectedPdfs = mutableStateListOf<java.io.File>()
    val selectedPdfs: List<java.io.File>
        get() = _selectedPdfs

    private val _sessionPasswords = MutableStateFlow<Map<String, String>>(emptyMap())
    val sessionPasswords: StateFlow<Map<String, String>> = _sessionPasswords

    fun setSessionPassword(filePath: String, password: String) {
        _sessionPasswords.value = _sessionPasswords.value + (filePath to password)
    }

    fun clearSessionPasswords() {
        _sessionPasswords.value = emptyMap()
    }

    fun togglePdfSelection(file: java.io.File) {
        if (_selectedPdfs.contains(file)) {
            _selectedPdfs.remove(file)
        } else {
            _selectedPdfs.add(file)
        }
    }

    fun movePdf(fromIndex: Int, toIndex: Int) {
        val item = _selectedPdfs.removeAt(fromIndex)
        _selectedPdfs.add(toIndex, item)
    }

    fun clearPdfSelection() {
        _selectedPdfs.clear()
    }

    fun setSelectedPdfs(files: List<java.io.File>) {
        _selectedPdfs.clear()
        _selectedPdfs.addAll(files)
    }

    fun filterImages(folder: String) {
        currentFolder.value = folder
        if (folder == "All images") {
            _images.value = _allImages.toList() // Force UI update with a new list instance
        } else {
            _images.value = _allImages.filter { it.bucketName == folder }
        }
    }

    fun toggleSelection(image: MediaImage) {
        if (_selectedImages.contains(image)) {
            _selectedImages.remove(image)
        } else {
            _selectedImages.add(image)
        }
    }

    fun addCapturedImage(image: MediaImage) {
        _selectedImages.add(image)
        _allImages.add(0, image)
        filterImages(currentFolder.value)
    }

    fun clearSelectedImages() {
        _selectedImages.clear()
    }

    private val _backupSelectedImages = mutableListOf<MediaImage>()

    fun backupSelectedImages() {
        _backupSelectedImages.clear()
        _backupSelectedImages.addAll(_selectedImages)
    }

    fun restoreSelectedImages() {
        _selectedImages.clear()
        _selectedImages.addAll(_backupSelectedImages)
        _backupSelectedImages.clear()
    }

    private val _isCreatingPdf = MutableStateFlow(false)
    val isCreatingPdf: StateFlow<Boolean> = _isCreatingPdf
    fun createPdfAndNavigate(
            context: Context,
            uris: List<Uri>,
            isPreview: Boolean = false,
            onReady: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCreatingPdf.value = true
            try {
                val pdfDocument = com.tom_roush.pdfbox.pdmodel.PDDocument()
                val quality = _compressionQuality.value
                val useMarginSwitch = _isPageMarginEnabled.value
                val frameName = _selectedFrame.value.name
                val password = _pdfPassword.value

                // A4 size in points (1 point = 1/72 inch)
                // A4 = 595 x 842 points
                val pageWidth = com.tom_roush.pdfbox.pdmodel.common.PDRectangle.A4.width
                val pageHeight = com.tom_roush.pdfbox.pdmodel.common.PDRectangle.A4.height

                // Calculate Grid Dimensions
                val (rows, cols) =
                        when (frameName) {
                            "2×1" -> 2 to 1
                            "1×2" -> 1 to 2
                            "2×2" -> 2 to 2
                            "2×3" -> 2 to 3
                            "3×2" -> 3 to 2
                            "3×3" -> 3 to 3
                            else -> 1 to 1
                        }

                val itemsPerPage = rows * cols
                val imageChunks = uris.chunked(itemsPerPage)

                // Margin Logic (in points)
                val marginPoints = if (useMarginSwitch) 20f else 0f

                // Frame Padding Logic (Internal padding between images)
                val framePaddingPoints =
                        when (frameName) {
                            "Narrow" -> 20f
                            "Normal" -> 10f
                            else -> 0f
                        }
                val spacingPoints = 8f

                imageChunks.forEachIndexed { _, pageUris ->
                    val page =
                            com.tom_roush.pdfbox.pdmodel.PDPage(
                                    com.tom_roush.pdfbox.pdmodel.common.PDRectangle.A4
                            )
                    pdfDocument.addPage(page)
                    val contentStream =
                            com.tom_roush.pdfbox.pdmodel.PDPageContentStream(pdfDocument, page)

                    // Calculate available drawing area (considering global page margin)
                    val contentWidth = pageWidth - (marginPoints * 2)
                    val contentHeight = pageHeight - (marginPoints * 2)

                    val startX = marginPoints
                    val startY = pageHeight - marginPoints

                    // Calculate cell size
                    val cellWidth =
                            if (cols > 1) {
                                (contentWidth - (cols - 1) * spacingPoints) / cols
                            } else {
                                contentWidth
                            }
                    val cellHeight =
                            if (rows > 1) {
                                (contentHeight - (rows - 1) * spacingPoints) / rows
                            } else {
                                contentHeight
                            }

                    pageUris.forEachIndexed { index, uri ->
                        val rowIndex = index / cols
                        val colIndex = index % cols

                        // Position of this cell (Top-Left based calculation converted to
                        // Bottom-Left)
                        val cellLeft = startX + colIndex * (cellWidth + spacingPoints)

                        // Top of the cell
                        val cellTopY = startY - rowIndex * (cellHeight + spacingPoints)
                        // Bottom of the cell
                        val cellBottomY = cellTopY - cellHeight

                        // Apply Frame Padding
                        val imageDrawLeft = cellLeft + framePaddingPoints
                        val imageDrawBottom = cellBottomY + framePaddingPoints

                        var imageDrawWidth = cellWidth - (framePaddingPoints * 2)
                        var imageDrawHeight = cellHeight - (framePaddingPoints * 2)

                        if (imageDrawWidth < 0) imageDrawWidth = 0f
                        if (imageDrawHeight < 0) imageDrawHeight = 0f

                        // Load Bitmap with Downsampling
                        var inputStream = context.contentResolver.openInputStream(uri)
                        val options =
                                android.graphics.BitmapFactory.Options().apply {
                                    inJustDecodeBounds = true
                                }
                        android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
                        inputStream?.close()

                        // Calculate inSampleSize
                        options.inSampleSize =
                                calculateInSampleSize(options, 1536, 1536) // Max 1536x1536
                        options.inJustDecodeBounds = false

                        inputStream = context.contentResolver.openInputStream(uri)
                        var bitmap =
                                android.graphics.BitmapFactory.decodeStream(
                                        inputStream,
                                        null,
                                        options
                                )
                        inputStream?.close()

                        bitmap?.let { originalBitmap ->
                            var finalBitmap = originalBitmap
                            // Apply Compression if needed
                            if (quality < 100) {
                                val out = java.io.ByteArrayOutputStream()
                                originalBitmap.compress(
                                        android.graphics.Bitmap.CompressFormat.JPEG,
                                        quality,
                                        out
                                )
                                val decoded =
                                        android.graphics.BitmapFactory.decodeStream(
                                                java.io.ByteArrayInputStream(out.toByteArray())
                                        )
                                if (decoded != null) {
                                    finalBitmap = decoded
                                }
                            }

                            val pdImage =
                                    com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
                                            .createFromImage(pdfDocument, finalBitmap)

                            // Scaling Logic (Fit Center)
                            val bitmapRatio = pdImage.width.toFloat() / pdImage.height.toFloat()
                            val targetRatio = imageDrawWidth / imageDrawHeight

                            var drawWidth = imageDrawWidth
                            var drawHeight = imageDrawHeight

                            if (bitmapRatio > targetRatio) {
                                // Bitmap is wider -> fit width
                                drawHeight = drawWidth / bitmapRatio
                            } else {
                                // Bitmap is taller/same -> fit height
                                drawWidth = drawHeight * bitmapRatio
                            }

                            // Center image
                            val drawX = imageDrawLeft + (imageDrawWidth - drawWidth) / 2f
                            val drawY = imageDrawBottom + (imageDrawHeight - drawHeight) / 2f

                            contentStream.drawImage(pdImage, drawX, drawY, drawWidth, drawHeight)

                            // Recycle bitmaps immediately
                            if (finalBitmap != originalBitmap) {
                                finalBitmap.recycle()
                            }
                            originalBitmap.recycle()
                        }
                    }
                    contentStream.close()
                }

                // Password Protection
                if (!isPreview && password.isNotEmpty()) {
                    val ap = com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission()
                    val spp =
                            com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy(
                                    password,
                                    password,
                                    ap
                            )
                    spp.encryptionKeyLength = 128
                    pdfDocument.protect(spp)
                }

                // File Saving Logic
                val fileName =
                        if (isPreview) {
                            "preview.pdf"
                        } else {
                            "${_pdfFilename.value.trim().ifEmpty { "PDFlex Scanner Editor" }}.pdf"
                        }

                val file =
                        if (isPreview) {
                            java.io.File(getApplication<Application>().cacheDir, fileName)
                        } else {
                            // Save to Documents/ImageToPdf
                            java.io.File(getAppDirectory(), fileName)
                        }

                // Navigate immediately if not preview
                withContext(Dispatchers.Main) {
                    if (!isPreview) {
                        onReady(file.absolutePath)
                        // Reset fields immediately so UI is clean when user returns
                        resetPdfSettings()
                    }
                }

                pdfDocument.save(file)
                pdfDocument.close()

                withContext(Dispatchers.Main) {
                    _isCreatingPdf.value = false
                    if (isPreview) {
                        onReady(file.absolutePath)
                    } else {
                        AnalyticsManager.logEventWithAction(
                            eventName = "feature_image_to_pdf",
                            screenName = "ImageViewModel",
                            action = "Image_to_PDF_Success"
                        )
                        scanFile(file)
                        loadRecentPdfs()
                    }
                }
            } catch (e: Throwable) {
                _isCreatingPdf.value = false
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (!isPreview) {
                        AnalyticsManager.logEventWithAction(
                            eventName = "feature_image_to_pdf",
                            screenName = "ImageViewModel",
                            action = "Image_to_PDF_Failed",
                            extraParams = mapOf(
                                "error" to (e.message ?: "Unknown Error")
                            )
                        )
                    }
                    android.widget.Toast.makeText(
                                    context,
                                    "Error creating PDF: ${e.message}",
                                    android.widget.Toast.LENGTH_LONG
                            )
                            .show()
                }
            }
        }
    }

    fun importPdf(uri: Uri, context: Context, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoadingPdfs.value = true
            try {
                val finalFile =
                        withContext(Dispatchers.IO) {
                            var realPath: String? = null
                            if ("content".equals(uri.scheme, ignoreCase = true)) {
                                try {
                                    val projection = arrayOf(android.provider.MediaStore.MediaColumns.DATA)
                                    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                                        if (cursor.moveToFirst()) {
                                            val columnIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA)
                                            if (columnIndex != -1) {
                                                realPath = cursor.getString(columnIndex)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                                realPath = uri.path
                            }

                            if (realPath != null && File(realPath).exists()) {
                                File(realPath)
                            } else {
                                val contentResolver = context.contentResolver
                                var displayName = "imported_document_${System.currentTimeMillis()}.pdf"

                                // Try to get original filename
                                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                    if (cursor.moveToFirst()) {
                                        val nameIndex =
                                                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                        if (nameIndex != -1) {
                                            displayName = cursor.getString(nameIndex)
                                        }
                                    }
                                }

                                if (!displayName.lowercase().endsWith(".pdf")) {
                                    displayName += ".pdf"
                                }

                                val outputDir = getAppDirectory()

                                var outputFile = File(outputDir, displayName)
                                var counter = 1
                                val baseName = displayName.substringBeforeLast(".")
                                while (outputFile.exists()) {
                                    outputFile = File(outputDir, "${baseName}_$counter.pdf")
                                    counter++
                                }

                                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                                if (inputStream != null) {
                                    val outputStream = FileOutputStream(outputFile)
                                    inputStream.copyTo(outputStream)
                                    inputStream.close()
                                    outputStream.close()
                                } else {
                                    throw Exception("Failed to open input stream from selected file")
                                }
                                outputFile // Return the file from withContext
                            }
                        }

                withContext(Dispatchers.Main) {
                    scanFile(finalFile)
                    
                    // Add to imported paths so it shows up in Home Screen's recent list
                    addImportedPath(finalFile.absolutePath)
                    
                    // Set as priority file so it appears at the top
                    setPriorityFile(finalFile.absolutePath)
                    
                    // Clear lists to trigger a "hard reload" in the UI, resetting the scroll position to top
                    _recentPdfs.value = emptyList()
                    _createdPdfs.value = emptyList()
                    
                    loadRecentPdfs()
                    _isLoadingPdfs.value = false
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_import_pdf",
                        screenName = "ImageViewModel",
                        action = "Import_Success"
                    )
                    onSuccess(finalFile.absolutePath)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoadingPdfs.value = false
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_import_pdf",
                        screenName = "ImageViewModel",
                        action = "Import_Failed",
                        extraParams = mapOf("error" to (e.message ?: "Unknown"))
                    )
                    onError(e.message ?: "Failed to import PDF")
                }
            }
        }
    }

    fun mergePdfs(
        outputFileName: String,
        passwords: Map<String, String> = emptyMap(),
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_selectedPdfs.size < 2) {
            onError("At least 2 PDFs are required to merge.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val tempUnlockedFiles = mutableListOf<File>()
            try {
                _isCreatingPdf.value = true
                val merger = com.tom_roush.pdfbox.multipdf.PDFMergerUtility()
                val appDir = getAppDirectory("Merge")
                
                // Sanitize filename to prevent file system errors
                val sanitizedFileName = outputFileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                val mergedFile = java.io.File(appDir, "$sanitizedFileName.pdf")
                merger.destinationFileName = mergedFile.absolutePath

                for (pdfFile in _selectedPdfs) {
                    val password = passwords[pdfFile.absolutePath]
                    if (password != null) {
                        // Create temporary unlocked copy for merging
                        val unlockedFile = createUnlockedPdfCopy(pdfFile, password)
                        if (unlockedFile != null) {
                            tempUnlockedFiles.add(unlockedFile)
                            merger.addSource(unlockedFile)
                        } else {
                            merger.addSource(pdfFile) // Fallback, though it might fail if still locked
                        }
                    } else {
                        merger.addSource(pdfFile)
                    }
                }

                // Use setupTempFileOnly for better memory management on Android
                merger.mergeDocuments(
                    com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly()
                )

                try {
                    PDDocument.load(mergedFile, MemoryUsageSetting.setupTempFileOnly()).use { doc ->
                        doc.documentInformation.setCustomMetadataValue("PDFlex-Type", "Merged")
                        doc.save(mergedFile)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Cleanup temp files
                tempUnlockedFiles.forEach { it.delete() }

                scanFile(mergedFile)
                loadRecentPdfs() // Refresh

                withContext(Dispatchers.Main) { 
                    _isCreatingPdf.value = false
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_merge_pdf",
                        screenName = "ImageViewModel",
                        action = "Merge_Success"
                    )
                    onSuccess(mergedFile.absolutePath) 
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                tempUnlockedFiles.forEach { it.delete() }
                withContext(Dispatchers.Main) { 
                    _isCreatingPdf.value = false
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_merge_pdf",
                        screenName = "ImageViewModel",
                        action = "Merge_Failed",
                        extraParams = mapOf(
                            "error" to (e.message ?: "Unknown Error")
                        )
                    )
                    onError(e.message ?: "Failed to merge PDFs") 
                }
            }
        }
    }

    fun applyFrameToPdf(
        context: Context,
        file: File,
        frame: FrameOption,
        password: String? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCreatingPdf.value = true
            try {
                val sourceDoc = if (password != null) {
                    PDDocument.load(file, password, MemoryUsageSetting.setupTempFileOnly())
                } else {
                    PDDocument.load(file, MemoryUsageSetting.setupTempFileOnly())
                }
                val targetDoc = PDDocument()
                val layerUtility = LayerUtility(targetDoc)

                val pageWidth = PDRectangle.A4.width
                val pageHeight = PDRectangle.A4.height

                val (rows, cols) = when (frame.name) {
                    "2×1" -> 2 to 1
                    "1×2" -> 1 to 2
                    "2×2" -> 2 to 2
                    "2×3" -> 2 to 3
                    "3×2" -> 3 to 2
                    "3×3" -> 3 to 3
                    else -> 1 to 1
                }

                val itemsPerPage = rows * cols
                val marginPoints = when (frame.name) {
                    "Narrow" -> 40f
                    "Normal" -> 20f
                    else -> 0f
                }
                val spacingPoints = 10f

                val totalPages = sourceDoc.numberOfPages
                for (i in 0 until totalPages step itemsPerPage) {
                    val targetPage = PDPage(PDRectangle.A4)
                    targetDoc.addPage(targetPage)
                    val contentStream = PDPageContentStream(targetDoc, targetPage)

                    val contentWidth = pageWidth - (marginPoints * 2)
                    val contentHeight = pageHeight - (marginPoints * 2)

                    val cellWidth = if (cols > 1) (contentWidth - (cols - 1) * spacingPoints) / cols else contentWidth
                    val cellHeight = if (rows > 1) (contentHeight - (rows - 1) * spacingPoints) / rows else contentHeight

                    for (j in 0 until itemsPerPage) {
                        val pageIdx = i + j
                        if (pageIdx >= totalPages) break

                        val sourcePage = sourceDoc.getPage(pageIdx)
                        val form = layerUtility.importPageAsForm(sourceDoc, sourcePage)

                        val rowIndex = j / cols
                        val colIndex = j % cols

                        val cellLeft = marginPoints + colIndex * (cellWidth + spacingPoints)
                        val cellTopY = (pageHeight - marginPoints) - rowIndex * (cellHeight + spacingPoints)
                        val cellBottomY = cellTopY - cellHeight

                        val formWidth = form.bBox.width
                        val formHeight = form.bBox.height
                        val formRatio = formWidth / formHeight
                        val cellRatio = cellWidth / cellHeight

                        var drawWidth = cellWidth
                        var drawHeight = cellHeight

                        if (formRatio > cellRatio) {
                            drawHeight = drawWidth / formRatio
                        } else {
                            drawWidth = drawHeight * formRatio
                        }

                        val drawX = cellLeft + (cellWidth - drawWidth) / 2f
                        val drawY = cellBottomY + (cellHeight - drawHeight) / 2f

                        contentStream.saveGraphicsState()
                        contentStream.transform(com.tom_roush.pdfbox.util.Matrix.getTranslateInstance(drawX, drawY))
                        contentStream.transform(com.tom_roush.pdfbox.util.Matrix.getScaleInstance(drawWidth / formWidth, drawHeight / formHeight))
                        contentStream.drawForm(form)
                        contentStream.restoreGraphicsState()
                    }
                    contentStream.close()
                }

                val fileName = "${file.nameWithoutExtension}_framed.pdf"
                val outputFile = File(getAppDirectory(), fileName)
                
                targetDoc.save(outputFile)
                targetDoc.close()
                sourceDoc.close()

                withContext(Dispatchers.Main) {
                    _isCreatingPdf.value = false
                    scanFile(outputFile)
                    
                    // Clear lists to trigger refresh
                    _recentPdfs.value = emptyList()
                    _createdPdfs.value = emptyList()
                    
                    loadRecentPdfs()
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_file_operations",
                        screenName = "ImageViewModel",
                        action = "Apply_Frame_Success",
                        extraParams = mapOf("frame_type" to frame.name)
                    )
                    onSuccess(outputFile.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isCreatingPdf.value = false
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_file_operations",
                        screenName = "ImageViewModel",
                        action = "Apply_Frame_Failed",
                        extraParams = mapOf("error" to (e.message ?: "Unknown"))
                    )
                    onError(e.message ?: "Failed to apply frame")
                }
            }
        }
    }

    private fun calculateInSampleSize(
            options: android.graphics.BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun clearAllSelections(onComplete: () -> Unit) {
        _selectedImages.clear()
        _selectedPdfs.clear()
        clearSessionPasswords()
        onComplete()
    }

    private val _selectedFrame =
            MutableStateFlow(FrameOption("None", com.pdfscanner.editorapp.R.drawable.frame_none))
    val selectedFrame: StateFlow<FrameOption> = _selectedFrame

    fun updateFrame(frame: FrameOption) {
        _selectedFrame.value = frame
    }

    private var _imageToCrop: MediaImage? = null

    fun setImageToCrop(image: MediaImage) {
        _imageToCrop = image
    }

    fun getImageToCrop(): MediaImage? = _imageToCrop

    fun updateCroppedImage(originalId: Long, newUri: Uri) {
        val index = _selectedImages.indexOfFirst { it.id == originalId }
        if (index != -1) {
            _selectedImages[index] = _selectedImages[index].copy(uri = newUri)
        }
    }

    private val _pdfFilename = MutableStateFlow("")
    val pdfFilename: StateFlow<String> = _pdfFilename

    fun updatePdfFilename(name: String) {
        _pdfFilename.value = name
    }

    private val _pdfPassword = MutableStateFlow("")
    val pdfPassword: StateFlow<String> = _pdfPassword

    fun updatePdfPassword(password: String) {
        _pdfPassword.value = password
    }

    private val _isPageMarginEnabled = MutableStateFlow(false)
    val isPageMarginEnabled: StateFlow<Boolean> = _isPageMarginEnabled

    fun togglePageMargin(isEnabled: Boolean) {
        _isPageMarginEnabled.value = isEnabled
    }

    // Compression: 0 to 100, where 100 is original quality
    private val _compressionQuality = MutableStateFlow(80)
    val compressionQuality: StateFlow<Int> = _compressionQuality

    fun updateCompressionQuality(quality: Int) {
        _compressionQuality.value = quality
    }

    fun resetPdfSettings() {
        _pdfFilename.value = ""
        _compressionQuality.value = 80
        _pdfPassword.value = ""
        _isPageMarginEnabled.value = false
        _selectedFrame.value = FrameOption("None", R.drawable.frame_none)
        _previewPageIndex.value = 0
    }

    fun isFileExists(filename: String): Boolean {
        val appDir = getAppDirectory()
        val file = java.io.File(appDir, "$filename.pdf")
        return file.exists()
    }


    fun setSearchActive(isActive: Boolean) {
        _isSearchActive.value = isActive
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        loadRecentPdfs()
    }

    fun triggerSearchFocus() {
        _shouldFocusSearch.value = true
    }

    fun consumeSearchFocus() {
        _shouldFocusSearch.value = false
    }

    fun saveScannedPdf(
            context: android.content.Context,
            sourceUri: android.net.Uri,
            fileName: String,
            onSuccess: (File) -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCreatingPdf.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                if (inputStream == null) {
                    withContext(Dispatchers.Main) { onError("Could not read scanned PDF") }
                    return@launch
                }

                val appDir = getAppDirectory()
                if (!appDir.exists()) appDir.mkdirs()

                // Ensure the filename ends with .pdf
                val finalFileName =
                        if (fileName.endsWith(".pdf", ignoreCase = true)) fileName
                        else "$fileName.pdf"

                // Ensure unique name if file already exists
                var destFile = java.io.File(appDir, finalFileName)
                var counter = 1
                val nameWithoutExt = finalFileName.substringBeforeLast(".")
                while (destFile.exists()) {
                    destFile = java.io.File(appDir, "${nameWithoutExt}_$counter.pdf")
                    counter++
                }

                destFile.outputStream().use { output -> inputStream.copyTo(output) }
                inputStream.close()

                withContext(Dispatchers.Main) {
                    scanFile(destFile)
                    loadRecentPdfs()
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_smart_scan",
                        screenName = "ImageViewModel",
                        action = "Scan_Save_Success"
                    )
                    onSuccess(destFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_smart_scan",
                        screenName = "ImageViewModel",
                        action = "Scan_Save_Failed",
                        extraParams = mapOf("error" to (e.message ?: "Unknown"))
                    )
                    onError(e.message ?: "Failed to save scanned PDF")
                }
            } finally {
                _isCreatingPdf.value = false
            }
        }
    }

    private fun scanFile(file: File) {
        MediaScannerConnection.scanFile(
                getApplication(),
                arrayOf(file.absolutePath),
                arrayOf("application/pdf"),
                null
        )
    }

    private val _recentPdfs = MutableStateFlow<List<java.io.File>>(emptyList())
    val recentPdfs: StateFlow<List<java.io.File>> = _recentPdfs

    private val _createdPdfs = MutableStateFlow<List<java.io.File>>(emptyList())
    val createdPdfs: StateFlow<List<java.io.File>> = _createdPdfs

    private val _fileUpdateRefresh = MutableStateFlow<Map<String, Long>>(emptyMap())
    val fileUpdateRefresh = _fileUpdateRefresh.asStateFlow()

    fun triggerFileRefresh(path: String) {
        _fileUpdateRefresh.update { it + (path to System.currentTimeMillis()) }
    }

    private val _isLoadingPdfs = MutableStateFlow(true)
    val isLoadingPdfs: StateFlow<Boolean> = _isLoadingPdfs

    private var loadPdfsJob: kotlinx.coroutines.Job? = null

    fun loadRecentPdfs() {
        loadPdfsJob?.cancel()
        loadPdfsJob =
                viewModelScope.launch(Dispatchers.IO) {
                    if (_recentPdfs.value.isEmpty()) {
                        _isLoadingPdfs.value = true
                        kotlinx.coroutines.delay(100)
                    }

                    val allSystemPdfs = mutableListOf<File>()
                    val appCreatedPdfs = mutableListOf<File>()

                    val projection =
                            arrayOf(
                                    MediaStore.Files.FileColumns.DATA,
                                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                                    MediaStore.Files.FileColumns.SIZE
                            )

                    val selection =
                            "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
                    val selectionArgs = arrayOf("application/pdf", "%.pdf", "%.pdf")

                    // Determine sort order for the query
                    val mediaStoreSortOrder =
                            when (_sortCriteria.value) {
                                SortCriteria.Name -> "${MediaStore.Files.FileColumns.DISPLAY_NAME} "
                                SortCriteria.Size -> "${MediaStore.Files.FileColumns.SIZE} "
                                SortCriteria.DateCreated ->
                                        "${MediaStore.Files.FileColumns.DATE_ADDED} "
                                else -> "${MediaStore.Files.FileColumns.DATE_MODIFIED} "
                            } +
                                    when (_sortOrder.value) {
                                        SortOrder.Ascending -> "ASC"
                                        SortOrder.Descending -> "DESC"
                                    }

                    // 1. Get files from MediaStore
                    getApplication<Application>()
                            .contentResolver
                            .query(
                                    MediaStore.Files.getContentUri("external"),
                                    projection,
                                    selection,
                                    selectionArgs,
                                    mediaStoreSortOrder
                            )
                            ?.use { cursor ->
                                val dataColumn =
                                        cursor.getColumnIndexOrThrow(
                                                MediaStore.Files.FileColumns.DATA
                                        )

                                while (cursor.moveToNext()) {
                                    val path = cursor.getString(dataColumn)
                                    if (path != null) {
                                        val file = File(path)
                                        if (file.exists()) {
                                            // Apply search filter if query is not empty
                                            val matchesSearch =
                                                    _searchQuery.value.isEmpty() ||
                                                            file.name.contains(
                                                                    _searchQuery.value,
                                                                    ignoreCase = true
                                                            )

                                            if (matchesSearch) {
                                                allSystemPdfs.add(file)
                                                if (path.contains(
                                                                "PDFlex Scanner Editor",
                                                                ignoreCase = true
                                                        ) || _importedPdfPaths.value.contains(path)
                                                ) {
                                                    appCreatedPdfs.add(file)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                    // 2. Manual fallback for key folders (ensures visibility after
                    // reinstall/latency)
                    val searchDirs =
                            listOf(
                                    getAppDirectory(),
                                    android.os.Environment.getExternalStoragePublicDirectory(
                                            android.os.Environment.DIRECTORY_DOWNLOADS
                                    ),
                                    android.os.Environment.getExternalStoragePublicDirectory(
                                            android.os.Environment.DIRECTORY_DOCUMENTS
                                    )
                            )

                    for (dir in searchDirs) {
                        scanFolderForPdfs(dir, 0, allSystemPdfs, appCreatedPdfs)
                    }

                    // 3. Deduplicate and sort to maintain consistency
                    val finalAllPdfs = allSystemPdfs.distinctBy { it.absolutePath }.toMutableList()
                    val finalCreatedPdfs = appCreatedPdfs.distinctBy { it.absolutePath }.toMutableList()

                    sortPdfList(finalAllPdfs, isCreatedList = false)
                    sortPdfList(finalCreatedPdfs, isCreatedList = true)

                    withContext(Dispatchers.Main) {
                        _recentPdfs.value = finalAllPdfs
                        _createdPdfs.value = finalCreatedPdfs
                        _isLoadingPdfs.value = false
                    }
                }
    }


    fun updateSortSettings(criteria: SortCriteria, order: SortOrder) {
        _sortCriteria.value = criteria
        _sortOrder.value = order
        saveSortSettings(criteria, order)
        
        // Clear lists to trigger a "hard reload" in the UI, resetting the scroll position
        _recentPdfs.value = emptyList()
        _createdPdfs.value = emptyList()
        
        loadRecentPdfs() // Reload/Resort
    }

    private fun sortPdfList(list: MutableList<File>, isCreatedList: Boolean = false) {
        val criteria = _sortCriteria.value
        val order = _sortOrder.value

        list.sortWith { f1, f2 ->
            val result = when (criteria) {
                SortCriteria.Name -> f1.name.lowercase().compareTo(f2.name.lowercase())
                SortCriteria.Size -> {
                    val res = f1.length().compareTo(f2.length())
                    if (res != 0) res else f1.name.lowercase().compareTo(f2.name.lowercase())
                }
                SortCriteria.DateCreated, SortCriteria.DateModified -> {
                    // Use the custom import timestamp if available, otherwise file timestamp
                    val time1 = _importedPdfMap.value[f1.absolutePath] ?: f1.lastModified()
                    val time2 = _importedPdfMap.value[f2.absolutePath] ?: f2.lastModified()
                    val res = time1.compareTo(time2)
                    if (res != 0) res else f1.name.lowercase().compareTo(f2.name.lowercase())
                }
            }
            if (order == SortOrder.Ascending) result else -result
        }

        // Single priority file overrides everything briefly for visual feedback
        _priorityFilePath.value?.let { priorityPath ->
            val index = list.indexOfFirst { it.absolutePath == priorityPath }
            if (index != -1) {
                val file = list.removeAt(index)
                list.add(0, file)
            }
        }
    }

    fun renamePdf(
            file: java.io.File,
            newName: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val validName = newName.trim()
            if (validName.isEmpty()) {
                withContext(Dispatchers.Main) { onError("Name cannot be empty") }
                return@launch
            }
            if (validName.equals(file.nameWithoutExtension, ignoreCase = true)) {
                withContext(Dispatchers.Main) { onSuccess() } // No change
                return@launch
            }

            val parentDir = file.parentFile
            val newFile = java.io.File(parentDir, "$validName.pdf")

            if (newFile.exists()) {
                withContext(Dispatchers.Main) { onError("File with this name already exists") }
                return@launch
            }

            val oldPath = file.absolutePath
            if (file.renameTo(newFile)) {
                // Update favorite if needed
                val currentFavs = _favoritePdfPaths.value.toMutableSet()
                if (currentFavs.contains(oldPath)) {
                    currentFavs.remove(oldPath)
                    currentFavs.add(newFile.absolutePath)
                    prefs.edit().putStringSet("favorite_pdfs", currentFavs).apply()
                    _favoritePdfPaths.value = currentFavs
                }

                replaceImportedPath(oldPath, newFile.absolutePath)

                scanFile(newFile)
                withContext(Dispatchers.Main) {
                    loadRecentPdfs() // Refresh list
                    onSuccess()
                }
            } else {
                withContext(Dispatchers.Main) { onError("Failed to rename file") }
            }
        }
    }

    fun deletePdf(file: java.io.File, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pathToDelete = file.absolutePath
            if (file.exists() && file.delete()) {
                // Remove from favorites if it was there
                val currentFavs = _favoritePdfPaths.value.toMutableSet()
                if (currentFavs.contains(pathToDelete)) {
                    currentFavs.remove(pathToDelete)
                    prefs.edit().putStringSet("favorite_pdfs", currentFavs).apply()
                    _favoritePdfPaths.value = currentFavs
                }

                removeImportedPath(pathToDelete)

                withContext(Dispatchers.Main) {
                    loadRecentPdfs() // Refresh list
                    onSuccess()
                }
            } else {
                withContext(Dispatchers.Main) { onError("Failed to delete file") }
            }
        }
    }

    fun setPdfPassword(
            file: java.io.File,
            password: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(file, com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly())
                val ap = com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission()
                val spp =
                        com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy(
                                password,
                                password,
                                ap
                        )
                spp.encryptionKeyLength = 128
                document.protect(spp)

                val tempFile = java.io.File(file.parent, file.name + ".tmp")
                document.save(tempFile)
                document.close()

                if (tempFile.renameTo(file)) {
                    // Update MediaStore in background
                    scanFile(file)
                    withContext(Dispatchers.Main) {
                        // Refresh in-place to maintain position
                        val newFile = File(file.absolutePath) // New instance to ensure update
                        _recentPdfs.value =
                                _recentPdfs.value.map {
                                    if (it.absolutePath == file.absolutePath) newFile else it
                                }
                        _createdPdfs.value =
                                _createdPdfs.value.map {
                                    if (it.absolutePath == file.absolutePath) newFile else it
                                }
                        triggerFileRefresh(file.absolutePath)
                        onSuccess()
                    }
                } else {
                    tempFile.delete()
                    withContext(Dispatchers.Main) { onError("Failed to save protected PDF") }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError("Error: ${e.message}") }
            }
        }
    }

    fun removePdfPassword(
            file: java.io.File,
            password: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(file, password, com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly())
                document.isAllSecurityToBeRemoved = true
                val tempFile = java.io.File(file.parent, file.name + ".tmp")
                document.save(tempFile)
                document.close()

                if (tempFile.renameTo(file)) {
                    // Update MediaStore in background
                    scanFile(file)
                    withContext(Dispatchers.Main) {
                        // Refresh in-place to maintain position
                        val newFile = File(file.absolutePath) // New instance to ensure update
                        _recentPdfs.value =
                                _recentPdfs.value.map {
                                    if (it.absolutePath == file.absolutePath) newFile else it
                                }
                        _createdPdfs.value =
                                _createdPdfs.value.map {
                                    if (it.absolutePath == file.absolutePath) newFile else it
                                }
                        triggerFileRefresh(file.absolutePath)
                        onSuccess()
                    }
                } else {
                    tempFile.delete()
                    withContext(Dispatchers.Main) { onError("Failed to save unprotected PDF") }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                val msg = e.message?.lowercase() ?: ""
                val errorMsg =
                        if (msg.contains("password")) {
                            "Incorrect password"
                        } else {
                            "Error unlocking PDF"
                        }
                withContext(Dispatchers.Main) { onError(errorMsg) }
            }
        }
    }
    var pendingPdfPath by mutableStateOf<String?>(null)
    var showGlobalPasswordDialog by mutableStateOf(false)
    var onPasswordCheckSuccess by mutableStateOf<((String, String?) -> Unit)?>(null)

    fun openPdfWithCheck(path: String, context: Context, onClearToNavigate: (String, String?) -> Unit) {
        viewModelScope.launch {
            val isProtected =
                    withContext(Dispatchers.IO) {
                        try {
                            if (path.startsWith("content://")) {
                                PdfUtils.isPasswordProtected(context, Uri.parse(path))
                            } else {
                                PdfUtils.isPasswordProtected(java.io.File(path))
                            }
                        } catch (e: Exception) {
                            false
                        }
                    }

            if (!isProtected) {
                onClearToNavigate(path, null)
            } else {
                pendingPdfPath = path
                onPasswordCheckSuccess = onClearToNavigate
                showGlobalPasswordDialog = true
            }
        }
    }

    fun onGlobalPasswordConfirmed(context: Context, password: String, onError: (String) -> Unit) {
        val path = pendingPdfPath ?: return
        viewModelScope.launch {
            val isValid =
                    withContext(Dispatchers.IO) {
                        try {
                            if (path.startsWith("content://")) {
                                val inputStream =
                                        context.contentResolver.openInputStream(Uri.parse(path))
                                if (inputStream != null) {
                                    val document =
                                            com.tom_roush.pdfbox.pdmodel.PDDocument.load(
                                                    inputStream,
                                                    password
                                            )
                                    document.close()
                                    inputStream.close()
                                    true
                                } else false
                            } else {
                                val document =
                                        com.tom_roush.pdfbox.pdmodel.PDDocument.load(
                                                java.io.File(path),
                                                password
                                        )
                                document.close()
                                true
                            }
                        } catch (e: Exception) {
                            false
                        }
                    }

            if (isValid) {
                showGlobalPasswordDialog = false
                onPasswordCheckSuccess?.invoke(path, password)
                pendingPdfPath = null
                onPasswordCheckSuccess = null
            } else {
                onError("Invalid password")
            }
        }
    }

    fun validatePdfPassword(
            file: File,
            password: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(file, password, com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly())
                document.close()
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Invalid Password") }
            }
        }
    }

    fun validatePdfPassword(
            context: android.content.Context,
            uri: android.net.Uri,
            password: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val document =
                            com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputStream, password, com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly())
                    document.close()
                    inputStream.close()
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    withContext(Dispatchers.Main) { onError("Failed to open PDF") }
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Invalid Password") }
            }
        }
    }

    fun compressPdf(
            file: File,
            quality: Int,
            password: String? = null,
            onProgress: (Int) -> Unit = {},
            onSuccess: (File) -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document =
                        if (password != null) {
                            PDDocument.load(file, password, MemoryUsageSetting.setupTempFileOnly())
                        } else {
                            PDDocument.load(file, MemoryUsageSetting.setupTempFileOnly())
                        }
                document.isAllSecurityToBeRemoved = true
                val totalPages = document.numberOfPages
                
                // Track processed image objects to avoid re-compressing duplicates
                val processedImages = mutableMapOf<COSBase, PDImageXObject>()
                
                // Determine max dimension based on quality (Standard Professional thresholds)
                val maxDimension = when {
                    quality >= 80 -> 1600 // High Quality
                    quality >= 50 -> 1200 // Medium Quality
                    else -> 800          // Low Quality
                }

                for ((index, page) in document.pages.withIndex()) {
                    val progress =
                            ((index.toFloat() / totalPages) * 90)
                                     .toInt() // up to 90% for processing
                    withContext(Dispatchers.Main) { onProgress(progress) }

                    val resources = page.resources
                    val xObjectNames =
                            resources.xObjectNames.toList() // Avoid concurrent modification

                    for (name in xObjectNames) {
                        if (resources.isImageXObject(name)) {
                            val xObject = resources.getXObject(name)
                            if (xObject is PDImageXObject) {
                                try {
                                    val cosObject = xObject.cosObject

                                    // Check if we've already compressed this specific image object
                                    if (processedImages.containsKey(cosObject)) {
                                        resources.put(name, processedImages[cosObject])
                                        continue
                                    }

                                    val originalBitmap = xObject.image
                                    if (originalBitmap != null) {
                                        // 1. Process Image: Downsample if too large
                                        val processedBitmap = downsampleBitmap(originalBitmap, maxDimension)

                                        // 2. Process Image: JPEG Compression
                                        val baos = ByteArrayOutputStream()
                                        processedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                                        val compressedByteArray = baos.toByteArray()

                                        // 3. Create new PDF Image object
                                        val newImage =
                                                PDImageXObject.createFromByteArray(
                                                        document,
                                                        compressedByteArray,
                                                        "comp_${System.currentTimeMillis()}"
                                                )
                                        
                                        // Store for reuse across pages
                                        processedImages[cosObject] = newImage
                                        resources.put(name, newImage)

                                        // Cleanup Bitmaps immediately
                                        if (processedBitmap != originalBitmap) {
                                            processedBitmap.recycle()
                                        }
                                        originalBitmap.recycle()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) { onProgress(95) } // 95% before saving

                val parentDir = getAppDirectory("Compress")
                val newFile = File(parentDir, "${file.nameWithoutExtension}_compressed.pdf")

                // Ensure a unique filename if it already exists
                var finalFile = newFile
                var counter = 1
                while (finalFile.exists()) {
                    finalFile =
                            File(parentDir, "${file.nameWithoutExtension}_compressed_$counter.pdf")
                    counter++
                }

                // Mark as compressed in metadata for persistent iconography
                document.documentInformation.setCustomMetadataValue("PDFlex-Type", "Compressed")
                
                document.save(finalFile)
                document.close()

                // File Size Verification: Professional way to handle text PDFs or highly optimized PDFs
                if (finalFile.length() >= file.length()) {
                    finalFile.delete() // Remove the larger file
                    withContext(Dispatchers.Main) {
                        onProgress(100)
                        onError("This document is already highly optimized and cannot be compressed further.")
                    }
                    return@launch
                }

                scanFile(finalFile)
                withContext(Dispatchers.Main) {
                    onProgress(100) // 100% done
                    loadRecentPdfs() // Refresh the list
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_compress_pdf",
                        screenName = "ImageViewModel",
                        action = "Compress_Success"
                    )
                    onSuccess(finalFile)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_compress_pdf",
                        screenName = "ImageViewModel",
                        action = "Compress_Failed",
                        extraParams = mapOf(
                            "error" to (e.message ?: "Unknown Error")
                        )
                    )
                    onError(e.message ?: "Failed to compress PDF")
                }
            }
        }
    }

    private fun downsampleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val scale = Math.min(
            maxDimension.toFloat() / width,
            maxDimension.toFloat() / height
        )

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    fun splitPdf(
            file: File,
            ranges: List<List<Int>>,
            password: String? = null,
            onProgress: (Int) -> Unit = {},
            onSuccess: (List<File>) -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document =
                        if (password != null) {
                            PDDocument.load(file, password, MemoryUsageSetting.setupTempFileOnly())
                        } else {
                            PDDocument.load(file, MemoryUsageSetting.setupTempFileOnly())
                        }

                val generatedFiles = mutableListOf<File>()
                val totalPagesToProcess = ranges.sumOf { it.size }.coerceAtLeast(1)
                var processedPages = 0

                val parentDir = getAppDirectory("Split")
                val baseName = file.nameWithoutExtension
                val cleanBaseName = baseName
                        .substringBeforeLast("_split")
                        .substringBeforeLast("_compressed")
                        .let { name ->
                            if (name.matches(Regex(".*_split_\\d+"))) {
                                name.substringBeforeLast("_split_")
                            } else {
                                name
                            }
                        }

                for ((rangeIndex, rangeIndices) in ranges.withIndex()) {
                    if (rangeIndices.isEmpty()) continue
                    
                    val newDocument = PDDocument()
                    val sortedIndices = rangeIndices.distinct().sorted()

                    for (index in sortedIndices) {
                        if (index >= 0 && index < document.numberOfPages) {
                            val page = document.getPage(index)
                            newDocument.importPage(page)
                        }
                        processedPages++
                        val progress = (processedPages.toFloat() / totalPagesToProcess * 100).toInt()
                        withContext(Dispatchers.Main) { onProgress(progress) }
                    }

                    val rangeStart = sortedIndices.firstOrNull()?.plus(1) ?: 1
                    val rangeEnd = sortedIndices.lastOrNull()?.plus(1) ?: rangeStart

                    val isContiguous = sortedIndices.indices.all { i ->
                        i == 0 || sortedIndices[i] == sortedIndices[i - 1] + 1
                    }

                    var finalFile = if (isContiguous && sortedIndices.size > 1 && sortedIndices.size < document.numberOfPages) {
                        val rangeName = "$rangeStart-$rangeEnd"
                        File(parentDir, "${cleanBaseName}_split_$rangeName.pdf")
                    } else if (isContiguous && sortedIndices.size == 1) {
                         File(parentDir, "${cleanBaseName}_split_$rangeStart.pdf")
                    } else {
                        File(parentDir, "${cleanBaseName}_split.pdf")
                    }

                    var counter = 1
                    val originalFinalFile = finalFile
                    while (finalFile.exists()) {
                        val nameWithoutExt = originalFinalFile.nameWithoutExtension
                        finalFile = File(parentDir, "${nameWithoutExt}_$counter.pdf")
                        counter++
                    }

                    newDocument.documentInformation.setCustomMetadataValue("PDFlex-Type", "Split")
                    newDocument.save(finalFile)
                    newDocument.close()
                    generatedFiles.add(finalFile)
                    scanFile(finalFile)
                }

                document.close()

                if (generatedFiles.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError("No pages selected to split")
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    loadRecentPdfs()
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_split_pdf",
                        screenName = "ImageViewModel",
                        action = "Split_Success",
                        extraParams = mapOf("file_count" to generatedFiles.size)
                    )
                    onSuccess(generatedFiles)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isLoadingPdfs.value = false
                    AnalyticsManager.logEventWithAction(
                        eventName = "feature_split_pdf",
                        screenName = "ImageViewModel",
                        action = "Split_Failed",
                        extraParams = mapOf(
                            "error" to (e.message ?: "Unknown Error")
                        )
                    )
                    onError(e.message ?: "Failed to split PDF")
                }
            }
        }
    }

    fun createUnlockedPdfCopy(file: File, password: String): File? {
        return try {
            val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(file, password, com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly())
            document.isAllSecurityToBeRemoved = true

            val parentDir = file.parentFile ?: getAppDirectory("Temp")
            val tempFile = File(parentDir, "${file.nameWithoutExtension}_temp_unlocked.pdf")

            document.save(tempFile)
            document.close()
            tempFile
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    fun createUnlockedPdfCopy(
            context: android.content.Context,
            uri: android.net.Uri,
            password: String
    ): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputStream, password, com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly())
            document.isAllSecurityToBeRemoved = true

            val parentDir = getAppDirectory("Temp")
            val tempFile = File(parentDir, "temp_unlocked_${System.currentTimeMillis()}.pdf")

            document.save(tempFile)
            document.close()
            inputStream.close()
            tempFile
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
    private fun scanFolderForPdfs(
            dir: java.io.File,
            depth: Int,
            allPdfs: MutableList<java.io.File>,
            createdPdfs: MutableList<java.io.File>
    ) {
        if (depth > 2 || !dir.exists() || !dir.isDirectory) return

        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                scanFolderForPdfs(file, depth + 1, allPdfs, createdPdfs)
            } else if (file.extension.lowercase(java.util.Locale.ROOT) == "pdf") {
                val path = file.absolutePath
                val isAlreadyAdded = allPdfs.any { it.absolutePath == path }

                if (!isAlreadyAdded) {
                    val matchesSearch =
                            _searchQuery.value.isEmpty() ||
                                    file.name.contains(_searchQuery.value, ignoreCase = true)

                    if (matchesSearch) {
                        allPdfs.add(file)
                        if (path.contains("PDFlex Scanner Editor", ignoreCase = true) || _importedPdfPaths.value.contains(path)) {
                            createdPdfs.add(file)
                        }
                        // Scan it so MediaStore picks it up eventually
                        scanFile(file)
                    }
                }
            }
        }
    }

    private fun getAppDirectory(subFolder: String? = null): java.io.File {
        val documentsDir =
                android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOCUMENTS
                )
        val appRoot = java.io.File(documentsDir, "PDFlex Scanner Editor")
        val finalDir = if (subFolder != null) java.io.File(appRoot, subFolder) else appRoot
        if (!finalDir.exists()) finalDir.mkdirs()
        return finalDir
    }
}

enum class SortCriteria {
    DateModified,
    DateCreated,
    Name,
    Size
}

enum class SortOrder {
    Ascending,
    Descending
}

data class FrameOption(val name: String, val iconRes: Int)
