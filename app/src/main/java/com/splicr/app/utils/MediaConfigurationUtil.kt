package com.splicr.app.utils

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.core.net.toUri
import com.cloudinary.Transformation
import com.cloudinary.android.MediaManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.splicr.app.BuildConfig
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.data.MediaMetadataData
import com.splicr.app.data.TrimRangeData
import com.splicr.app.utils.InAppReviewUtil.triggerInAppReviewAutomatically
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object MediaConfigurationUtil {

    fun getAllVideoMetadata(context: Context, videoUri: Uri): MediaMetadataData? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            val fileSize = getFileSize(context, videoUri)
            val thumbnail = retriever.frameAtTime

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull()
            val rotation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toIntOrNull() ?: 0
            val adjustedWidth = if (rotation == 90 || rotation == 270) height else width
            val adjustedHeight = if (rotation == 90 || rotation == 270) width else height

            MediaMetadataData(
                width = adjustedWidth,
                height = adjustedHeight,
                duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong(),
                bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                    ?.toInt(),
                location = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION),
                mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                    ?.toFloat(),
                rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toInt(),
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE),
                numTracks = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)
                    ?.toInt(),
                hasAudio = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO
                ),
                composer = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_COMPOSER
                ),
                discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                    ?.toInt(),
                writer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER),
                albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toInt(),
                fileSize = fileSize,
                thumbnail = thumbnail
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        val tb = gb / 1024.0

        return when {
            tb >= 1 -> formatSize(tb, "TB")
            gb >= 1 -> formatSize(gb, "GB")
            mb >= 1 -> formatSize(mb, "MB")
            kb >= 1 -> formatSize(kb, "KB")
            else -> "$bytes B"
        }
    }

    private fun formatSize(value: Double, unit: String): String {
        return if (value % 1 == 0.0) {
            String.format(Locale.getDefault(), "%.0f %s", value, unit)
        } else {
            String.format(Locale.getDefault(), "%.2f %s", value, unit)
        }
    }

    private fun getFileSize(context: Context, videoUri: Uri): Long? {
        return try {
            if (videoUri.scheme == "file") {
                val file = File(videoUri.path!!)
                file.length()
            } else {
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(videoUri, null, null, null, null)
                    cursor?.let {
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        it.moveToFirst()
                        it.getLong(sizeIndex)
                    }
                } finally {
                    cursor?.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun checkIfVideoUrl(videoUrl: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(videoUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val mimeType = connection.contentType
                connection.disconnect()

                if (mimeType.startsWith("video/")) {
                    return@withContext Result.success(true)
                } else {
                    return@withContext Result.failure(Exception("Invalid media type"))
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    fun formatDuration(durationMillis: Long, shouldBeInFullFormat: Boolean = true): String {
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (shouldBeInFullFormat) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else if (hours > 0) {
            String.format(Locale.getDefault(), "%2d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%2d:%02d", minutes, seconds)
        }
    }

    private fun timeToSeconds(time: String): Int {
        val parts = time.split(":")
        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0
        val seconds = parts[2].toIntOrNull() ?: 0
        return (hours * 3600) + (minutes * 60) + seconds
    }

    fun formatTimestamp(timestamp: Any): String {
        val date = when (timestamp) {
            is Timestamp -> {
                timestamp.toDate()
            }

            is Long -> {
                Date(timestamp)
            }

            else -> {
                Date()
            }
        }
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun exportVideo(
        context: Context,
        fileUriString: MutableState<String>,
        videoUrl: String,
        resolution: String,
        source: String,
        loaderDescription: MutableIntState,
        canvasItemData: CanvasItemData,
        onCompletion: (Boolean, Int?, String?) -> Unit
    ) {
        if (videoUrl.contains("firebasestorage.googleapis.com")) {
            val tempFile = File(context.cacheDir, "temp_url_video.mp4")
            if (tempFile.exists()) {
                uploadToCloudinary(tempFile, resolution) { cloudinaryUrl ->
                    if (cloudinaryUrl != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            downloadSaveAndUploadVideo(
                                context,
                                fileUriString,
                                cloudinaryUrl,
                                source,
                                loaderDescription,
                                canvasItemData,
                                onCompletion
                            )
                        }
                    } else {
                        onCompletion(false, R.string.an_error_occurred_while_processing_video, null)
                    }
                }
            } else {
                val storageReference = Firebase.storage.getReferenceFromUrl(videoUrl)
                storageReference.getFile(tempFile.toUri()).addOnSuccessListener {
                    uploadToCloudinary(tempFile, resolution) { cloudinaryUrl ->
                        if (cloudinaryUrl != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                downloadSaveAndUploadVideo(
                                    context,
                                    fileUriString,
                                    cloudinaryUrl,
                                    source,
                                    loaderDescription,
                                    canvasItemData,
                                    onCompletion
                                )
                            }
                        } else {
                            onCompletion(
                                false, R.string.an_error_occurred_while_processing_video, null
                            )
                        }
                    }
                }.addOnFailureListener {
                    onCompletion(false, null, it.localizedMessage)
                }

            }
        } else if (videoUrl.contains("res.cloudinary.com")) {
            CoroutineScope(Dispatchers.IO).launch {
                downloadSaveAndUploadVideo(
                    context, fileUriString, MediaManager.get().url().transformation(
                        Transformation<Transformation<*>>().width(if (resolution == "4k") 3840 else 1280)
                            .height(if (resolution == "4k") 2160 else 720).crop("fill").chain()
                            .flags("keep_dar")
                    ).generate(videoUrl), source, loaderDescription, canvasItemData, onCompletion
                )
            }

        } else {
            onCompletion(false, R.string.an_error_occurred_please_attempt_the_process_again, null)
        }
    }

    fun uploadToCloudinary(videoFile: File, resolution: String, onComplete: (String?) -> Unit) {
        val options = HashMap<String, Any>()
        options.put("resource_type", "video")
        options.put("public_id", "temp_video/${getUserId()}")
        options.put(
            "transformation",
            Transformation<Transformation<*>>().width(if (resolution == "4k") 3840 else 1280)
                .height(if (resolution == "4k") 2160 else 720).crop("fill").chain()
                .flags("keep_dar")
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = MediaManager.get().cloudinary.uploader()
                    .uploadLarge(videoFile.absolutePath, options, 6000000)

                val cloudinaryUrl = result?.get("secure_url") as? String
                onComplete(cloudinaryUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }

    fun downloadSaveAndUploadVideo(
        context: Context,
        fileUriString: MutableState<String>,
        videoUrl: String,
        source: String,
        loaderDescription: MutableIntState,
        canvasItemData: CanvasItemData,
        onCompletion: (success: Boolean, errorMessageResource: Int?, errorMessage: String?) -> Unit
    ) {
        CoroutineScope(context = Dispatchers.IO).launch {
            downloadAndSaveVideoToDevice(context, videoUrl) { uri, fileName ->
                if (uri != null) {
                    fileUriString.value = uri.toString()
                    if (source != "HomeScreen" && Firebase.auth.currentUser != null) {
                        loaderDescription.intValue =
                            R.string.saving_your_medium_to_your_account_thank_you_for_your_patience
                        uploadToFirebaseStorage(
                            context, uri, fileName.toString(), canvasItemData.thumbnailUrl
                        ) { videoUrl, thumbnailUrl ->
                            if (videoUrl != null && thumbnailUrl != null) {
                                canvasItemData.id = "${Firebase.auth.currentUser?.uid}_${
                                    fileName?.replace(
                                        if (fileName.contains(".mp4")) ".mp4" else ".jpg", ""
                                    )
                                }"
                                canvasItemData.url = videoUrl
                                canvasItemData.thumbnailUrl = thumbnailUrl
                                canvasItemData.size = getAllVideoMetadata(
                                    context, uri
                                )?.fileSize ?: 0
                                canvasItemData.duration = getAllVideoMetadata(
                                    context, uri
                                )?.duration ?: 0

                                Firebase.firestore.collection("media").document(canvasItemData.id)
                                    .set(canvasItemData).addOnSuccessListener {
                                        triggerInAppReviewAutomatically(context)
                                        onCompletion(true, null, null)
                                    }.addOnFailureListener {
                                        onCompletion(
                                            false, null, it.localizedMessage
                                        )
                                    }
                            } else {
                                onCompletion(
                                    false,
                                    if (videoUrl == null) R.string.an_error_occurred_while_saving_to_your_account_failed_to_upload_video else R.string.an_error_occurred_while_saving_to_your_account_failed_to_upload_thumbnail,
                                    null
                                )
                            }
                        }
                    } else {
                        canvasItemData.size = getAllVideoMetadata(
                            context, uri
                        )?.fileSize ?: 0
                        triggerInAppReviewAutomatically(context)
                        onCompletion(true, null, null)
                    }
                } else {
                    onCompletion(
                        false, R.string.an_error_occurred_while_saving_to_your_device, null
                    )
                }
            }
        }
    }

    suspend fun downloadAndSaveVideoToDevice(
        context: Context, videoUrl: String, onSuccess: (Uri?, String?) -> Unit
    ) {
        return withContext(Dispatchers.IO) {
            val fileName = "${
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()
                ).format(
                    Date()
                )
            }.mp4"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/" + context.getString(R.string.in_app_name)
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val contentResolver = context.contentResolver
            val uri: Uri? =
                contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            try {
                uri?.let { videoUri ->
                    contentResolver.openOutputStream(videoUri).use { outputStream ->
                        URL(videoUrl).openStream().use { inputStream ->
                            inputStream.copyTo(outputStream!!)
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(videoUri, contentValues, null, null)

                    onSuccess(videoUri, fileName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uri?.let {
                    contentResolver.delete(
                        it, null, null
                    )
                }
                onSuccess(null, null)
            }
        }
    }

    private fun uploadToFirebaseStorage(
        context: Context,
        videoUri: Uri,
        fileName: String,
        thumbnailUrl: String?,
        onCompletion: (String?, String?) -> Unit
    ) {
        val firebaseStorage = Firebase.storage
        val videoRef = firebaseStorage.reference.child(
            "videos/${Firebase.auth.currentUser?.uid}_${
                fileName
            }"
        )
        val thumbnailRef = firebaseStorage.reference.child(
            "thumbnails/${Firebase.auth.currentUser?.uid}_${
                fileName.replace(".mp4", ".jpg")
            }"
        )

        videoRef.putFile(videoUri).addOnSuccessListener {
            videoRef.downloadUrl.addOnSuccessListener { videoUrl ->
                if (!thumbnailUrl.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        downloadMediaToLocal(
                            context, thumbnailUrl, "temp_download_thumbnail.jpg"
                        ).onSuccess { thumbnailUri ->
                            if (thumbnailUri != null) {
                                thumbnailRef.putFile(thumbnailUri).addOnSuccessListener {
                                    thumbnailRef.downloadUrl.addOnSuccessListener { thumbnailUrl ->
                                        File(thumbnailUri.path!!).delete()
                                        onCompletion(videoUrl.toString(), thumbnailUrl.toString())
                                    }.addOnFailureListener { e ->
                                        e.printStackTrace()
                                        onCompletion(videoUrl.toString(), null)
                                    }
                                }.addOnFailureListener {
                                    it.printStackTrace()
                                    onCompletion(videoUrl.toString(), null)
                                }
                            } else {
                                onCompletion(videoUrl.toString(), null)
                            }
                        }.onFailure {
                            it.printStackTrace()
                            onCompletion(videoUrl.toString(), null)
                        }
                    }
                } else {
                    onCompletion(videoUrl.toString(), null)
                }
            }.addOnFailureListener {
                it.printStackTrace()
                onCompletion(null, null)
            }
        }.addOnFailureListener {
            it.printStackTrace()
            onCompletion(null, null)
        }
    }

    suspend fun downloadMediaToLocal(
        context: Context, mediaUrl: String, fileName: String = "temp_download_video.mp4"
    ): Result<Uri?> {
        return withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, fileName)

            try {
                val url = URL(mediaUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                file.outputStream().use { outputStream ->
                    connection.inputStream.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                connection.disconnect()

                if (file.length() == 0L) {
                    file.delete()
                    return@withContext Result.failure(Exception(context.getString(R.string.downloaded_file_is_empty)))
                }

                Result.success(Uri.fromFile(file))
            } catch (e: Exception) {
                e.printStackTrace()
                if (file.exists()) {
                    file.delete()
                }
                Result.failure(e)
            }
        }
    }

    fun shareVideo(
        context: Context,
        videoUri: Uri,
        packageName: String?,
        fallbackPackageName: String?,
        onResult: (Boolean, Int?) -> Unit
    ) {
        packageName?.let {
            if (isPackageInstalled(context, it)) {
                startSharingIntent(context, videoUri, it, onResult)
            } else if (fallbackPackageName != null && isPackageInstalled(
                    context, fallbackPackageName
                )
            ) {
                startSharingIntent(context, videoUri, fallbackPackageName, onResult)
            } else {
                onResult(false, R.string.we_could_not_find_any_application_to_handle_that_operation)
            }
        } ?: run {
            onResult(
                false, R.string.something_went_wrong_we_could_not_successfully_handle_that_operation
            )
        }
    }

    private fun startSharingIntent(
        context: Context, videoUri: Uri, packageName: String, onResult: (Boolean, Int?) -> Unit
    ) {
        try {
            context.startActivity(Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, videoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage(packageName)
            })
            onResult(true, null)
        } catch (_: ActivityNotFoundException) {
            onResult(
                false, R.string.something_went_wrong_we_could_not_successfully_handle_that_operation
            )
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0).enabled
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getUserId(): String {
        val user = Firebase.auth.currentUser
        return if (user != null) {
            user.uid
        } else {
            when {
                SharedPreferenceUtil.guestUserId() != null -> {
                    SharedPreferenceUtil.guestUserId().toString()
                }

                SharedPreferenceUtil.guestUserId() == null -> {
                    SharedPreferenceUtil.guestUserId(UUID.randomUUID().toString()).toString()
                }

                else -> {
                    ""
                }
            }
        }
    }

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            "file" -> uri.path
            "content" -> {
                var filePath: String? = null
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex("_data")
                        if (columnIndex != -1) {
                            filePath = it.getString(columnIndex)
                        }
                    }
                }
                filePath
            }

            else -> null
        }
    }

    fun uploadVideoAndWaitForPreview(
        context: Context,
        uri: Uri,
        trimRanges: List<TrimRangeData>,
        aspectRatio: String,
        onPreviewReady: (String?, String?) -> Unit
    ) {
        val options = HashMap<String, Any>()
        options.put("resource_type", "video")
        options.put("public_id", "temp_video/${getUserId()}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = MediaManager.get().cloudinary.uploader()
                    .uploadLarge(getFilePathFromUri(context, uri), options, 6000000)

                val publicId = result?.get("public_id").toString()

                val videoTransformation = Transformation<Transformation<*>>()
                val thumbnailTransformation = Transformation<Transformation<*>>()

                trimRanges.forEachIndexed { index, range ->
                    val startSeconds = timeToSeconds(range.startTime)
                    val endSeconds = timeToSeconds(range.endTime)

                    if (index == 0) {
                        videoTransformation.startOffset(startSeconds.toFloat())
                            .endOffset(endSeconds.toFloat())

                        thumbnailTransformation.startOffset(startSeconds.toFloat())
                            .endOffset(endSeconds.toFloat())
                    } else {
                        videoTransformation.chain().flags("splice")
                            .overlay("video:${publicId.replace("/", ":")}")
                            .startOffset(startSeconds.toFloat()).endOffset(endSeconds.toFloat())
                            .chain().flags("layer_apply")

                        thumbnailTransformation.chain().flags("splice")
                            .overlay("video:${publicId.replace("/", ":")}")
                            .startOffset(startSeconds.toFloat()).endOffset(endSeconds.toFloat())
                            .chain().flags("layer_apply")
                    }
                }

                val thumbnailUrl = MediaManager.get().url().resourceType("video").transformation(
                    thumbnailTransformation.startOffset("auto").chain().gravity("auto").chain()
                        .quality("auto").fetchFormat("jpg")
                ).generate(publicId)

                val finalVideoUrl = MediaManager.get().url().resourceType("video").transformation(
                    videoTransformation.chain().aspectRatio(aspectRatio).crop("fill")
                ).generate(publicId)

                withContext(Dispatchers.Main) {
                    onPreviewReady(finalVideoUrl, thumbnailUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onPreviewReady(null, null)
                }
            }
        }
    }

    suspend fun fetchCloudinaryMetadata(publicId: String): Result<Long?> {
        return withContext(Dispatchers.IO) {
            try {
                val url =
                    "https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/resources/video/upload/$publicId"
                val connection = URL(url).openConnection() as HttpURLConnection
                val auth = Base64.encodeToString(
                    "${BuildConfig.CLOUDINARY_API_KEY}:${BuildConfig.CLOUDINARY_API_SECRET}".toByteArray(),
                    Base64.NO_WRAP
                )
                connection.setRequestProperty("Authorization", "Basic $auth")
                connection.requestMethod = "GET"
                connection.connect()

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val derivedArray = JSONObject(response).optJSONArray("derived") ?: JSONArray()
                if (derivedArray.length() > 0 && derivedArray.getJSONObject(0)
                        .getString("format") != "jpg"
                ) {
                    Result.success(
                        derivedArray.getJSONObject(0).getLong("bytes")
                    )
                } else {
                    Result.success(0L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    fun convertDimensionsToAspectRatio(context: Context, width: Int, height: Int): String {
        val aspectRatio = width.toFloat() / height.toFloat()
        val tolerance = 0.01f

        return when {
            kotlin.math.abs(aspectRatio - 1f / 1f) < tolerance -> "1:1"           // Square
            kotlin.math.abs(aspectRatio - 4f / 3f) < tolerance -> "4:3"           // Traditional TV, older monitors
            kotlin.math.abs(aspectRatio - 3f / 2f) < tolerance -> "3:2"           // Photography, digital cameras
            kotlin.math.abs(aspectRatio - 16f / 9f) < tolerance -> "16:9"         // Modern widescreen (HDTV, monitors)
            kotlin.math.abs(aspectRatio - 9f / 16f) < tolerance -> "9:16"         // Portrait mode for vertical videos
            kotlin.math.abs(aspectRatio - 5f / 4f) < tolerance -> "5:4"           // Older monitors, some photography
            kotlin.math.abs(aspectRatio - 21f / 9f) < tolerance -> "21:9"         // Ultra-wide displays, some movies
            kotlin.math.abs(aspectRatio - 18f / 9f) < tolerance -> "18:9"         // Modern smartphones
            kotlin.math.abs(aspectRatio - 19.5f / 9f) < tolerance -> "19.5:9"     // Some newer smartphones
            kotlin.math.abs(aspectRatio - 9f / 20f) < tolerance -> "9:20"         // Vertical videos with higher aspect ratios
            kotlin.math.abs(aspectRatio - 2f / 1f) < tolerance -> "2:1"           // Ultra-widescreen video
            kotlin.math.abs(aspectRatio - 32f / 9f) < tolerance -> "32:9"         // Super ultra-wide monitors
            kotlin.math.abs(aspectRatio - 1.85f / 1f) < tolerance -> "1.85:1"     // Standard widescreen cinema
            kotlin.math.abs(aspectRatio - 2.39f / 1f) < tolerance -> "2.39:1"     // CinemaScope/Anamorphic widescreen
            kotlin.math.abs(aspectRatio - 2.76f / 1f) < tolerance -> "2.76:1"     // Ultra Panavision 70
            kotlin.math.abs(aspectRatio - 5f / 3f) < tolerance -> "5:3"           // Some display formats
            kotlin.math.abs(aspectRatio - 3f / 1f) < tolerance -> "3:1"           // Ultra-wide displays
            kotlin.math.abs(aspectRatio - 4f / 1f) < tolerance -> "4:1"           // Extra-wide digital signage
            kotlin.math.abs(aspectRatio - 1.33f / 1f) < tolerance -> "1.33:1"     // Legacy 4:3 video format
            kotlin.math.abs(aspectRatio - 2.20f / 1f) < tolerance -> "2.20:1"     // 70mm film format
            kotlin.math.abs(aspectRatio - 2.55f / 1f) < tolerance -> "2.55:1"     // Original CinemaScope format
            kotlin.math.abs(aspectRatio - 5f / 2f) < tolerance -> "5:2"           // Panoramic photography
            kotlin.math.abs(aspectRatio - 10f / 3f) < tolerance -> "10:3"         // Ultra-wide cinematic
            kotlin.math.abs(aspectRatio - 1.6f / 1f) < tolerance -> "1.6:1"       // Photography format (similar to 16:10)
            kotlin.math.abs(aspectRatio - 2.35f / 1f) < tolerance -> "2.35:1"     // Older anamorphic widescreen
            kotlin.math.abs(aspectRatio - 5f / 7f) < tolerance -> "5:7"           // Portrait photography
            kotlin.math.abs(aspectRatio - 8f / 10f) < tolerance -> "8:10"         // Photography prints
            kotlin.math.abs(aspectRatio - 3f / 4f) < tolerance -> "3:4"           // Photography and digital cameras
            kotlin.math.abs(aspectRatio - 7f / 5f) < tolerance -> "7:5"           // Photography prints
            kotlin.math.abs(aspectRatio - 2f / 3f) < tolerance -> "2:3"           // Photography format
            kotlin.math.abs(aspectRatio - 2.4f / 1f) < tolerance -> "2.4:1"       // Cinematic widescreen
            kotlin.math.abs(aspectRatio - 2.55f / 1f) < tolerance -> "2.55:1"     // CinemaScope
            kotlin.math.abs(aspectRatio - 2.76f / 1f) < tolerance -> "2.76:1"     // Ultra Panavision 70
            kotlin.math.abs(aspectRatio - 1.33f / 1f) < tolerance -> "1.33:1"     // Legacy 4:3
            kotlin.math.abs(aspectRatio - 2.76f / 1f) < tolerance -> "2.76:1"     // Ultra Panavision 70
            kotlin.math.abs(aspectRatio - 3.2f / 1f) < tolerance -> "3.2:1"       // Ultra-wide aspect ratio
            kotlin.math.abs(aspectRatio - 4.5f / 1f) < tolerance -> "4.5:1"       // Extra-wide
            kotlin.math.abs(aspectRatio - 5.5f / 1f) < tolerance -> "5.5:1"       // Super extra-wide
            else -> context.getString(R.string.unknown_aspect_ratio)
        }
    }
}