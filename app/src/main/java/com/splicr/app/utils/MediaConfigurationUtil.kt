package com.splicr.app.utils

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.arthenica.ffmpegkit.FFmpegKit
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.data.MediaMetadataData
import com.splicr.app.data.TrimRangeData
import com.splicr.app.utils.InAppReviewUtil.triggerInAppReviewAutomatically
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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
            tb >= 1 -> String.format(Locale.getDefault(), "%.2fTB", tb)
            gb >= 1 -> String.format(Locale.getDefault(), "%.2fGB", gb)
            mb >= 1 -> String.format(Locale.getDefault(), "%.2fMB", mb)
            kb >= 1 -> String.format(Locale.getDefault(), "%.2fKB", kb)
            else -> String.format(Locale.getDefault(), "%dB", bytes)
        }
    }

    private fun getFileSize(context: Context, videoUri: Uri): Long? {
        return try {
            if (videoUri.scheme == "file") {
                // Use File API for file:// URIs
                val file = File(videoUri.path!!)
                file.length()
            } else {
                // Use ContentResolver for content:// URIs
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

    suspend fun downloadVideoToLocal(context: Context, videoUrl: String): Result<Uri?> {
        return withContext(Dispatchers.IO) {
            val fileName = "temp_video_${System.currentTimeMillis()}.mp4"
            val file = File(context.cacheDir, fileName)

            try {
                val url = URL(videoUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val outputStream = FileOutputStream(file)
                val inputStream = connection.inputStream

                inputStream.use { input ->
                    outputStream.use { output ->
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }

                connection.disconnect()
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
        val dateFormat =
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun exportVideo(
        context: Context,
        inputUri: Uri,
        outputFilePath: String,
        thumbnailPath: String,
        resolution: String,
        source: String,
        loaderDescription: MutableIntState,
        canvasItemData: CanvasItemData,
        onCompletion: (success: Boolean, errorMessageResource: Int?) -> Unit
    ) {
        when (inputUri.scheme) {
            "file" -> {

                if (canvasItemData.duration == 0L) {
                    canvasItemData.duration = getAllVideoMetadata(
                        context = context, videoUri = inputUri
                    )?.duration ?: 0
                }

                processVideo(
                    context = context,
                    inputFile = File(inputUri.path!!),
                    outputFilePath = outputFilePath,
                    thumbnailPath = thumbnailPath,
                    resolution = resolution,
                    source = source,
                    loaderDescription = loaderDescription,
                    canvasItemData = canvasItemData,
                    onCompletion = onCompletion,
                    fileName = "temp_file_video.mp4"
                )
            }

            "http", "https" -> {
                val fileName = "temp_url_video.mp4"
                val tempFile = File(context.cacheDir, fileName)
                if (tempFile.exists()) {
                    processVideo(
                        context = context,
                        inputFile = tempFile,
                        outputFilePath = outputFilePath,
                        thumbnailPath = thumbnailPath,
                        resolution = resolution,
                        source = source,
                        loaderDescription = loaderDescription,
                        canvasItemData = canvasItemData,
                        onCompletion = onCompletion,
                        fileName = fileName
                    )
                } else {
                    val storageReference = Firebase.storage.getReferenceFromUrl(inputUri.toString())
                    storageReference.getFile(tempFile.toUri()).addOnSuccessListener {
                        processVideo(
                            context = context,
                            inputFile = tempFile,
                            outputFilePath = outputFilePath,
                            thumbnailPath = thumbnailPath,
                            resolution = resolution,
                            source = source,
                            loaderDescription = loaderDescription,
                            canvasItemData = canvasItemData,
                            onCompletion = onCompletion,
                            fileName = fileName
                        )
                    }.addOnFailureListener { _ ->
                        if (tempFile.exists()) {
                            tempFile.delete()
                        }
                        onCompletion(
                            false, R.string.an_error_occurred_while_downloading_video
                        )
                    }
                }
            }

            else -> {
                onCompletion(false, R.string.an_error_occurred_please_attempt_the_process_again)
            }
        }
    }

    private fun processVideo(
        context: Context,
        inputFile: File,
        outputFilePath: String,
        thumbnailPath: String,
        resolution: String,
        source: String,
        loaderDescription: MutableIntState,
        canvasItemData: CanvasItemData,
        fileName: String,
        onCompletion: (success: Boolean, errorMessageResource: Int?) -> Unit
    ) {
        val scale = if (resolution == "4k") "3840:2160" else "1280:720"

        FFmpegKit.executeAsync("-i ${inputFile.absolutePath} -vf scale=$scale -c:v mpeg4 -preset slow -crf 22 -c:a copy $outputFilePath") { session ->
            val returnCode = session.returnCode
            Log.d("VideoExport", "FFmpeg output: ${session.output}")

            if (returnCode.isValueSuccess) {
                val savedToDevice = saveVideoToDevice(context, outputFilePath)
                if (savedToDevice) {
                    if (source != "HomeScreen" && Firebase.auth.currentUser != null) {
                        loaderDescription.intValue =
                            R.string.saving_your_medium_to_your_account_thank_you_for_your_patience
                        uploadToFirebaseStorage(
                            videoPath = outputFilePath,
                            thumbnailPath = thumbnailPath,
                            thumbnailBitmap = canvasItemData.thumbnailBitmap
                        ) { videoUrl, thumbnailUrl ->
                            if (videoUrl != null && thumbnailUrl != null) {
                                canvasItemData.id = UUID.randomUUID().toString() + SimpleDateFormat(
                                    "_yyyyMMdd_HHmmss", Locale.ENGLISH
                                ).format(
                                    Date()
                                )
                                canvasItemData.url = videoUrl
                                canvasItemData.thumbnailUrl = thumbnailUrl

                                Firebase.firestore.collection("media").add(canvasItemData)
                                    .addOnSuccessListener {
                                        if (File(context.cacheDir, fileName).exists()) {
                                            File(context.cacheDir, fileName).delete()
                                        }
                                        triggerInAppReviewAutomatically(context = context)
                                        onCompletion(true, null)
                                    }.addOnFailureListener { e ->
                                        e.printStackTrace()
                                        onCompletion(
                                            false,
                                            R.string.an_error_occurred_while_saving_to_your_account
                                        )
                                    }
                            } else if (videoUrl == null) {
                                onCompletion(
                                    false,
                                    R.string.an_error_occurred_while_saving_to_your_account_failed_to_upload_video
                                )
                            } else {
                                onCompletion(
                                    false,
                                    R.string.an_error_occurred_while_saving_to_your_account_failed_to_upload_thumbnail
                                )
                            }

                        }
                    } else {
                        if (File(context.cacheDir, fileName).exists()) {
                            File(context.cacheDir, fileName).delete()
                        }
                        triggerInAppReviewAutomatically(context = context)
                        onCompletion(true, null)
                    }
                } else {
                    onCompletion(
                        false, R.string.an_error_occurred_while_saving_to_your_device
                    )
                }
            } else {
                onCompletion(
                    false, R.string.an_error_occurred_while_saving_to_your_device
                )
            }
        }
    }

    private fun uploadToFirebaseStorage(
        videoPath: String,
        thumbnailPath: String,
        thumbnailBitmap: Bitmap?,
        onCompletion: (String?, String?) -> Unit
    ) {
        val firebaseStorage = Firebase.storage
        val videoRef = firebaseStorage.reference.child(
            "videos/${Firebase.auth.currentUser?.uid}_${
                File(videoPath).name
            }"
        )
        val thumbnailRef = firebaseStorage.reference.child(
            "thumbnails/${Firebase.auth.currentUser?.uid}_${
                File(thumbnailPath).name
            }"
        )

        videoRef.putFile(Uri.fromFile(File(videoPath))).addOnSuccessListener {
            videoRef.downloadUrl.addOnSuccessListener { videoUrl ->
                if (thumbnailBitmap != null) {
                    thumbnailRef.putBytes(bitmapToByteArray(thumbnailBitmap)).addOnSuccessListener {
                        thumbnailRef.downloadUrl.addOnSuccessListener { thumbnailUrl ->
                            onCompletion(videoUrl.toString(), thumbnailUrl.toString())
                        }.addOnFailureListener { e ->
                            e.printStackTrace()
                            onCompletion(videoUrl.toString(), null)
                        }
                    }.addOnFailureListener { e ->
                        e.printStackTrace()
                        onCompletion(videoUrl.toString(), null)
                    }
                } else {
                    onCompletion(videoUrl.toString(), null)
                }
            }.addOnFailureListener { _ ->
                onCompletion(null, null)
            }
        }.addOnFailureListener { _ ->
            onCompletion(null, null)
        }
    }

    fun shareVideo(
        context: Context, videoPath: String, packageName: String?, onResult: (Boolean, Int?) -> Unit
    ) {
        val videoFile = File(videoPath)
        val videoUri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", videoFile)

        packageName?.let {
            if (isPackageInstalled(context, it)) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, videoUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage(it)
                }
                try {
                    context.startActivity(shareIntent)
                    onResult(true, null)
                } catch (e: ActivityNotFoundException) {
                    onResult(false, R.string.invalid_package_name)
                }
            } else {
                onResult(false, R.string.we_could_not_find_any_application_to_handle_that_operation)
            }
        } ?: run {
            onResult(false, R.string.invalid_package_name)
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun saveVideoToDevice(context: Context, filePath: String): Boolean {
        val file = File(filePath)
        val folderName = context.getString(R.string.in_app_name)
        val directoryPath =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}/${folderName}"
        val directory = File(directoryPath)

        // Check and create directory if necessary
        if (!directory.exists()) {
            val created = directory.mkdirs()
            if (!created) {
                return false
            }
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_MOVIES}/${folderName}"
            )
        }

        return try {
            val uri: Uri? = context.contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues
            )
            uri?.let {
                context.contentResolver.openOutputStream(it).use { outputStream ->
                    File(filePath).inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getCustomDirectory(context: Context): File {
        val directory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), context.getString(
                R.string.in_app_name
            )
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    fun getOutputFilePath(context: Context, filename: String): String {
        val customDirectory = getCustomDirectory(context)
        return File(customDirectory, filename).absolutePath
    }

    private fun convertContentUriToFile(context: Context, uri: Uri): File? {
        return try {
            val file = File(context.cacheDir, "temp_uri_video.mp4")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    fun processVideo(
        uri: Uri,
        trimRanges: List<TrimRangeData>,
        context: Context,
        aspectRatioWidth: Int,
        aspectRatioHeight: Int,
        onCompletion: (Uri?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result =
                processVideoInternal(uri, trimRanges, context, aspectRatioWidth, aspectRatioHeight)
            withContext(Dispatchers.Main) {
                onCompletion(result)
            }
        }
    }

    private fun processVideoInternal(
        uri: Uri,
        trimRanges: List<TrimRangeData>,
        context: Context,
        aspectRatioWidth: Int,
        aspectRatioHeight: Int
    ): Uri? {
        val videoFile = convertContentUriToFile(context, uri) ?: return null
        val tempDirectory = context.cacheDir
        val tempFileForMerge = File(tempDirectory, "merged_output.mp4").absolutePath
        val tempFileVideoPath = File(tempDirectory, "temp_file_video.mp4").absolutePath

        return when {
            trimRanges.size == 1 -> {
                // Single range case: trim and adjust resolution
                val range = trimRanges.first()
                val tempFilePath = File(tempDirectory, "segment_1.mp4").absolutePath
                val trimCommand =
                    "-y -i ${videoFile.absolutePath} -ss ${range.startTime} -to ${range.endTime} -c:v mpeg4 -crf 23 -preset fast -c:a aac $tempFilePath"

                val trimSession = FFmpegKit.execute(trimCommand)
                if (trimSession.returnCode.isValueSuccess) {
                    // Use a different file for resolution adjustment
                    adjustAspectRatio(
                        tempFilePath, tempFileVideoPath, aspectRatioWidth, aspectRatioHeight
                    )
                } else {
                    null
                }
            }

            else -> {
                // Multiple ranges case: trim, merge, and adjust resolution
                val tempFilePaths = mutableListOf<String>()
                trimRanges.forEachIndexed { index, range ->
                    val tempFilePath = File(tempDirectory, "segment_${index + 1}.mp4").absolutePath
                    val trimCommand =
                        "-y -i ${videoFile.absolutePath} -ss ${range.startTime} -to ${range.endTime} -c:v mpeg4 -crf 23 -preset fast -c:a aac $tempFilePath"

                    val trimSession = FFmpegKit.execute(trimCommand)
                    if (trimSession.returnCode.isValueSuccess) {
                        tempFilePaths.add(tempFilePath)
                    } else {
                        return@processVideoInternal null
                    }
                }
                if (tempFilePaths.size == trimRanges.size) {
                    val mergeResult = mergeVideos(
                        tempFilePaths = tempFilePaths,
                        finalVideoPath = tempFileForMerge,
                        context = context
                    )
                    // Ensure different file paths for merging and final resolution adjustment
                    if (mergeResult != null) {
                        adjustAspectRatio(
                            tempFileForMerge, tempFileVideoPath, aspectRatioWidth, aspectRatioHeight
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
    }

    private fun adjustAspectRatio(
        inputPath: String,
        outputPath: String,
        targetAspectRatioWidth: Int,
        targetAspectRatioHeight: Int
    ): Uri? {
        val scaleAndPadCommand =
            "-i $inputPath -vf \"scale=$targetAspectRatioWidth:$targetAspectRatioHeight\" -c:v mpeg4 $outputPath"

        val session = FFmpegKit.execute(scaleAndPadCommand)

        return if (session.returnCode.isValueSuccess) {
            Uri.fromFile(File(outputPath))
        } else {
            Log.i("FFmpegError", scaleAndPadCommand)
            Log.i("FFmpegError", "InputPath: $inputPath, OutputPath: $outputPath")
            Log.d("FFmpegError", "FFmpeg output: ${session.output}")
            null
        }
    }

    private fun mergeVideos(
        tempFilePaths: List<String>, finalVideoPath: String, context: Context
    ): Uri? {
        val fileListPath = File(context.cacheDir, "filelist.txt").absolutePath
        val fileListContent = tempFilePaths.joinToString("\n") { "file '$it'" }
        File(fileListPath).writeText(fileListContent)

        val mergeCommand = "-f concat -safe 0 -i $fileListPath -c copy $finalVideoPath"
        val mergeSession = FFmpegKit.execute(mergeCommand)
        return if (mergeSession.returnCode.isValueSuccess) {
            Uri.fromFile(File(finalVideoPath))
        } else {
            null
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