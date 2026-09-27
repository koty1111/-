package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackgroundDeep
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark

@Composable
fun IntroScreen(
    onFinished: () -> Unit
) {
    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition(label = "intro")

    // Ambient glow rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )

    // Star pulse
    val starGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star"
    )

    // Progress bar animation
    val progressAnim = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(1200))
        progressAnim.animateTo(1f, tween(3600))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackgroundDeep)
            .clickable { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        // Kaaba Background
        Image(
            painter = painterResource(id = com.example.R.drawable.bg_kaaba),
            contentDescription = "الكعبة المشرفة",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.28f)
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DarkBackgroundDeep.copy(alpha = 0.85f),
                            DarkBackgroundDeep.copy(alpha = 0.92f),
                            DarkBackgroundDeep
                        )
                    )
                )
        )

        // Ambient radial glows
        Box(
            modifier = Modifier
                .size(450.dp)
                .offset(x = (-80).dp, y = (-120).dp)
                .blur(80.dp)
                .background(IslamicGold.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = 100.dp, y = 160.dp)
                .blur(90.dp)
                .background(IslamicGold.copy(alpha = 0.06f), CircleShape)
        )

        // Geometric diamond decoration
        Box(
            modifier = Modifier
                .size(260.dp)
                .rotate(45f)
                .border(1.dp, IslamicGold.copy(alpha = 0.06f))
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Bismillah
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, IslamicGold.copy(alpha = 0.6f))
                            )
                        )
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "بِسْمِ اللهِ الرَّحْمَنِ الرَّحِيمِ",
                    fontFamily = FontFamily.Serif,
                    color = IslamicGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(14.dp))
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(IslamicGold.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Glowing Moon & Star container
            Box(
                modifier = Modifier
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating ring
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .rotate(ringRotation)
                        .border(1.dp, IslamicGold.copy(alpha = 0.15f), CircleShape)
                )

                // Crescent Moon (drawn with overlapping circles)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(-15f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(IslamicGold)
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .offset(x = 16.dp, y = (-8).dp)
                            .clip(CircleShape)
                            .background(DarkBackgroundDeep)
                    )
                }

                // Shining Star ✦
                Text(
                    text = "✦",
                    color = IslamicGoldLight,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 14.dp)
                        .scale(starGlow)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Grand Brand Title
            Text(
                text = "حي على الفلاح",
                fontFamily = FontFamily.Serif,
                color = TextPrimaryDark,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 52.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Golden sweeping divider line
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                IslamicGoldDark.copy(alpha = 0.4f),
                                IslamicGoldLight,
                                IslamicGoldDark.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // English transliteration
            Text(
                text = "HAYYA 'ALA AL-FALAH",
                color = IslamicGold.copy(alpha = 0.75f),
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Subtitle
            Text(
                text = "منصة الصلاة والعبادة",
                color = TextMutedDark,
                fontSize = 15.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Loading track and progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressAnim.value)
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(IslamicGoldDark, IslamicGoldLight, IslamicGold)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "جاري الدخول...",
                    color = TextMutedDark.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Skip button in top corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .clickable { onFinished() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "تخطي ←",
                color = IslamicGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
