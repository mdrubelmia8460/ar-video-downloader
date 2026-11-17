package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AudioTrack
import com.example.data.model.DownloadItem
import com.example.data.model.MediaQualityOption
import com.example.data.model.MediaType
import com.example.data.model.DownloadStatus
import com.example.data.service.DownloadService
import com.example.data.service.LinkType
import com.example.ui.components.ARBrandHeader
import com.example.ui.components.DownloadItemCard
import com.example.ui.components.DownloadStatsCard
import com.example.ui.components.MediaAnalysisResultCard
import com.example.ui.components.RealtimeDownloadProgressCard
import com.example.ui.components.SupportedPlatformsRow
import com.example.ui.components.VideoUrlInputCard
import com.example.ui.state.MainUiState
import com.example.ui.theme.ButtonGoldGradient
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldGradient
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.ui.theme.SilverHighlight

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onUrlChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onDownloadSelected: (MediaQualityOption) -> Unit,
    onCloseAnalysis: () -> Unit,
    onSelectPlatform: (String) -> Unit,
    onPauseDownload: (Long) -> Unit,
    onResumeDownload: (Long) -> Unit,
    onCancelDownload: (Long) -> Unit,
    onDeleteDownload: (Long) -> Unit,
    onRetryDownload: (Long) -> Unit,
    onPlayMedia: (DownloadItem) -> Unit,
    onOpenAboutUs: () -> Unit = {},
    onWatchOnYouTube: (String) -> Unit = {},
    onOpenDownloadsTab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Brand Header
        item {
            ARBrandHeader(onLogoClick = onOpenAboutUs)
        }

        // 2. Video URL Input Field & Download Action Button
        item {
            VideoUrlInputCard(
                url = uiState.inputUrl,
                isAnalyzing = uiState.isAnalyzing,
                onUrlChange = onUrlChange,
                onAnalyze = onAnalyze,
                onWatchOnYouTube = onWatchOnYouTube,
                onOpenInAppBrowser = onSelectPlatform
            )
        }

        // 2.5 Real-time Active Download Progress Component (CircularProgressIndicator & ProgressBar)
        val activeDownload = uiState.downloads.firstOrNull { it.status == DownloadStatus.DOWNLOADING }
            ?: uiState.downloads.firstOrNull { it.status == DownloadStatus.PAUSED }

        if (activeDownload != null) {
            item {
                RealtimeDownloadProgressCard(
                    item = activeDownload,
                    onPause = onPauseDownload,
                    onResume = onResumeDownload,
                    onCancel = onCancelDownload,
                    onPlay = onPlayMedia
                )
            }
        }

        // 3. Official Home Screen Branding Showcase
        item {
            ARHomeBrandShowcase(
                onOpenAboutUs = onOpenAboutUs,
                onOpenDownloads = onOpenDownloadsTab
            )
        }

        // 4. Analysis Error / Notice card if any
        if (uiState.analysisError != null) {
            item {
                val validated = DownloadService.validateLink(uiState.inputUrl)
                val isSocialLink = validated.type == LinkType.SOCIAL_MEDIA
                val isYouTube = DownloadService.isYouTubeUrl(validated.url)
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("analysis_error_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSocialLink) RoyalBlueDark else Color(0xFF7F1D1D).copy(alpha = 0.85f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSocialLink) GoldMetallic.copy(alpha = 0.7f) else Color(0xFFEF4444).copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = uiState.analysisError,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isSocialLink) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "YouTube ও সোশ্যাল মিডিয়ার সুরক্ষা ও Terms of Service অনুযায়ী সরাসরি ডাউনলোড অনুমোদিত নয়। অনুগ্রহ করে সরাসরি .mp4/.webm (ভিডিও) বা .mp3/.m4a (অডিও) লিংক ব্যবহার করুন, অথবা নিচ থেকে ভিডিওটি দেখুন।",
                                color = SilverHighlight.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isYouTube) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFDC2626))
                                            .clickable { onWatchOnYouTube(validated.url) }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                            .testTag("banner_watch_on_youtube_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Watch on YouTube",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ButtonGoldGradient)
                                        .clickable { onSelectPlatform(validated.url) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .testTag("open_social_in_browser_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "ইন-অ্যাপ ব্রাউজারে দেখুন",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlueDeep
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Analysis Result Card (Video & Audio Formats Selection)
        if (uiState.analyzedMedia != null) {
            item {
                MediaAnalysisResultCard(
                    media = uiState.analyzedMedia,
                    onDownloadSelected = onDownloadSelected,
                    onClose = onCloseAnalysis,
                    onOpenBrowser = onSelectPlatform
                )
            }
        }

        // 6. Download Statistics Card
        item {
            DownloadStatsCard(
                completedCount = uiState.completedCount,
                totalBytes = uiState.totalDownloadedBytes
            )
        }

        // 7. Supported Platforms Row
        item {
            SupportedPlatformsRow(
                onSelectPlatform = onSelectPlatform
            )
        }

        // 8. Recent Downloads Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = GoldMetallic,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECENT DOWNLOADS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "${uiState.downloads.size} items • View All",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenDownloadsTab() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("view_all_downloads_btn")
                )
            }
        }

        if (uiState.downloads.isEmpty()) {
            item {
                EmptyRecentDownloadsBanner()
            }
        } else {
            items(uiState.downloads.take(4), key = { it.id }) { item ->
                DownloadItemCard(
                    item = item,
                    onPause = onPauseDownload,
                    onResume = onResumeDownload,
                    onCancel = onCancelDownload,
                    onDelete = onDeleteDownload,
                    onRetry = onRetryDownload,
                    onPlay = onPlayMedia
                )
            }
        }
    }
}

@Composable
private fun ARHomeBrandShowcase(
    onOpenAboutUs: () -> Unit,
    onOpenDownloads: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_brand_showcase_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalBlueDark),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(listOf(GoldDark, GoldMetallic, GoldDark))
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            RoyalBlueSurface.copy(alpha = 0.85f),
                            RoyalBlueDeep.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ButtonGoldGradient)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OFFICIAL BRAND",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalBlueDeep
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ROYAL CROWN EDITION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "AR All Video Downloader",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Ultra HD 1080p Engine • Hi-Fi Audio Extraction",
                        fontSize = 11.sp,
                        color = SilverHighlight.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ButtonGoldGradient)
                                .clickable { onOpenDownloads() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("quick_my_downloads_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = RoyalBlueDeep,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "My Downloads",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlueDeep
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RoyalBlueSurface)
                                .border(1.dp, GoldMetallic.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable { onOpenAboutUs() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("home_about_brand_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Brand Info",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // The Official AR Logo Emblem with unclipped ContentScale.Fit
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = GoldMetallic)
                        .border(2.dp, GoldGradient, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF021736))
                        .clickable { onOpenAboutUs() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ar_downloader_logo),
                        contentDescription = "AR All Video Downloader Official Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(70.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRecentDownloadsBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalBlueDark.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E325C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(RoyalBlueSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = GoldMetallic,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No Downloads Yet",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Paste a direct .mp4, .webm, .mp3, or .m4a media link above to download directly to your Gallery.",
                fontSize = 12.sp,
                color = SilverHighlight.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
