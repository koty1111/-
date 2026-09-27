package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayerManager
import com.example.model.SurahDetail
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldBorder
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark

@Composable
fun QuranReaderView(
    surahDetail: SurahDetail?,
    isLoading: Boolean,
    audioManager: AudioPlayerManager,
    onClose: () -> Unit,
    onPreviousSurah: () -> Unit,
    onNextSurah: () -> Unit
) {
    BackHandler {
        audioManager.stop()
        onClose()
    }

    val context = LocalContext.current
    val isPlaying by audioManager.isPlaying.collectAsState()
    val isAudioLoading by audioManager.isLoading.collectAsState()
    val currentTrack by audioManager.currentTrack.collectAsState()
    val currentTitle by audioManager.currentTitle.collectAsState()

    val isFullSurahPlaying = isPlaying && currentTrack == surahDetail?.fullAudioUrl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (isLoading || surahDetail == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = IslamicGold, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "جاري تحميل السورة الكريمة كاملة...",
                    color = TextMutedDark,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Top navigation and Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                audioManager.stop()
                                onClose()
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "HOLY QURAN",
                                color = IslamicGold,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = surahDetail.name,
                                fontFamily = FontFamily.Serif,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldLight
                            )
                            Text(
                                text = "${surahDetail.revelationArabic} • ${surahDetail.numberOfAyahs} آية • سورة رقم ${surahDetail.number}",
                                fontSize = 12.sp,
                                color = TextMutedDark
                            )
                        }

                        Spacer(modifier = Modifier.size(42.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sheikh Yasser Al-Dossari reciter banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(IslamicGold.copy(alpha = 0.1f))
                            .border(1.dp, IslamicGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = IslamicGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "القارئ: الشيخ ياسر الدوسري",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play / Pause full Surah by Yasser Al-Dossari
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFullSurahPlaying) IslamicGoldLight else IslamicGold.copy(alpha = 0.15f)
                                )
                                .border(1.dp, IslamicGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isFullSurahPlaying) {
                                        audioManager.stop()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "جاري تشغيل تلاوة الشيخ ياسر الدوسري...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        audioManager.playAudio(surahDetail.fullAudioUrl, surahDetail.name)
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isAudioLoading && currentTrack == surahDetail.fullAudioUrl) {
                                    CircularProgressIndicator(
                                        color = if (isFullSurahPlaying) Color.Black else IslamicGoldLight,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isFullSurahPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isFullSurahPlaying) Color.Black else IslamicGoldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFullSurahPlaying) "إيقاف التلاوة" else "تشغيل السورة كاملة",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFullSurahPlaying) Color.Black else IslamicGoldLight
                                )
                            }
                        }

                        // Copy full Surah
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val fullText = buildString {
                                        append(surahDetail.name).append("\n\n")
                                        surahDetail.ayahs.forEach {
                                            append(it.text).append(" ۝ ")
                                        }
                                    }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Surah", fullText))
                                    Toast.makeText(context, "تم نسخ السورة كاملة", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = TextMutedDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "نسخ السورة",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bismillah banner (except At-Tawbah)
                    if (surahDetail.number != 9 && surahDetail.number != 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceCard)
                                .border(1.dp, IslamicGoldBorder, RoundedCornerShape(14.dp))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontFamily = FontFamily.Serif,
                                fontSize = 20.sp,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Ayahs list
                itemsIndexed(surahDetail.ayahs) { index, ayah ->
                    val isAyahPlaying = isPlaying && currentTrack == ayah.audioUrl

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isAyahPlaying) IslamicGold.copy(alpha = 0.12f) else DarkSurfaceCard)
                            .border(
                                1.dp,
                                if (isAyahPlaying) IslamicGold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            // Ayah Number
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .border(1.dp, IslamicGold.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${ayah.numberInSurah}",
                                        fontSize = 12.sp,
                                        color = IslamicGoldLight
                                    )
                                }

                                if (isAyahPlaying) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = IslamicGoldLight,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "الشيخ ياسر الدوسري ♫",
                                            fontSize = 11.sp,
                                            color = IslamicGoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Ayah Arabic Text
                            Text(
                                text = ayah.text,
                                fontFamily = FontFamily.Serif,
                                fontSize = 23.sp,
                                lineHeight = 38.sp,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Actions
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Listen ayah
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isAyahPlaying) IslamicGoldLight else Color.White.copy(alpha = 0.05f)
                                        )
                                        .clickable {
                                            if (isAyahPlaying) {
                                                audioManager.stop()
                                            } else if (ayah.audioUrl.isNotEmpty()) {
                                                Toast.makeText(
                                                    context,
                                                    "تلاوة الآية ${ayah.numberInSurah} - الشيخ ياسر الدوسري",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                audioManager.playAudio(ayah.audioUrl, "آية ${ayah.numberInSurah}")
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isAyahPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = if (isAyahPlaying) Color.Black else IslamicGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAyahPlaying) "إيقاف" else "استماع",
                                            fontSize = 11.sp,
                                            fontWeight = if (isAyahPlaying) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isAyahPlaying) Color.Black else TextMutedDark
                                        )
                                    }
                                }

                                // Copy ayah
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Ayah", ayah.text))
                                            Toast.makeText(context, "تم نسخ الآية", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = IslamicGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "نسخ",
                                            fontSize = 11.sp,
                                            color = TextMutedDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Prev/Next navigation
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onPreviousSurah() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "← السورة السابقة",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(IslamicGold.copy(alpha = 0.1f))
                                .clickable {
                                    audioManager.stop()
                                    onClose()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "الفهرس",
                                color = IslamicGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onNextSurah() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "السورة التالية →",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
