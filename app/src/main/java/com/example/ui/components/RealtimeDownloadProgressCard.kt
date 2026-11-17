package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaType
import com.example.ui.theme.ButtonGoldGradient
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldGradient
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.ui.theme.SilverHighlight
import com.example.ui.theme.SuccessGreen

/**
 * A dedicated real-time Download Progress Card displaying both
 * CircularProgressIndicator and LinearProgressBar for active media downloads.
 */
@Composable
fun RealtimeDownloadProgressCard(
    item: DownloadItem,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onPlay: (DownloadItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val downloadedMb = item.downloadedBytes / (1024f * 1024f)
    val totalMb = item.totalBytes / (1024f * 1024f)
    val speedKbps = item.speedBps / 1024
    val speedText = if (speedKbps > 1024) String.format("%.1f MB/s", speedKbps / 1024f) else "$speedKbps KB/s"

    val remainingBytes = (item.totalBytes - item.downloadedBytes).coerceAtLeast(0L)
    val etaSeconds = if (item.speedBps > 0) remainingBytes / item.speedBps else 0L
    val etaText = if (etaSeconds in 1..3600) "~${etaSeconds}s remaining" else if (item.progress == 100) "Ready" else "Calculating..."

    val animatedProgress by animateFloatAsState(
        targetValue = (item.progress / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "realtime_progress_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = GoldMetallic)
            .testTag("realtime_download_progress_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalBlueDark),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = if (item.status == DownloadStatus.DOWNLOADING) {
                Brush.horizontalGradient(listOf(GoldPrimary, GoldMetallic, GoldLight))
            } else {
                Brush.horizontalGradient(listOf(SuccessGreen, GoldMetallic, SuccessGreen))
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            RoyalBlueSurface.copy(alpha = 0.9f),
                            RoyalBlueDeep.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header: Live Indicator & Platform
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (item.status) {
                                    DownloadStatus.DOWNLOADING -> RoyalBluePrimary
                                    DownloadStatus.PAUSED -> GoldDark.copy(alpha = 0.35f)
                                    else -> SuccessGreen.copy(alpha = 0.2f)
                                }
                            )
                            .border(
                                1.dp,
                                when (item.status) {
                                    DownloadStatus.DOWNLOADING -> GoldMetallic
                                    DownloadStatus.PAUSED -> GoldLight
                                    else -> SuccessGreen
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (item.status) {
                                DownloadStatus.DOWNLOADING -> {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GoldLight)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "DOWNLOADING LIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldLight,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                DownloadStatus.PAUSED -> {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PAUSED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldLight
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SAVED TO GALLERY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${item.sourcePlatform} • ${item.format.uppercase()}",
                        fontSize = 11.sp,
                        color = SilverHighlight.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // ETA or Quality
                Text(
                    text = item.quality,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldMetallic
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Visual Section: CircularProgressIndicator + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress Indicator Component
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .testTag("circular_progress_indicator_container"),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .size(70.dp)
                            .testTag("circular_progress_indicator"),
                        color = if (item.status == DownloadStatus.COMPLETED) SuccessGreen else GoldMetallic,
                        trackColor = RoyalBlueDeep,
                        strokeWidth = 5.dp,
                        strokeCap = StrokeCap.Round
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${item.progress}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (item.status == DownloadStatus.COMPLETED) SuccessGreen else Color.White
                        )
                        Text(
                            text = when (item.status) {
                                DownloadStatus.DOWNLOADING -> "ACTIVE"
                                DownloadStatus.PAUSED -> "PAUSED"
                                else -> "DONE"
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title & Real-time Metrics
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = GoldMetallic,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (item.status == DownloadStatus.DOWNLOADING) speedText else "Complete",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• $etaText",
                            fontSize = 11.sp,
                            color = SilverHighlight.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${String.format("%.1f", downloadedMb)} MB / ${String.format("%.1f", totalMb)} MB",
                        fontSize = 11.sp,
                        color = SilverHighlight.copy(alpha = 0.65f)
                    )
                }

                // Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.status == DownloadStatus.DOWNLOADING) {
                        IconButton(
                            onClick = { onPause(item.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RoyalBlueSurface)
                                .testTag("realtime_pause_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = GoldMetallic,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { onCancel(item.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RoyalBlueSurface)
                                .testTag("realtime_cancel_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = SilverHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (item.status == DownloadStatus.PAUSED) {
                        IconButton(
                            onClick = { onResume(item.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ButtonGoldGradient)
                                .testTag("realtime_resume_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = RoyalBlueDeep,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { onCancel(item.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RoyalBlueSurface)
                                .testTag("realtime_cancel_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = SilverHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (item.status == DownloadStatus.COMPLETED) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ButtonGoldGradient)
                                .clickable { onPlay(item) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("realtime_play_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = RoyalBlueDeep,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Play",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlueDeep
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Linear Progress Bar Component
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("realtime_linear_progress_bar"),
                    color = if (item.status == DownloadStatus.COMPLETED) SuccessGreen else GoldMetallic,
                    trackColor = RoyalBlueDeep,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gallery Auto-Save Status Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val galleryDest = if (item.mediaType == MediaType.AUDIO) "Music/AR_Audio" else "Movies/AR_Videos"
                Text(
                    text = if (item.status == DownloadStatus.COMPLETED) {
                        "✓ Saved to $galleryDest"
                    } else if (item.status == DownloadStatus.PAUSED) {
                        "⏸ Paused • Tap Play to resume"
                    } else {
                        "⚡ Saving to $galleryDest upon completion"
                    },
                    fontSize = 10.sp,
                    color = if (item.status == DownloadStatus.COMPLETED) SuccessGreen else GoldLight.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${item.progress}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldMetallic
                )
            }
        }
    }
}
