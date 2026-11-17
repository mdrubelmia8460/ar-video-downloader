package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.ui.theme.SilverHighlight
import com.example.ui.theme.SuccessGreen

@Composable
fun DownloadItemCard(
    item: DownloadItem,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onRetry: (Long) -> Unit,
    onPlay: (DownloadItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val downloadedMb = item.downloadedBytes / (1024f * 1024f)
    val totalMb = item.totalBytes / (1024f * 1024f)
    val speedKbps = item.speedBps / 1024
    val speedText = if (speedKbps > 1024) String.format("%.1f MB/s", speedKbps / 1024f) else "$speedKbps KB/s"

    val animatedProgress by animateFloatAsState(
        targetValue = (item.progress / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "download_progress_${item.id}"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("download_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalBlueDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.status == DownloadStatus.DOWNLOADING) GoldMetallic else Color(0xFF1E325C)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading Visual: CircularProgressIndicator for active downloads
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("avatar_container_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.status == DownloadStatus.DOWNLOADING) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("circular_progress_item_${item.id}"),
                                color = GoldMetallic,
                                trackColor = RoyalBlueDeep,
                                strokeWidth = 3.dp,
                                strokeCap = StrokeCap.Round
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when (item.status) {
                                        DownloadStatus.COMPLETED -> SuccessGreen.copy(alpha = 0.2f)
                                        DownloadStatus.FAILED -> ErrorRed.copy(alpha = 0.2f)
                                        DownloadStatus.DOWNLOADING -> RoyalBlueDeep
                                        else -> if (item.mediaType == MediaType.VIDEO) RoyalBluePrimary else GoldDark.copy(alpha = 0.5f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (item.status) {
                                        DownloadStatus.COMPLETED -> SuccessGreen
                                        DownloadStatus.FAILED -> ErrorRed
                                        DownloadStatus.DOWNLOADING -> GoldMetallic.copy(alpha = 0.6f)
                                        else -> GoldDark
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.status == DownloadStatus.DOWNLOADING) {
                                Text(
                                    text = "${item.progress}%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldLight
                                )
                            } else {
                                Icon(
                                    imageVector = when (item.status) {
                                        DownloadStatus.COMPLETED -> Icons.Default.CheckCircle
                                        DownloadStatus.FAILED -> Icons.Default.Error
                                        else -> if (item.mediaType == MediaType.VIDEO) Icons.Default.Movie else Icons.Default.Audiotrack
                                    },
                                    contentDescription = null,
                                    tint = when (item.status) {
                                        DownloadStatus.COMPLETED -> SuccessGreen
                                        DownloadStatus.FAILED -> ErrorRed
                                        else -> GoldMetallic
                                    },
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${item.sourcePlatform} • ${item.format.uppercase()} ${item.quality}",
                                fontSize = 11.sp,
                                color = GoldLight.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Status chip
                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-time Progress Bar & Status
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.status == DownloadStatus.DOWNLOADING) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GoldMetallic)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Downloading ${item.progress}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        } else if (item.status == DownloadStatus.COMPLETED) {
                            Text(
                                text = "Completed (100%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen
                            )
                        } else if (item.status == DownloadStatus.PAUSED) {
                            Text(
                                text = "Paused at ${item.progress}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GoldLight
                            )
                        } else if (item.status == DownloadStatus.FAILED) {
                            Text(
                                text = "Failed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ErrorRed
                            )
                        } else {
                            Text(
                                text = "Cancelled",
                                fontSize = 11.sp,
                                color = SilverHighlight.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (item.status == DownloadStatus.DOWNLOADING) {
                        val remainingBytes = (item.totalBytes - item.downloadedBytes).coerceAtLeast(0L)
                        val etaSec = if (item.speedBps > 0) remainingBytes / item.speedBps else 0L
                        val etaText = if (etaSec in 1..3600) "~${etaSec}s left" else ""
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = GoldMetallic,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$speedText ${if (etaText.isNotBlank()) "• $etaText" else ""}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = SilverHighlight.copy(alpha = 0.9f)
                            )
                        }
                    } else if (item.status == DownloadStatus.COMPLETED) {
                        val destFolder = if (item.mediaType == MediaType.AUDIO) "Music/AR_Audio" else "Movies/AR_Videos"
                        Text(
                            text = "${String.format("%.1f", totalMb)} MB • $destFolder",
                            fontSize = 10.sp,
                            color = SuccessGreen.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // High-visibility animated LinearProgressIndicator (ProgressBar)
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(3.5.dp))
                        .testTag("linear_progress_${item.id}"),
                    color = when (item.status) {
                        DownloadStatus.COMPLETED -> SuccessGreen
                        DownloadStatus.FAILED -> ErrorRed
                        DownloadStatus.PAUSED -> GoldPrimary
                        else -> GoldMetallic
                    },
                    trackColor = RoyalBlueDeep,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details and actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dlText = String.format("%.1f", downloadedMb)
                    val totText = String.format("%.1f", totalMb)
                    when (item.status) {
                        DownloadStatus.DOWNLOADING -> {
                            Text(
                                text = "${item.progress}% • $dlText/$totText MB • $speedText",
                                fontSize = 11.sp,
                                color = SilverHighlight.copy(alpha = 0.85f)
                            )
                        }
                        DownloadStatus.COMPLETED -> {
                            Text(
                                text = "Downloaded: $totText MB • Ready to play",
                                fontSize = 11.sp,
                                color = SuccessGreen
                            )
                        }
                        DownloadStatus.PAUSED -> {
                            Text(
                                text = "Paused • $dlText/$totText MB",
                                fontSize = 11.sp,
                                color = GoldLight
                            )
                        }
                        DownloadStatus.FAILED -> {
                            Text(
                                text = item.errorMessage ?: "Failed to download",
                                fontSize = 11.sp,
                                color = ErrorRed,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DownloadStatus.CANCELLED -> {
                            Text(
                                text = "Cancelled",
                                fontSize = 11.sp,
                                color = SilverHighlight.copy(alpha = 0.6f)
                            )
                        }
                        else -> {
                            Text(
                                text = "Pending...",
                                fontSize = 11.sp,
                                color = SilverHighlight.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Control Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (item.status) {
                        DownloadStatus.DOWNLOADING -> {
                            IconButton(
                                onClick = { onPause(item.id) },
                                modifier = Modifier.size(32.dp).testTag("pause_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = GoldMetallic,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onCancel(item.id) },
                                modifier = Modifier.size(32.dp).testTag("cancel_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = SilverHighlight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            IconButton(
                                onClick = { onResume(item.id) },
                                modifier = Modifier.size(32.dp).testTag("resume_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { onCancel(item.id) },
                                modifier = Modifier.size(32.dp).testTag("cancel_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = SilverHighlight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier.size(32.dp).testTag("delete_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = SilverHighlight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DownloadStatus.COMPLETED -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ButtonGoldGradient)
                                    .clickable { onPlay(item) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("play_media_${item.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = RoyalBlueDeep,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Play",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlueDeep
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier.size(28.dp).testTag("delete_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = SilverHighlight.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DownloadStatus.FAILED -> {
                            IconButton(
                                onClick = { onRetry(item.id) },
                                modifier = Modifier.size(32.dp).testTag("retry_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    tint = GoldMetallic,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier.size(32.dp).testTag("delete_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = SilverHighlight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        else -> {
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier.size(32.dp).testTag("delete_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = SilverHighlight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: DownloadStatus) {
    val (label, bg, textCol) = when (status) {
        DownloadStatus.DOWNLOADING -> Triple("DOWNLOADING", RoyalBluePrimary, GoldLight)
        DownloadStatus.COMPLETED -> Triple("DONE", SuccessGreen.copy(alpha = 0.2f), SuccessGreen)
        DownloadStatus.PAUSED -> Triple("PAUSED", GoldDark.copy(alpha = 0.3f), GoldMetallic)
        DownloadStatus.FAILED -> Triple("ERROR", ErrorRed.copy(alpha = 0.2f), ErrorRed)
        DownloadStatus.CANCELLED -> Triple("CANCELLED", Color.Gray.copy(alpha = 0.2f), SilverHighlight)
        DownloadStatus.IDLE -> Triple("READY", RoyalBlueSurface, SilverHighlight)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textCol,
            letterSpacing = 0.5.sp
        )
    }
}
