package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.service.DownloadService
import com.example.data.service.LinkType
import com.example.ui.theme.ButtonGoldGradient
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.ui.theme.SilverHighlight
import com.example.ui.theme.SuccessGreen

@Composable
fun VideoUrlInputCard(
    url: String,
    isAnalyzing: Boolean,
    onUrlChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onWatchOnYouTube: (String) -> Unit = {},
    onOpenInAppBrowser: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val linkResult = remember(url) { DownloadService.validateLink(url) }
    val isYouTube = remember(url) { DownloadService.isYouTubeUrl(url) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RoyalBlueDark)
            .border(1.dp, GoldDark, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("url_input_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = GoldMetallic,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PASTE VIDEO / AUDIO LINK",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight,
                    letterSpacing = 0.5.sp
                )
            }

            // Paste button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(RoyalBlueSurface)
                    .clickable {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            onUrlChange(clip.trim())
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("paste_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = GoldLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PASTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input TextField
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            placeholder = {
                Text(
                    text = "Enter or paste video URL (e.g. https://...)",
                    color = SilverHighlight.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = GoldMetallic,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAnalyze() }
            ),
            trailingIcon = {
                if (url.isNotEmpty()) {
                    IconButton(
                        onClick = { onUrlChange("") },
                        modifier = Modifier.testTag("clear_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = SilverHighlight
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = RoyalBlueDeep,
                unfocusedContainerColor = RoyalBlueDeep,
                focusedBorderColor = GoldMetallic,
                unfocusedBorderColor = Color(0xFF243B6B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = GoldMetallic
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("url_text_field")
        )

        if (url.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            val isError = linkResult.type == LinkType.INVALID
            val statusColor = when (linkResult.type) {
                LinkType.DIRECT_VIDEO, LinkType.DIRECT_AUDIO -> SuccessGreen
                LinkType.SOCIAL_MEDIA -> GoldMetallic
                LinkType.INVALID -> ErrorRed
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.14f))
                    .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("link_validation_banner"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = linkResult.message,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }

        if (url.isNotBlank() && isYouTube) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorRed)
                        .clickable { onWatchOnYouTube(linkResult.url) }
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                        .testTag("watch_on_youtube_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = "Watch on YouTube",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoyalBlueSurface)
                        .border(1.dp, GoldMetallic.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .clickable { onOpenInAppBrowser(linkResult.url) }
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                        .testTag("watch_youtube_in_app_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Watch in App",
                            tint = GoldLight,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "In-App Player",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Supported Direct Media: .MP4 • .WebM • .M4V • .MOV • .MP3 • .M4A • .AAC • .OGG • .WAV",
                fontSize = 10.sp,
                color = SilverHighlight.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Download Media Button with 3D Gold Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ButtonGoldGradient)
                .clickable(enabled = !isAnalyzing) { onAnalyze() }
                .testTag("download_button"),
            contentAlignment = Alignment.Center
        ) {
            if (isAnalyzing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = RoyalBlueDeep,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "RETRIEVING MEDIA STREAMS...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlueDeep
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = RoyalBlueDeep,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalBlueDeep,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SupportedPlatformsRow(
    onSelectPlatform: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val platforms = listOf(
        Pair("YouTube", "https://www.youtube.com"),
        Pair("TikTok", "https://www.tiktok.com"),
        Pair("Facebook", "https://www.facebook.com"),
        Pair("Instagram", "https://www.instagram.com"),
        Pair("Vimeo", "https://vimeo.com"),
        Pair("Dailymotion", "https://www.dailymotion.com")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "SUPPORTED BROWSER SOURCES & DIRECT DOWNLOADS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GoldLight.copy(alpha = 0.9f),
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Direct MP4/WebM videos save to Movies/AR_Videos and MP3/M4A audios save to Music/AR_Audio. YouTube links open in the YouTube app or In-App Browser.",
            fontSize = 11.sp,
            color = SilverHighlight.copy(alpha = 0.75f),
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            platforms.take(3).forEach { (name, url) ->
                PlatformChip(name = name, onClick = { onSelectPlatform(url) })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            platforms.drop(3).forEach { (name, url) ->
                PlatformChip(name = name, onClick = { onSelectPlatform(url) })
            }
        }
    }
}

@Composable
private fun PlatformChip(
    name: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RoyalBlueDark)
            .border(1.dp, Color(0xFF203562), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("platform_chip_${name.lowercase().replace(" ", "_").replace("(", "").replace(")", "")}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SilverHighlight
        )
    }
}
