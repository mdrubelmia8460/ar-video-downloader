package com.example.ui.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ARDownloaderApp
import com.example.data.model.AudioTrack
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaQualityOption
import com.example.data.model.MediaType
import com.example.data.repository.DownloadRepository
import com.example.data.service.DownloadService
import com.example.data.service.LinkType
import com.example.ui.state.AppSettings
import com.example.ui.state.MainUiState
import com.example.ui.state.PlayerState
import com.example.ui.state.ScreenTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val repository: DownloadRepository = ARDownloaderApp.instance.repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressTrackingJob: Job? = null
    private val notifiedFailedIds = mutableSetOf<Long>()
    private var initialDownloadsLoaded = false

    init {
        observeDownloads()
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            repository.allDownloads.collect { list ->
                if (!initialDownloadsLoaded) {
                    notifiedFailedIds.addAll(
                        list.filter { it.status == DownloadStatus.FAILED }.map { it.id }
                    )
                    initialDownloadsLoaded = true
                } else {
                    val newFailure = list.firstOrNull {
                        it.status == DownloadStatus.FAILED &&
                            !notifiedFailedIds.contains(it.id) &&
                            !it.errorMessage.isNullOrBlank()
                    }
                    if (newFailure != null) {
                        notifiedFailedIds.add(newFailure.id)
                        _uiState.update {
                            it.copy(
                                snackbarMessage = newFailure.errorMessage,
                                isSnackbarError = true
                            )
                        }
                    }
                }
                _uiState.update { it.copy(downloads = list) }
            }
        }
        viewModelScope.launch {
            repository.completedCount.collect { count ->
                _uiState.update { it.copy(completedCount = count) }
            }
        }
        viewModelScope.launch {
            repository.totalBytesDownloaded.collect { total ->
                _uiState.update { it.copy(totalDownloadedBytes = total) }
            }
        }
    }

    fun selectTab(tab: ScreenTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun onUrlChange(newUrl: String) {
        _uiState.update { it.copy(inputUrl = newUrl, analysisError = null) }
    }

    fun analyzeUrl(urlToAnalyze: String? = null) {
        checkLink(urlToAnalyze)
    }

    fun checkLink(urlToAnalyze: String? = null) {
        val targetUrl = urlToAnalyze ?: _uiState.value.inputUrl
        val linkResult = DownloadService.validateLink(targetUrl)

        if (linkResult.type == LinkType.INVALID) {
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    analyzedMedia = null,
                    analysisError = linkResult.message,
                    snackbarMessage = linkResult.message,
                    isSnackbarError = true
                )
            }
            return
        }

        if (linkResult.type == LinkType.SOCIAL_MEDIA) {
            // Route to authorized platform metadata/oEmbed resolver or external/in-app playback
            Log.d("DownloadService", "Social URL: ${linkResult.url}")
            val socialError = if (DownloadService.isYouTubeUrl(linkResult.url)) {
                "YouTube লিংক সরাসরি ডাউনলোড করা অনুমোদিত নয়"
            } else {
                "শুধু সরাসরি অনুমোদিত মিডিয়া URL ডাউনলোড করা যাবে"
            }
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    analyzedMedia = null,
                    inputUrl = linkResult.url,
                    analysisError = socialError,
                    snackbarMessage = linkResult.message,
                    isSnackbarError = false
                )
            }
            return
        }

        if (linkResult.type == LinkType.DIRECT_VIDEO || linkResult.type == LinkType.DIRECT_AUDIO) {
            Log.d("DownloadService", "Media URL: ${linkResult.url}")
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    analysisError = null,
                    inputUrl = linkResult.url,
                    snackbarMessage = linkResult.message,
                    isSnackbarError = false
                )
            }
            val result = repository.analyzeUrl(linkResult.url)
            result.onSuccess { media ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analyzedMedia = media,
                        analysisError = null
                    )
                }
            }.onFailure { err ->
                val errorText = err.localizedMessage ?: "Failed to analyze media link"
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analysisError = errorText,
                        snackbarMessage = errorText,
                        isSnackbarError = true
                    )
                }
            }
        }
    }

    fun clearAnalysis() {
        _uiState.update { it.copy(analyzedMedia = null, analysisError = null) }
    }

    fun startDownload(option: MediaQualityOption) {
        val media = _uiState.value.analyzedMedia ?: return
        viewModelScope.launch {
            repository.startDownload(
                title = media.title,
                sourceUrl = media.originalUrl,
                option = option,
                platform = media.platform
            )
            _uiState.update {
                it.copy(
                    analyzedMedia = null,
                    snackbarMessage = "Download started: ${option.label}"
                )
            }
        }
    }

    fun quickDownloadVideo() {
        val media = _uiState.value.analyzedMedia ?: return
        val defaultVideo = media.formats.firstOrNull { it.mediaType == MediaType.VIDEO && it.resolutionOrBitrate == "720p" }
            ?: media.formats.firstOrNull { it.mediaType == MediaType.VIDEO }
        if (defaultVideo != null) {
            startDownload(defaultVideo)
        }
    }

    fun quickDownloadAudio() {
        val media = _uiState.value.analyzedMedia ?: return
        val defaultAudio = media.formats.firstOrNull { it.mediaType == MediaType.AUDIO && it.format == "mp3" }
            ?: media.formats.firstOrNull { it.mediaType == MediaType.AUDIO }
        if (defaultAudio != null) {
            startDownload(defaultAudio)
        }
    }

    fun pauseDownload(id: Long) {
        viewModelScope.launch { repository.pauseDownload(id) }
    }

    fun resumeDownload(id: Long) {
        viewModelScope.launch { repository.resumeDownload(id) }
    }

    fun cancelDownload(id: Long) {
        viewModelScope.launch { repository.cancelDownload(id) }
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch { repository.deleteDownload(id) }
    }

    fun retryDownload(id: Long) {
        viewModelScope.launch { repository.retryDownload(id) }
    }

    fun clearCompletedDownloads() {
        viewModelScope.launch { repository.clearCompleted() }
    }

    // Audio Player functionality (Real downloaded files only)
    fun playAudio(track: AudioTrack) {
        viewModelScope.launch {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null

                val file = File(track.filePath)
                if (file.exists() && file.length() > 0L) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(track.filePath)
                        prepare()
                        start()
                        setOnCompletionListener {
                            _uiState.update {
                                it.copy(playerState = it.playerState.copy(isPlaying = false, currentPositionMs = 0L))
                            }
                        }
                    }
                    val totalMs = mediaPlayer?.duration?.toLong()?.coerceAtLeast(1000L) ?: track.durationMs
                    _uiState.update {
                        it.copy(
                            playerState = PlayerState(
                                currentTrack = track,
                                isPlaying = true,
                                currentPositionMs = 0L,
                                totalDurationMs = totalMs
                            )
                        )
                    }
                    startProgressTracker()
                } else {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = "অডিও ফাইলটি স্টোরেজে পাওয়া যায়নি",
                            isSnackbarError = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        snackbarMessage = "অডিও চালু করা যায়নি: ${e.localizedMessage ?: "Unsupported format"}",
                        isSnackbarError = true
                    )
                }
            }
        }
    }

    fun togglePlayPause() {
        val current = _uiState.value.playerState
        if (current.currentTrack == null) return

        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                _uiState.update { it.copy(playerState = current.copy(isPlaying = false)) }
            } else {
                mediaPlayer!!.start()
                _uiState.update { it.copy(playerState = current.copy(isPlaying = true)) }
                startProgressTracker()
            }
        } else {
            playAudio(current.currentTrack)
        }
    }

    fun seekAudioTo(positionMs: Long) {
        val current = _uiState.value.playerState
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.seekTo(positionMs.toInt())
            } catch (ignored: Exception) {}
        }
        _uiState.update { it.copy(playerState = current.copy(currentPositionMs = positionMs)) }
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val currentPos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                _uiState.update {
                    it.copy(playerState = it.playerState.copy(currentPositionMs = currentPos))
                }
                delay(300)
            }
        }
    }

    // Browser navigation & media sniffing
    fun onBrowserUrlChange(url: String) {
        _uiState.update { it.copy(browserUrl = url) }
    }

    fun onMediaDetectedInBrowser(mediaUrl: String) {
        val list = _uiState.value.detectedMediaInBrowser.toMutableList()
        if (!list.contains(mediaUrl)) {
            list.add(0, mediaUrl)
            _uiState.update { it.copy(detectedMediaInBrowser = list.take(5)) }
        }
    }

    fun showSnackbar(message: String, isError: Boolean = false) {
        _uiState.update { it.copy(snackbarMessage = message, isSnackbarError = isError) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // Settings
    fun updateDefaultQuality(quality: String) {
        _uiState.update { it.copy(settings = it.settings.copy(defaultQuality = quality)) }
    }

    fun updateDefaultAudioFormat(format: String) {
        _uiState.update { it.copy(settings = it.settings.copy(defaultAudioFormat = format)) }
    }

    fun updateLanguage(lang: String) {
        _uiState.update { it.copy(settings = it.settings.copy(language = lang)) }
    }

    fun toggleTheme(isDark: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(isDarkMode = isDark)) }
    }

    fun toggleWifiOnly(wifiOnly: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(autoDownloadViaWifiOnly = wifiOnly)) }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        progressTrackingJob?.cancel()
    }
}
