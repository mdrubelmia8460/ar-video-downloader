package com.example.data.service

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

enum class LinkType {
    DIRECT_VIDEO,
    DIRECT_AUDIO,
    SOCIAL_MEDIA,
    INVALID
}

data class LinkResult(
    val type: LinkType,
    val message: String,
    val url: String
)

class DownloadPausedException : CancellationException("Download paused")

object DownloadService {
    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun isStatusValid(status: Int): Boolean = status in 200..399

    fun isMediaContentType(contentType: String?): Boolean {
        val normalized = contentType?.lowercase().orEmpty()
        return normalized.startsWith("video/") ||
            normalized.startsWith("audio/") ||
            normalized.contains("application/octet-stream")
    }

    fun isYouTubeUrl(url: String): Boolean {
        var value = url.trim()
        if (value.isEmpty()) return false
        if (!value.startsWith("http://", ignoreCase = true) &&
            !value.startsWith("https://", ignoreCase = true)
        ) {
            value = "https://$value"
        }
        val host = try {
            URI(value).host?.lowercase().orEmpty()
        } catch (e: Exception) {
            ""
        }
        return host == "youtube.com" ||
            host.endsWith(".youtube.com") ||
            host == "youtu.be" ||
            host.endsWith(".youtu.be")
    }

    fun sanitizeFileName(url: String, nowMs: Long = System.currentTimeMillis()): String {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            try {
                URI(url.replace(" ", "%20"))
            } catch (ignored: Exception) {
                null
            }
        }
        val segments = uri?.path
            ?.split('/')
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val rawName = if (segments.isNotEmpty()) {
            segments.last()
        } else {
            "AR_media_$nowMs"
        }
        val sanitized = rawName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return sanitized.ifBlank { "AR_media_$nowMs" }
    }

    private val socialHosts = listOf(
        "facebook.com",
        "fb.watch",
        "fb.com",
        "instagram.com",
        "tiktok.com",
        "vm.tiktok.com",
        "youtube.com",
        "youtu.be"
    )

    private val videoExtensions = listOf(
        ".mp4",
        ".webm",
        ".m4v",
        ".mov"
    )

    private val audioExtensions = listOf(
        ".mp3",
        ".m4a",
        ".aac",
        ".ogg",
        ".wav"
    )

    fun validateLink(input: String): LinkResult {
        var value = input.trim()

        if (value.isEmpty()) {
            return LinkResult(
                type = LinkType.INVALID,
                message = "লিংক খালি রাখা যাবে না",
                url = value
            )
        }

        // Add HTTPS if user omitted the scheme.
        if (!value.startsWith("http://", ignoreCase = true) &&
            !value.startsWith("https://", ignoreCase = true)
        ) {
            value = "https://$value"
        }

        val uri = try {
            URI(value)
        } catch (e: Exception) {
            null
        }

        val scheme = uri?.scheme?.lowercase()
        val hostValue = uri?.host

        if (uri == null ||
            uri.rawAuthority.isNullOrEmpty() ||
            hostValue.isNullOrEmpty() ||
            !uri.userInfo.isNullOrEmpty() ||
            scheme !in listOf("http", "https")
        ) {
            return LinkResult(
                type = LinkType.INVALID,
                message = "সঠিক URL দিন",
                url = value
            )
        }

        val host = hostValue.lowercase()
        val path = (uri.path ?: "").lowercase()

        val isSocial = socialHosts.any { domain ->
            host == domain || host.endsWith(".$domain")
        }

        if (isSocial) {
            return LinkResult(
                type = LinkType.SOCIAL_MEDIA,
                message = "সোশ্যাল মিডিয়া লিংক শনাক্ত হয়েছে",
                url = uri.toString()
            )
        }

        if (videoExtensions.any { path.endsWith(it) }) {
            return LinkResult(
                type = LinkType.DIRECT_VIDEO,
                message = "ভিডিও লিংক শনাক্ত হয়েছে",
                url = uri.toString()
            )
        }

        if (audioExtensions.any { path.endsWith(it) }) {
            return LinkResult(
                type = LinkType.DIRECT_AUDIO,
                message = "অডিও লিংক শনাক্ত হয়েছে",
                url = uri.toString()
            )
        }

        return LinkResult(
            type = LinkType.INVALID,
            message = "এটি সরাসরি মিডিয়া লিংক নয়",
            url = uri.toString()
        )
    }

    suspend fun downloadDirect(
        context: Context,
        url: String,
        onProgress: suspend (received: Long, total: Long) -> Unit = { _, _ -> },
        resumeFromPartial: Boolean = false,
        shouldPause: () -> Boolean = { false },
        shouldCancel: () -> Boolean = { false }
    ): String = withContext(Dispatchers.IO) {
        val result = validateLink(url)

        if (result.type != LinkType.DIRECT_VIDEO &&
            result.type != LinkType.DIRECT_AUDIO
        ) {
            throw IllegalArgumentException(
                "শুধু সরাসরি অনুমোদিত মিডিয়া URL ডাউনলোড করা যাবে"
            )
        }

        val directory = context.filesDir
        val downloadDir = File(directory, "Downloads")
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        val fileName = sanitizeFileName(result.url)
        val tempFile = File(downloadDir, "$fileName.part")
        val finalFile = File(downloadDir, fileName)

        if (!resumeFromPartial && tempFile.exists()) {
            tempFile.delete()
        }

        val existingBytes = if (resumeFromPartial && tempFile.exists()) tempFile.length() else 0L

        try {
            val requestBuilder = Request.Builder()
                .url(result.url)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                )
                .header("Accept", "*/*")
                .header("Accept-Language", "en-US,en;q=0.9")

            if (existingBytes > 0L) {
                requestBuilder.header("Range", "bytes=$existingBytes-")
            }

            val response = try {
                httpClient.newCall(requestBuilder.build()).execute()
            } catch (e: UnknownHostException) {
                throw IOException("নেটওয়ার্ক ত্রুটি: ইন্টারনেট সংযোগ নেই বা সার্ভার পাওয়া যায়নি", e)
            } catch (e: SocketTimeoutException) {
                throw IOException("নেটওয়ার্ক ত্রুটি: সার্ভার সাড়া দিতে দেরি করছে (Timeout)", e)
            } catch (e: ConnectException) {
                throw IOException("নেটওয়ার্ক ত্রুটি: সার্ভারে সংযোগ স্থাপন করা যায়নি", e)
            } catch (e: SSLException) {
                throw IOException("নিরাপত্তা ত্রুটি: SSL সংযোগ ব্যর্থ হয়েছে", e)
            } catch (e: IOException) {
                throw IOException("নেটওয়ার্ক ত্রুটি: ${e.localizedMessage ?: "সংযোগ বিচ্ছিন্ন হয়েছে"}", e)
            }

            response.use { resp ->
                val contentType = resp.header("Content-Type")?.lowercase().orEmpty()
                val validType = isMediaContentType(contentType)
                val statusCode = resp.code

                if (statusCode == 401 || statusCode == 403) {
                    throw IOException("সার্ভার অ্যাক্সেস অনুমোদিত নয় (HTTP $statusCode)")
                }
                if (statusCode == 404) {
                    throw IOException("মিডিয়া ফাইলটি সার্ভারে পাওয়া যায়নি (HTTP 404)")
                }
                if (statusCode !in 200..299 || !validType || resp.body == null) {
                    throw IOException(
                        "সার্ভার থেকে আসল মিডিয়া ফাইল পাওয়া যায়নি"
                    )
                }

                val body = resp.body!!
                val isAppending = existingBytes > 0L && statusCode == 206
                val startOffset = if (isAppending) existingBytes else 0L
                val bodyLength = body.contentLength()
                val total = if (bodyLength > 0L) startOffset + bodyLength else -1L
                val buffer = ByteArray(64 * 1024)
                var received = startOffset

                body.byteStream().use { input ->
                    FileOutputStream(tempFile, isAppending).use { output ->
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            coroutineContext.ensureActive()
                            if (shouldCancel()) {
                                throw CancellationException("Download cancelled")
                            }
                            if (shouldPause()) {
                                output.flush()
                                throw DownloadPausedException()
                            }
                            output.write(buffer, 0, read)
                            received += read
                            onProgress(received, total)
                        }
                        output.flush()
                    }
                }
            }

            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw IOException("ডাউনলোড করা ফাইল খালি")
            }

            if (finalFile.exists()) {
                finalFile.delete()
            }

            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            finalFile.absolutePath
        } catch (paused: DownloadPausedException) {
            // Keep tempFile (.part) intact so Resume continues from existing bytes
            throw paused
        } catch (t: Throwable) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw t
        }
    }
}
