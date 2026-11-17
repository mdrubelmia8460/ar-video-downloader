package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.example.data.model.DownloadItem
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AudioTrack
import com.example.data.model.MediaType
import com.example.ui.components.ARBottomNavigationBar
import com.example.ui.components.MiniAudioPlayer
import androidx.compose.ui.draw.shadow
import com.example.ui.screens.AboutUsScreen
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.ScreenTab
import com.example.ui.theme.ARDownloaderTheme
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldGradient
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle shared URL from external intent
        handleIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var showSplash by remember { mutableStateOf(true) }
            var showAboutUs by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(1200) // Smooth premium 3D splash branding
                showSplash = false
            }

            ARDownloaderTheme(darkTheme = uiState.settings.isDarkMode) {
                if (showSplash) {
                    SplashScreen()
                } else if (showAboutUs) {
                    AboutUsScreen(onNavigateBack = { showAboutUs = false })
                } else {
                    val snackbarHostState = remember { SnackbarHostState() }

                    LaunchedEffect(uiState.snackbarMessage) {
                        uiState.snackbarMessage?.let { msg ->
                            snackbarHostState.showSnackbar(msg)
                            viewModel.dismissSnackbar()
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Floating Mini Player when audio is active
                                if (uiState.playerState.currentTrack != null) {
                                    MiniAudioPlayer(
                                        playerState = uiState.playerState,
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onSeek = { viewModel.seekAudioTo(it) }
                                    )
                                }

                                val activeDownloads = uiState.downloads.count {
                                    it.status == com.example.data.model.DownloadStatus.DOWNLOADING
                                }

                                ARBottomNavigationBar(
                                    currentTab = uiState.currentTab,
                                    onTabSelected = { viewModel.selectTab(it) },
                                    activeDownloadsCount = activeDownloads
                                )
                            }
                        },
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState) { data ->
                                Snackbar(
                                    snackbarData = data,
                                    containerColor = if (uiState.isSnackbarError) ErrorRed else SuccessGreen,
                                    contentColor = Color.White
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(RoyalBlueDeep)
                                .padding(innerPadding)
                        ) {
                            when (uiState.currentTab) {
                                ScreenTab.HOME -> {
                                    HomeScreen(
                                        uiState = uiState,
                                        onUrlChange = { viewModel.onUrlChange(it) },
                                        onAnalyze = { viewModel.analyzeUrl() },
                                        onDownloadSelected = { viewModel.startDownload(it) },
                                        onCloseAnalysis = { viewModel.clearAnalysis() },
                                        onSelectPlatform = { platformUrl ->
                                            viewModel.onBrowserUrlChange(platformUrl)
                                            viewModel.selectTab(ScreenTab.BROWSER)
                                        },
                                        onPauseDownload = { viewModel.pauseDownload(it) },
                                        onResumeDownload = { viewModel.resumeDownload(it) },
                                        onCancelDownload = { viewModel.cancelDownload(it) },
                                        onDeleteDownload = { viewModel.deleteDownload(it) },
                                        onRetryDownload = { viewModel.retryDownload(it) },
                                        onPlayMedia = { item ->
                                            if (item.mediaType == MediaType.AUDIO) {
                                                val track = AudioTrack(
                                                    id = item.id,
                                                    title = item.title,
                                                    artist = "${item.sourcePlatform} Audio",
                                                    durationText = "03:45",
                                                    durationMs = 225000L,
                                                    filePath = item.filePath ?: "",
                                                    format = item.format
                                                )
                                                viewModel.playAudio(track)
                                            } else {
                                                playVideo(item)
                                            }
                                        },
                                        onOpenAboutUs = { showAboutUs = true },
                                        onWatchOnYouTube = { ytUrl -> openWatchOnYouTube(ytUrl) },
                                        onOpenDownloadsTab = { viewModel.selectTab(ScreenTab.DOWNLOADS) }
                                    )
                                }
                                ScreenTab.BROWSER -> {
                                    BrowserScreen(
                                        initialUrl = uiState.browserUrl,
                                        onDownloadUrlRequested = { sniffedUrl ->
                                            viewModel.onUrlChange(sniffedUrl)
                                            viewModel.selectTab(ScreenTab.HOME)
                                            viewModel.analyzeUrl(sniffedUrl)
                                        }
                                    )
                                }
                                ScreenTab.DOWNLOADS -> {
                                    DownloadsScreen(
                                        downloads = uiState.downloads,
                                        completedCount = uiState.completedCount,
                                        totalDownloadedBytes = uiState.totalDownloadedBytes,
                                        onPause = { viewModel.pauseDownload(it) },
                                        onResume = { viewModel.resumeDownload(it) },
                                        onCancel = { viewModel.cancelDownload(it) },
                                        onDelete = { viewModel.deleteDownload(it) },
                                        onRetry = { viewModel.retryDownload(it) },
                                        onPlayMedia = { item ->
                                            if (item.mediaType == MediaType.AUDIO) {
                                                val track = AudioTrack(
                                                    id = item.id,
                                                    title = item.title,
                                                    artist = "${item.sourcePlatform} Audio",
                                                    durationText = "03:45",
                                                    durationMs = 225000L,
                                                    filePath = item.filePath ?: "",
                                                    format = item.format
                                                )
                                                viewModel.playAudio(track)
                                                viewModel.selectTab(ScreenTab.MUSIC)
                                            } else {
                                                playVideo(item)
                                            }
                                        },
                                        onClearCompleted = { viewModel.clearCompletedDownloads() }
                                    )
                                }
                                ScreenTab.MUSIC -> {
                                    MusicScreen(
                                        downloads = uiState.downloads,
                                        playerState = uiState.playerState,
                                        onPlayTrack = { viewModel.playAudio(it) },
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onSeek = { viewModel.seekAudioTo(it) }
                                    )
                                }
                                ScreenTab.SETTINGS -> {
                                    SettingsScreen(
                                        settings = uiState.settings,
                                        onUpdateDefaultQuality = { viewModel.updateDefaultQuality(it) },
                                        onUpdateDefaultAudioFormat = { viewModel.updateDefaultAudioFormat(it) },
                                        onUpdateLanguage = { viewModel.updateLanguage(it) },
                                        onToggleTheme = { viewModel.toggleTheme(it) },
                                        onToggleWifiOnly = { viewModel.toggleWifiOnly(it) },
                                        onOpenAboutUs = { showAboutUs = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun playVideo(item: DownloadItem) {
        val path = item.filePath
        if (path.isNullOrBlank()) {
            viewModel.showSnackbar("Video file path is missing", isError = true)
            return
        }
        val file = File(path)
        if (!file.exists()) {
            viewModel.showSnackbar("Video file not found on storage", isError = true)
            return
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(Intent.createChooser(viewIntent, "Play Video with"))
        } catch (e: Exception) {
            viewModel.showSnackbar("No compatible video player app found: ${e.localizedMessage}", isError = true)
        }
    }

    private fun openWatchOnYouTube(url: String) {
        val parsedUri = try {
            Uri.parse(url)
        } catch (e: Exception) {
            viewModel.showSnackbar("সঠিক YouTube URL নয়", isError = true)
            return
        }
        try {
            val ytIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                setPackage("com.google.android.youtube")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(ytIntent)
        } catch (e: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(browserIntent)
            } catch (ex: Exception) {
                viewModel.onBrowserUrlChange(url)
                viewModel.selectTab(ScreenTab.BROWSER)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                viewModel.onUrlChange(sharedText.trim())
                viewModel.selectTab(ScreenTab.HOME)
                viewModel.analyzeUrl(sharedText.trim())
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(RoyalBlueDark, RoyalBlueDeep)
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Official AR Logo Emblem with unclipped safe framing
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(20.dp, RoundedCornerShape(30.dp), spotColor = GoldMetallic)
                    .border(2.5.dp, GoldGradient, RoundedCornerShape(30.dp))
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFF021736)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ar_downloader_logo),
                    contentDescription = "AR All Video Downloader Official Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(132.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AR",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                style = androidx.compose.ui.text.TextStyle(brush = GoldGradient)
            )

            Text(
                text = "ALL VIDEO DOWNLOADER",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "OFFICIAL BRAND EDITION • CREATED BY AR",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldLight.copy(alpha = 0.9f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = GoldMetallic,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
