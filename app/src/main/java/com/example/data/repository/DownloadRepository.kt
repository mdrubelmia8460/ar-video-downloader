package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.local.DownloadDao
import com.example.data.model.AnalyzedMedia
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaQualityOption
import com.example.data.model.MediaType
import com.example.data.service.DownloadPausedException
import com.example.data.service.DownloadService
import com.example.data.service.LinkType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

class DownloadRepository(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val client = DownloadService.httpClient

    private val repositoryScope = CoroutineScope(Dispatchers.IO + Job())
    private val activeDownloadJobs = ConcurrentHashMap<Long, Job>()
    private val pausedDownloads = ConcurrentHashMap<Long, Boolean>()
    private val cancelledDownloads = ConcurrentHashMap<Long, Boolean>()

    init {
        repositoryScope.launch {
            downloadDao.markInterruptedAsPaused()
        }
    }

    val allDownloads: Flow<List<DownloadItem>> = downloadDao.getAllDownloads()
    val completedCount: Flow<Int> = downloadDao.getCompletedCount()
    val totalBytesDownloaded: Flow<Long> = downloadDao.getTotalBytesDownloaded()

    suspend fun analyzeUrl(inputUrl: String): Result<AnalyzedMedia> = withContext(Dispatchers.IO) {
        try {
            val linkResult = DownloadService.validateLink(inputUrl)
            if (linkResult.type == LinkType.INVALID) {
                return@withContext Result.failure(IllegalArgumentException(linkResult.message))
            }
            if (linkResult.type != LinkType.DIRECT_VIDEO && linkResult.type != LinkType.DIRECT_AUDIO) {
                return@withContext Result.failure(
                    IllegalArgumentException("শুধু সরাসরি অনুমোদিত মিডিয়া URL ডাউনলোড করা যাবে")
                )
            }

            val validUrl = linkResult.url

            val host = try {
                URL(validUrl).host.lowercase()
            } catch (e: Exception) {
                "web"
            }

            val platform = "Direct Media"

            val path = try { URL(validUrl).path } catch (e: Exception) { "" }
            val lastSegment = path.substringAfterLast("/", "")
            val titleFromUrl = if (lastSegment.isNotBlank() && lastSegment.contains(".")) {
                lastSegment.substringBeforeLast(".").replace("%20", " ").replace("-", " ").replace("_", " ")
                    .replaceFirstChar { it.uppercase() }
            } else {
                "AR Media - ${System.currentTimeMillis() % 10000}"
            }

            var remoteLength = 0L
            try {
                val headReq = Request.Builder()
                    .url(validUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                    .head()
                    .build()
                client.newCall(headReq).execute().use { response ->
                    when {
                        response.isSuccessful -> {
                            val ct = response.header("Content-Type")
                            if (!ct.isNullOrBlank() && !DownloadService.isMediaContentType(ct)) {
                                return@withContext Result.failure(
                                    IllegalArgumentException("সার্ভার থেকে আসল মিডিয়া ফাইল পাওয়া যায়নি")
                                )
                            }
                            remoteLength = response.header("Content-Length")?.toLongOrNull() ?: 0L
                        }
                        response.code == 404 -> {
                            return@withContext Result.failure(
                                IllegalArgumentException("মিডিয়া ফাইলটি সার্ভারে পাওয়া যায়নি (HTTP 404)")
                            )
                        }
                        response.code == 401 || response.code == 403 -> {
                            return@withContext Result.failure(
                                IllegalArgumentException("সার্ভার অ্যাক্সেস অনুমোদিত নয় (HTTP ${response.code})")
                            )
                        }
                    }
                }
            } catch (e: UnknownHostException) {
                return@withContext Result.failure(
                    IllegalStateException("নেটওয়ার্ক ত্রুটি: ইন্টারনেট সংযোগ নেই বা সার্ভার পাওয়া যায়নি")
                )
            } catch (e: SocketTimeoutException) {
                return@withContext Result.failure(
                    IllegalStateException("নেটওয়ার্ক ত্রুটি: সার্ভার সাড়া দিতে দেরি করছে (Timeout)")
                )
            } catch (e: ConnectException) {
                return@withContext Result.failure(
                    IllegalStateException("নেটওয়ার্ক ত্রুটি: সার্ভারে সংযোগ স্থাপন করা যায়নি")
                )
            } catch (ignored: Exception) {}

            val baseSize = if (remoteLength > 0) remoteLength else 24_500_000L
            val formatList = mutableListOf<MediaQualityOption>()

            val detectedVideoExt = listOf("mp4", "webm", "m4v", "mov")
                .firstOrNull { path.lowercase().endsWith(".$it") } ?: "mp4"
            val detectedAudioExt = listOf("mp3", "m4a", "aac", "ogg", "wav")
                .firstOrNull { path.lowercase().endsWith(".$it") } ?: "mp3"

            if (linkResult.type == LinkType.DIRECT_VIDEO) {
                formatList.add(
                    MediaQualityOption(
                        label = "Original Quality (${detectedVideoExt.uppercase()})",
                        resolutionOrBitrate = "Original",
                        format = detectedVideoExt,
                        mediaType = MediaType.VIDEO,
                        estimatedSizeBytes = baseSize,
                        downloadUrl = validUrl
                    )
                )
            } else {
                formatList.add(
                    MediaQualityOption(
                        label = "Original Audio (${detectedAudioExt.uppercase()})",
                        resolutionOrBitrate = "Original",
                        format = detectedAudioExt,
                        mediaType = MediaType.AUDIO,
                        estimatedSizeBytes = if (remoteLength > 0) remoteLength else 7_370_000L,
                        downloadUrl = validUrl,
                        isAudioExtraction = false
                    )
                )
            }

            val analyzed = AnalyzedMedia(
                title = titleFromUrl.ifBlank { "AR Direct Media" },
                originalUrl = validUrl,
                platform = platform,
                author = host,
                duration = "Direct Stream",
                durationSeconds = 0L,
                thumbnail = null,
                formats = formatList,
                description = "${linkResult.message} • Direct Gallery Save"
            )
            Result.success(analyzed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startDownload(
        title: String,
        sourceUrl: String,
        option: MediaQualityOption,
        platform: String
    ): Long = withContext(Dispatchers.IO) {
        val safeTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
        val extension = option.format.lowercase()
        val downloadDir = File(context.filesDir, "Downloads").apply {
            if (!exists()) mkdirs()
        }
        val expectedFileName = DownloadService.sanitizeFileName(option.downloadUrl)
        val expectedFile = File(downloadDir, expectedFileName)

        val item = DownloadItem(
            title = safeTitle,
            sourceUrl = sourceUrl,
            downloadUrl = option.downloadUrl,
            mediaType = option.mediaType,
            quality = option.resolutionOrBitrate,
            format = extension,
            filePath = expectedFile.absolutePath,
            totalBytes = option.estimatedSizeBytes,
            downloadedBytes = 0L,
            progress = 0,
            speedBps = 0L,
            status = DownloadStatus.DOWNLOADING,
            sourcePlatform = platform
        )

        val downloadId = downloadDao.insertDownload(item)
        val createdItem = item.copy(id = downloadId)

        executeDownload(createdItem)
        downloadId
    }

    private fun executeDownload(item: DownloadItem, resumeFromPartial: Boolean = false) {
        pausedDownloads[item.id] = false
        cancelledDownloads[item.id] = false

        val job = repositoryScope.launch {
            var currentItem = item
            var lastTime = System.currentTimeMillis()
            var lastDownloaded = item.downloadedBytes

            try {
                val savedPath = DownloadService.downloadDirect(
                    context = context,
                    url = currentItem.downloadUrl,
                    onProgress = { received, total ->
                        val now = System.currentTimeMillis()
                        if (now - lastTime >= 200) {
                            val timeDiff = (now - lastTime).coerceAtLeast(1)
                            val delta = (received - lastDownloaded).coerceAtLeast(0L)
                            val speed = (delta * 1000) / timeDiff
                            val effectiveTotal = if (total > 0) total else currentItem.totalBytes.coerceAtLeast(received + 1L)
                            val progress = ((received * 100) / effectiveTotal).toInt().coerceIn(0, 99)

                            currentItem = currentItem.copy(
                                downloadedBytes = received,
                                totalBytes = effectiveTotal,
                                progress = progress,
                                speedBps = speed,
                                status = DownloadStatus.DOWNLOADING
                            )
                            downloadDao.updateDownload(currentItem)

                            lastTime = now
                            lastDownloaded = received
                        }
                    },
                    resumeFromPartial = resumeFromPartial,
                    shouldPause = { pausedDownloads[item.id] == true },
                    shouldCancel = { cancelledDownloads[item.id] == true }
                )

                val savedFile = File(savedPath)
                val finalSize = savedFile.length()

                currentItem = currentItem.copy(
                    filePath = savedPath,
                    downloadedBytes = finalSize,
                    totalBytes = finalSize,
                    progress = 100,
                    speedBps = 0L,
                    status = DownloadStatus.COMPLETED,
                    errorMessage = null
                )
                downloadDao.updateDownload(currentItem)

                // Export & register into device Gallery / MediaStore
                registerInGallery(
                    file = savedFile,
                    title = currentItem.title,
                    isVideo = currentItem.mediaType == MediaType.VIDEO,
                    extension = currentItem.format
                )
            } catch (paused: DownloadPausedException) {
                val partFile = currentItem.filePath?.let { File("$it.part") }
                val bytesOnDisk = if (partFile?.exists() == true) partFile.length() else currentItem.downloadedBytes
                val total = currentItem.totalBytes.coerceAtLeast(bytesOnDisk)
                val progress = if (total > 0L) ((bytesOnDisk * 100) / total).toInt().coerceIn(0, 99) else currentItem.progress
                currentItem = currentItem.copy(
                    downloadedBytes = bytesOnDisk,
                    progress = progress,
                    speedBps = 0L,
                    status = DownloadStatus.PAUSED
                )
                downloadDao.updateDownload(currentItem)
            } catch (cancelled: CancellationException) {
                if (pausedDownloads[item.id] != true) {
                    currentItem = currentItem.copy(
                        status = DownloadStatus.CANCELLED,
                        speedBps = 0L
                    )
                    downloadDao.updateDownload(currentItem)
                }
            } catch (e: Exception) {
                currentItem = currentItem.copy(
                    status = DownloadStatus.FAILED,
                    speedBps = 0L,
                    errorMessage = e.localizedMessage ?: "নেটওয়ার্ক বা ডাউনলোড ত্রুটি"
                )
                downloadDao.updateDownload(currentItem)
            } finally {
                activeDownloadJobs.remove(item.id)
            }
        }

        activeDownloadJobs[item.id] = job
    }

    private fun registerInGallery(
        file: File,
        title: String,
        isVideo: Boolean,
        extension: String = if (isVideo) "mp4" else "mp3"
    ) {
        if (!file.exists() || file.length() == 0L) return
        try {
            val mimeType = when (extension.lowercase()) {
                "webm" -> "video/webm"
                "mov" -> "video/quicktime"
                "m4v" -> "video/x-m4v"
                "mp4" -> "video/mp4"
                "m4a" -> "audio/mp4"
                "aac" -> "audio/aac"
                "ogg" -> "audio/ogg"
                "wav" -> "audio/wav"
                "mp3" -> "audio/mpeg"
                else -> if (isVideo) "video/mp4" else "audio/mpeg"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collection = if (isVideo) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
                val relativePath = if (isVideo) {
                    "${Environment.DIRECTORY_MOVIES}/AR_Videos"
                } else {
                    "${Environment.DIRECTORY_MUSIC}/AR_Audio"
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    put(MediaStore.MediaColumns.TITLE, title)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val itemUri = resolver.insert(collection, values)
                if (itemUri != null) {
                    try {
                        resolver.openOutputStream(itemUri)?.use { out ->
                            file.inputStream().use { input ->
                                input.copyTo(out)
                            }
                        }
                        val publishValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.IS_PENDING, 0)
                        }
                        resolver.update(itemUri, publishValues, null, null)
                    } catch (e: Exception) {
                        resolver.delete(itemUri, null, null)
                    }
                }
            } else {
                val baseDir = Environment.getExternalStoragePublicDirectory(
                    if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_MUSIC
                )
                val targetDir = File(baseDir, if (isVideo) "AR_Videos" else "AR_Audio").apply {
                    if (!exists()) mkdirs()
                }
                val publicFile = File(targetDir, file.name)
                try {
                    file.copyTo(publicFile, overwrite = true)
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(publicFile.absolutePath),
                        arrayOf(mimeType),
                        null
                    )
                } catch (ignored: Exception) {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(file.absolutePath),
                        arrayOf(mimeType),
                        null
                    )
                }
            }
        } catch (ignored: Exception) {}
    }

    suspend fun pauseDownload(id: Long) = withContext(Dispatchers.IO) {
        pausedDownloads[id] = true

        val item = downloadDao.getDownloadById(id)
        if (item != null) {
            downloadDao.updateDownload(item.copy(status = DownloadStatus.PAUSED, speedBps = 0L))
        }
    }

    suspend fun resumeDownload(id: Long) = withContext(Dispatchers.IO) {
        val item = downloadDao.getDownloadById(id) ?: return@withContext
        pausedDownloads[id] = false
        cancelledDownloads[id] = false
        val updated = item.copy(status = DownloadStatus.DOWNLOADING, errorMessage = null)
        downloadDao.updateDownload(updated)
        executeDownload(updated, resumeFromPartial = true)
    }

    suspend fun cancelDownload(id: Long) = withContext(Dispatchers.IO) {
        cancelledDownloads[id] = true
        pausedDownloads[id] = false
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)

        val item = downloadDao.getDownloadById(id)
        if (item != null) {
            item.filePath?.let { path ->
                File(path).delete()
                File("$path.part").delete()
            }
            downloadDao.updateDownload(
                item.copy(
                    status = DownloadStatus.CANCELLED,
                    speedBps = 0L,
                    progress = 0,
                    downloadedBytes = 0L
                )
            )
        }
    }

    suspend fun deleteDownload(id: Long) = withContext(Dispatchers.IO) {
        cancelDownload(id)
        val item = downloadDao.getDownloadById(id)
        item?.filePath?.let { path ->
            File(path).delete()
            File("$path.part").delete()
        }
        downloadDao.deleteById(id)
    }

    suspend fun retryDownload(id: Long) = withContext(Dispatchers.IO) {
        val item = downloadDao.getDownloadById(id) ?: return@withContext
        item.filePath?.let { path ->
            File(path).delete()
            File("$path.part").delete()
        }
        val resetItem = item.copy(
            status = DownloadStatus.DOWNLOADING,
            progress = 0,
            downloadedBytes = 0L,
            speedBps = 0L,
            errorMessage = null
        )
        downloadDao.updateDownload(resetItem)
        executeDownload(resetItem, resumeFromPartial = false)
    }

    suspend fun clearCompleted() = withContext(Dispatchers.IO) {
        downloadDao.clearCompleted()
    }
}
