package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ButtonGoldGradient
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldGradient
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.ui.theme.SilverHighlight
import com.example.ui.theme.SuccessGreen

@Composable
fun AboutUsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RoyalBlueDeep)
            .testTag("about_us_screen")
    ) {
        // Top App Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalBlueDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("about_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldMetallic
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "ABOUT US",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldLight,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Official Branding & Identity",
                        fontSize = 11.sp,
                        color = SilverHighlight.copy(alpha = 0.7f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Official Logo Presentation Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("official_logo_presentation_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = RoyalBlueDark),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(listOf(GoldDark, GoldMetallic, GoldDark))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        RoyalBlueSurface.copy(alpha = 0.7f),
                                        RoyalBlueDeep.copy(alpha = 0.95f)
                                    )
                                )
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Official Emblem Frame
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = GoldMetallic)
                                .border(2.5.dp, GoldGradient, RoundedCornerShape(32.dp))
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color(0xFF021736)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ar_downloader_logo),
                                contentDescription = "AR All Video Downloader Official Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(142.dp)
                                    .testTag("official_logo_image")
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Verified Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(ButtonGoldGradient)
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Official Emblem",
                                    tint = RoyalBlueDeep,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OFFICIAL BRAND IDENTITY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoyalBlueDeep,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "AR ALL VIDEO DOWNLOADER",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Royal Blue & Gold Edition • Created by AR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldLight,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "The ultimate high-performance media engine designed for downloading, extracting, organizing, and playing videos and music in pristine quality.",
                            fontSize = 12.sp,
                            color = SilverHighlight.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 2. Emblem Design Symbolism Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = RoyalBlueDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E325C))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldMetallic,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EMBLEM SYMBOLISM & IDENTITY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SymbolismItem(
                            icon = Icons.Default.Star,
                            title = "Royal Golden Crown",
                            description = "Symbolizes industry-leading performance, sovereign authority in video extraction, and premium luxury design."
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SymbolismItem(
                            icon = Icons.Default.Verified,
                            title = "Monogram 'AR'",
                            description = "The permanent official signature of creator AR, signifying craftsmanship and uncompromised build quality."
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SymbolismItem(
                            icon = Icons.Default.Bolt,
                            title = "Cyber Neon Blue Aura",
                            description = "Represents ultra-fast multi-threaded network throughput, background concurrency, and modern engineering."
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SymbolismItem(
                            icon = Icons.Default.Download,
                            title = "Precision Downward Arrow",
                            description = "Highlights direct media capture, high-definition video saving, and lossless MP3/M4A audio extraction."
                        )
                    }
                }
            }

            // 3. Core App Capabilities
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = RoyalBlueDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E325C))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "BUILT-IN CAPABILITIES",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        FeatureRow(
                            icon = Icons.Default.Videocam,
                            title = "Ultra HD 1080p / 2K Downloader",
                            subtitle = "Multi-resolution video stream detection and download"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FeatureRow(
                            icon = Icons.Default.MusicNote,
                            title = "Lossless Audio Extractor",
                            subtitle = "Converts video streams to 320kbps MP3 and AAC audio"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FeatureRow(
                            icon = Icons.Default.Security,
                            title = "100% On-Device Privacy",
                            subtitle = "Zero server tracking, completely local database and storage"
                        )
                    }
                }
            }

            // 4. Developer & Legal Copyright Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = RoyalBlueSurface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B6B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "APPLICATION SPECIFICATIONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SpecRow(label = "Application", value = "AR All Video Downloader")
                        SpecRow(label = "Version", value = "1.0.0 (Official Build)")
                        SpecRow(label = "Developer / Creator", value = "AR")
                        SpecRow(label = "Architecture", value = "Kotlin • Jetpack Compose • Room")
                        SpecRow(label = "Theme", value = "Royal Blue & Gold Edition")

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "© 2026 AR. All rights reserved.\nThe uploaded logo is the permanent official brand identity of AR All Video Downloader.",
                            fontSize = 11.sp,
                            color = SilverHighlight.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SymbolismItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(RoyalBlueDeep)
                .border(1.dp, GoldMetallic.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldMetallic,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = SilverHighlight.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(RoyalBlueDeep)
                .border(1.dp, GoldDark, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SilverHighlight.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = SilverHighlight.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
