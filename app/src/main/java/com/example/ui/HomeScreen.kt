package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.AudioPlayerManager
import com.example.data.AzkarData
import com.example.data.PreferencesManager
import com.example.model.AzkarCategory
import com.example.model.NextPrayerCountdown
import com.example.model.PrayerTimings
import com.example.model.Surah
import com.example.ui.theme.ActiveCardBorder
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBorder
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldDark
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightSurfaceCard
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    prayerTimings: PrayerTimings,
    nextPrayer: NextPrayerCountdown,
    qiblaBearing: Double,
    compassHeading: Float,
    isCompassSensorAvailable: Boolean,
    surahs: List<Surah>,
    isSurahsLoading: Boolean,
    cityName: String,
    hijriDateStr: String,
    prefs: PreferencesManager,
    audioManager: AudioPlayerManager,
    onOpenSurah: (Int) -> Unit,
    onRefreshSurahs: () -> Unit,
    onRequestLocation: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // State for modal
    var selectedAzkarCategory by remember { mutableStateOf<AzkarCategory?>(null) }

    // State for Tasbih
    var tasbihCount by remember { mutableIntStateOf(prefs.tasbihCount) }
    val tasbihPhrases = listOf(
        "سُبْحَانَ اللهِ",
        "الحَمْدُ للهِ",
        "اللهُ أَكْبَرُ",
        "لَا إِلَهَ إِلَّا اللهُ",
        "أَسْتَغْفِرُ اللهَ"
    )
    var selectedPhraseIndex by remember { mutableIntStateOf(0) }

    // State for Quran search
    var searchQuery by remember { mutableStateOf("") }
    val filteredSurahs = remember(surahs, searchQuery) {
        if (searchQuery.isBlank()) surahs
        else {
            val q = searchQuery.trim()
            surahs.filter {
                it.name.contains(q, ignoreCase = true) ||
                it.englishName.contains(q, ignoreCase = true) ||
                it.number.toString() == q
            }
        }
    }

    // Settings local toggles for immediate reaction
    var prayerAlerts by remember { mutableStateOf(prefs.prayerAlertsEnabled) }
    var visualEffects by remember { mutableStateOf(prefs.visualEffectsEnabled) }
    var backgroundAnim by remember { mutableStateOf(prefs.backgroundAnimationEnabled) }
    var adhanSound by remember { mutableStateOf(prefs.adhanSoundEnabled) }
    var saveTasbih by remember { mutableStateOf(prefs.saveTasbihEnabled) }

    // Compass calculation:
    // Relative angle for the needle pointing to Kaaba: (qiblaBearing - compassHeading)
    val targetNeedleAngle = if (isCompassSensorAvailable) {
        (qiblaBearing.toFloat() - compassHeading + 360f) % 360f
    } else {
        qiblaBearing.toFloat()
    }

    val animatedNeedleAngle by animateFloatAsState(
        targetValue = targetNeedleAngle,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "needle_rot"
    )

    val targetDialAngle = if (isCompassSensorAvailable) {
        (-compassHeading + 360f) % 360f
    } else {
        0f
    }

    val animatedDialAngle by animateFloatAsState(
        targetValue = targetDialAngle,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "dial_rot"
    )

    // Check if phone is aligned with Qibla (within ±8 degrees)
    val angleDiff = abs(targetNeedleAngle)
    val isFacingQibla = (angleDiff <= 8f || angleDiff >= 352f)

    // Background stars floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starsOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_float"
    )

    val listState = rememberLazyListState()

    // Haptic vibration helper
    fun vibrate(duration: Long = 35) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(duration)
            }
        } catch (_: Exception) {}
    }

    val isAudioPlaying by audioManager.isPlaying.collectAsState()

    val currentBgColor = if (isDarkMode) DarkBackground else LightBackground
    val currentCardBg = if (isDarkMode) DarkSurfaceCard else LightSurfaceCard
    val currentTextColor = if (isDarkMode) TextPrimaryDark else TextPrimaryLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor)
    ) {
        // ================= KAABA BACKGROUND IMAGE =================
        Image(
            painter = painterResource(id = R.drawable.bg_kaaba),
            contentDescription = "الكعبة المشرفة",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isDarkMode) 0.42f else 0.20f)
        )

        // Luxury atmospheric gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isDarkMode) Color(0xD0050706) else Color(0xD5F5F3ED),
                            if (isDarkMode) Color(0xE8050706) else Color(0xE8F5F3ED),
                            if (isDarkMode) Color(0xF2050706) else Color(0xF4F5F3ED)
                        )
                    )
                )
        )

        // Floating starry background (Canvas) if visual effects enabled
        if (visualEffects) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (backgroundAnim) 0.dp else 1.dp)
            ) {
                val random = Random(42)
                for (i in 0..60) {
                    val x = random.nextFloat() * size.width
                    val initialY = random.nextFloat() * size.height
                    val currentY = (initialY + (starsOffsetY * (i % 3 + 1))) % size.height
                    val radius = (random.nextFloat() * 1.5f + 1f).dp.toPx()
                    drawCircle(
                        color = Color(0xFFF4DC9B).copy(alpha = (random.nextFloat() * 0.4f + 0.2f)),
                        radius = radius,
                        center = Offset(x, if (currentY < 0) currentY + size.height else currentY)
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // ================= HEADER =================
            item {
                Spacer(modifier = Modifier.height(36.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo + Text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Gold boxed icon
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.radialGradient(
                                        listOf(IslamicGold.copy(alpha = 0.25f), IslamicGold.copy(alpha = 0.05f))
                                    )
                                )
                                .border(1.dp, IslamicGold.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "☪",
                                fontSize = 22.sp,
                                color = IslamicGoldLight
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "حي على الفلاح",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTextColor
                            )
                            Text(
                                text = "منصة الصلاة والعبادة",
                                fontSize = 10.sp,
                                color = IslamicGold,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Action buttons (Sound Toggle & Dark Mode)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                if (isAudioPlaying) {
                                    audioManager.stop()
                                    Toast.makeText(context, "تم إيقاف الصوت", Toast.LENGTH_SHORT).show()
                                } else {
                                    adhanSound = !adhanSound
                                    prefs.adhanSoundEnabled = adhanSound
                                    val msg = if (adhanSound) {
                                        "تم تفعيل صوت الأذان (سيؤذن فقط عند موعد الصلاة ⏱)"
                                    } else {
                                        "تم كتم صوت الأذان 🔕"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = if (adhanSound || isAudioPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "تفعيل أو كتم صوت الأذان",
                                tint = if (adhanSound || isAudioPlaying) IslamicGoldLight else TextMutedDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "المظهر",
                                tint = IslamicGoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // ================= CATEGORY TABS =================
            item {
                Spacer(modifier = Modifier.height(18.dp))
                val sections = listOf(
                    Triple("الصلاة", "🕌", 1),
                    Triple("القبلة", "🧭", 3),
                    Triple("الأذكار", "🤲", 4),
                    Triple("القرآن", "📖", 5),
                    Triple("المسبحة", "📿", 6),
                    Triple("الإعدادات", "⚙️", 7)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
                ) {
                    items(sections) { (title, icon, targetIndex) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        listState.animateScrollToItem(targetIndex)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ================= HERO / NEXT PRAYER =================
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Location Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(1.dp, IslamicGoldBorder, RoundedCornerShape(50.dp))
                            .clickable { onRequestLocation() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📍", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cityName,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = hijriDateStr,
                                fontSize = 11.sp,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Title
                    Text(
                        text = "حي على الفلاح",
                        fontFamily = FontFamily.Serif,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTextColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "واجهتك اليومية للصلاة والذكر والقرآن",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next Prayer Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(currentCardBg)
                            .border(1.dp, IslamicGoldBorder, RoundedCornerShape(26.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "الصلاة القادمة",
                                color = IslamicGold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = nextPrayer.prayerName,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = currentTextColor
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Live Countdown 3 boxes
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CountdownBox(value = nextPrayer.hours, label = "ساعة")
                                Text(text = ":", color = IslamicGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                CountdownBox(value = nextPrayer.minutes, label = "دقيقة")
                                Text(text = ":", color = IslamicGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                CountdownBox(value = nextPrayer.seconds, label = "ثانية")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val gregorianDate = remember {
                                val sdf = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
                                sdf.format(Date())
                            }
                            Text(
                                text = gregorianDate,
                                color = TextMutedDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // ================= PRAYER TIMES GRID =================
            item {
                Spacer(modifier = Modifier.height(40.dp))
                SectionHeader(
                    eyebrow = "PRAYER TIMES",
                    title = "مواقيت الصلاة",
                    subtitle = "مواقيت اليوم حسب موقعك الحالي"
                )

                Spacer(modifier = Modifier.height(16.dp))

                val prayerItems = prayerTimings.toList(nextPrayer.prayerKey)

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    maxItemsInEachRow = 2,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    prayerItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (item.isNext) IslamicGold.copy(alpha = 0.12f)
                                    else currentCardBg
                                )
                                .border(
                                    1.dp,
                                    if (item.isNext) ActiveCardBorder else CardBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(vertical = 18.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = item.icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    color = TextSecondaryDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.time,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTextColor
                                )
                                if (item.isNext) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(IslamicGoldLight)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "القادمة",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================= QUICK HISN AL-MUSLIM ACCESS =================
            item {
                Spacer(modifier = Modifier.height(30.dp))
                SectionHeader(
                    eyebrow = "HISN AL-MUSLIM",
                    title = "أذكار حصن المسلم — وصول سريع",
                    subtitle = "أكثر الأذكار اليومية طلباً بلمسة واحدة مباشرة"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val quickAzkarList = listOf(
                        Triple(AzkarData.morningAzkar, "أذكار الصباح", "🌅"),
                        Triple(AzkarData.eveningAzkar, "أذكار المساء", "🌙"),
                        Triple(AzkarData.afterPrayerAzkar, "بعد الصلاة", "🕌")
                    )

                    quickAzkarList.forEach { (cat, title, icon) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(currentCardBg)
                                .border(1.dp, IslamicGoldBorder.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .clickable { selectedAzkarCategory = cat }
                                .padding(vertical = 14.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = icon, fontSize = 26.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTextColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${cat.items.size} أذكار",
                                    fontSize = 10.sp,
                                    color = IslamicGoldLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // ================= QUICK SERVICES =================
            item {
                Spacer(modifier = Modifier.height(40.dp))
                SectionHeader(
                    eyebrow = "DAILY WORSHIP",
                    title = "كل ما تحتاجه في مكان واحد",
                    subtitle = "أدوات يومية تساعدك على المحافظة على عبادتك"
                )

                Spacer(modifier = Modifier.height(16.dp))

                val quickCards = listOf(
                    Triple("اتجاه القبلة", "بوصلة تفاعلية حية تتفاعل مع حركة الهاتف", "🧭") to 3,
                    Triple("الأذكار", "أذكار الصباح والمساء وبعد الصلاة", "🤲") to 4,
                    Triple("القرآن الكريم", "114 سورة بصوت الشيخ ياسر الدوسري", "📖") to 5,
                    Triple("المسبحة", "عداد إلكتروني للذكر والتسبيح", "📿") to 6
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    maxItemsInEachRow = 2,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quickCards.forEach { (card, targetIdx) ->
                        val (title, desc, icon) = card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(currentCardBg)
                                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    scope.launch { listState.animateScrollToItem(targetIdx) }
                                }
                                .padding(18.dp)
                        ) {
                            Column {
                                Text(text = icon, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTextColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = TextMutedDark,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // ================= LIVE ROTATING QIBLA COMPASS =================
            item {
                Spacer(modifier = Modifier.height(45.dp))
                SectionHeader(
                    eyebrow = "LIVE COMPASS",
                    title = "اتجاه القبلة التفاعلي",
                    subtitle = "وجّه هاتفك نحو الكعبة — الإبرة تدور وتتحرك مباشرة مع حركة هاتفك"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(currentCardBg)
                        .border(
                            2.dp,
                            if (isFacingQibla) IslamicGoldLight else IslamicGoldBorder,
                            RoundedCornerShape(26.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Live alignment banner if facing Kaaba
                        if (isFacingQibla) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(IslamicGoldLight)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "أنت باتجاه القبلة الآن مباشرة 🕋✨",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Compass dial
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .border(
                                    2.dp,
                                    if (isFacingQibla) IslamicGoldLight else IslamicGold.copy(alpha = 0.5f),
                                    CircleShape
                                )
                                .background(
                                    Brush.radialGradient(
                                        listOf(IslamicGold.copy(alpha = 0.12f), Color.Black.copy(alpha = 0.65f))
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Dial markings that rotate with compassHeading
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(animatedDialAngle)
                            ) {
                                Text(
                                    text = "N",
                                    color = IslamicGoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 8.dp)
                                )
                                Text(
                                    text = "S",
                                    color = TextMutedDark,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "E",
                                    color = TextMutedDark,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 10.dp)
                                )
                                Text(
                                    text = "W",
                                    color = TextMutedDark,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 10.dp)
                                )

                                // Inner concentric circle
                                Box(
                                    modifier = Modifier
                                        .size(175.dp)
                                        .align(Alignment.Center)
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                )
                            }

                            // Dynamic rotating needle pointing directly towards Kaaba
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .rotate(animatedNeedleAngle),
                                contentAlignment = Alignment.Center
                            ) {
                                // Upper half pointing to Kaaba
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🕋",
                                        fontSize = 26.sp,
                                        modifier = Modifier.offset(y = 2.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(72.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(IslamicGoldLight, IslamicGoldDark, Color.Transparent)
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                // Center pivot
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(IslamicGoldLight)
                                        .border(2.dp, Color.Black, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Readings: Qibla bearing & live device heading
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "زاوية القبلة",
                                    fontSize = 11.sp,
                                    color = TextMutedDark
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", qiblaBearing)}°",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "اتجاه هاتفك الآن",
                                    fontSize = 11.sp,
                                    color = TextMutedDark
                                )
                                Text(
                                    text = "${compassHeading.toInt()}°",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFacingQibla) IslamicGoldLight else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sensor status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = if (isCompassSensorAvailable) IslamicGoldLight else TextMutedDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCompassSensorAvailable) {
                                    "البوصلة متصلة وتتحرك تلقائيًا مع التفاف هاتفك"
                                } else {
                                    "مقياس الاتجاه محسوب بدقة على موقعك الجغرافي"
                                },
                                color = TextMutedDark,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gold Action Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(IslamicGoldDark, IslamicGoldLight, IslamicGold)
                                    )
                                )
                                .clickable { onRequestLocation() }
                                .padding(horizontal = 22.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تحديث موقعي للقبلة",
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ================= AZKAR SECTION =================
            item {
                Spacer(modifier = Modifier.height(45.dp))
                SectionHeader(
                    eyebrow = "HISN AL-MUSLIM",
                    title = "أذكار حصن المسلم كاملة",
                    subtitle = "أذكار اليوم والليلة مقسمة ومبوبة مع عدادات تفاعلية وفضائل كل ذكر"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AzkarData.allCategories.forEach { category ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(currentCardBg)
                                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedAzkarCategory = category }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Icon
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(IslamicGold.copy(alpha = 0.12f))
                                        .border(1.dp, IslamicGold.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = category.icon, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Category Titles & Badges
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = category.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentTextColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(IslamicGold.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${category.items.size} أذكار",
                                                fontSize = 10.sp,
                                                color = IslamicGoldLight,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = category.subtitle,
                                        fontSize = 12.sp,
                                        color = TextMutedDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(IslamicGoldDark, IslamicGoldLight)
                                            )
                                        )
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "فتح الأذكار",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= HOLY QURAN SECTION (ALL 114 SURAHS + YASSER AL-DOSSARI) =================
            item {
                Spacer(modifier = Modifier.height(45.dp))
                SectionHeader(
                    eyebrow = "HOLY QURAN",
                    title = "القرآن الكريم كاملًا (114 سورة)",
                    subtitle = "تلاوة خاشعة بصوت الشيخ ياسر الدوسري — اقرأ واستمع لكل سورة وآية"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(currentCardBg)
                        .border(1.dp, IslamicGoldBorder, RoundedCornerShape(26.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        // Reciter banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(IslamicGold.copy(alpha = 0.12f))
                                .border(1.dp, IslamicGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(vertical = 10.dp, horizontal = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = IslamicGoldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تلاوة: الشيخ ياسر الدوسري",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IslamicGoldLight
                                    )
                                }

                                Text(
                                    text = "114 سورة كاملة",
                                    fontSize = 11.sp,
                                    color = TextMutedDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search bar + Count + Refresh
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text("ابحث عن اسم السورة أو رقمها...", color = TextMutedDark, fontSize = 12.sp)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = IslamicGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IslamicGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Surahs counter badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IslamicGold.copy(alpha = 0.1f))
                                    .border(1.dp, IslamicGold.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 13.dp)
                            ) {
                                Text(
                                    text = "${filteredSurahs.size} سورة",
                                    fontSize = 11.sp,
                                    color = IslamicGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Refresh
                            IconButton(
                                onClick = onRefreshSurahs,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث",
                                    tint = IslamicGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "اضغط على أي سورة من الـ 114 سورة للاستماع لتلاوة الشيخ ياسر الدوسري وقراءة جميع الآيات كاملة.",
                            fontSize = 11.sp,
                            color = TextMutedDark
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isSurahsLoading && surahs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = IslamicGold, modifier = Modifier.size(28.dp))
                            }
                        } else {
                            // Show ALL filtered Surahs without any limitation!
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                maxItemsInEachRow = 2,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredSurahs.forEach { surah ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.035f))
                                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                                            .clickable { onOpenSurah(surah.number) }
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${surah.number}",
                                                    fontSize = 10.sp,
                                                    color = IslamicGold
                                                )
                                                Text(
                                                    text = surah.revelationArabic,
                                                    fontSize = 9.sp,
                                                    color = TextMutedDark
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = surah.name,
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = currentTextColor
                                            )

                                            Text(
                                                text = "${surah.numberOfAyahs} آية",
                                                fontSize = 10.sp,
                                                color = TextMutedDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================= DIGITAL TASBIH =================
            item {
                Spacer(modifier = Modifier.height(45.dp))
                SectionHeader(
                    eyebrow = "DIGITAL TASBIH",
                    title = "المسبحة الإلكترونية",
                    subtitle = "اضغط للذكر والثناء"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(currentCardBg)
                        .border(1.dp, IslamicGoldBorder, RoundedCornerShape(26.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Big Number Counter
                        Text(
                            text = "$tasbihCount",
                            fontSize = 76.sp,
                            fontWeight = FontWeight.Black,
                            color = IslamicGoldLight,
                            lineHeight = 80.sp
                        )

                        // Dhikr Selector
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(tasbihPhrases.size) { idx ->
                                val phrase = tasbihPhrases[idx]
                                val isSelected = idx == selectedPhraseIndex
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) IslamicGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                        .border(
                                            1.dp,
                                            if (isSelected) IslamicGold else Color.White.copy(alpha = 0.08f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedPhraseIndex = idx }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = phrase,
                                        fontSize = 11.sp,
                                        color = if (isSelected) IslamicGoldLight else TextMutedDark,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Text(
                            text = tasbihPhrases[selectedPhraseIndex],
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            color = currentTextColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        // Giant circular "سَبِّح" button
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val buttonScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.92f else 1.0f,
                            label = "button_scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(buttonScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.15f),
                                            IslamicGold.copy(alpha = 0.12f),
                                            Color.Black.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                                .border(2.dp, IslamicGold.copy(alpha = 0.7f), CircleShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    tasbihCount++
                                    if (saveTasbih) prefs.tasbihCount = tasbihCount
                                    vibrate(25)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "سَبِّح",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldLight
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Reset button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .clickable {
                                    tasbihCount = 0
                                    prefs.tasbihCount = 0
                                    vibrate(40)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "إعادة العداد",
                                color = TextMutedDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ================= SETTINGS SECTION =================
            item {
                Spacer(modifier = Modifier.height(45.dp))
                SectionHeader(
                    eyebrow = "SETTINGS",
                    title = "الإعدادات",
                    subtitle = "خصص تجربة حي على الفلاح لتناسبك"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingRow(
                        title = "🔔 تنبيهات الصلاة",
                        checked = prayerAlerts,
                        onCheckedChange = {
                            prayerAlerts = it
                            prefs.prayerAlertsEnabled = it
                        }
                    )
                    SettingRow(
                        title = "✨ المؤثرات البصرية",
                        checked = visualEffects,
                        onCheckedChange = {
                            visualEffects = it
                            prefs.visualEffectsEnabled = it
                        }
                    )
                    SettingRow(
                        title = "🌌 حركة الخلفية",
                        checked = backgroundAnim,
                        onCheckedChange = {
                            backgroundAnim = it
                            prefs.backgroundAnimationEnabled = it
                        }
                    )
                    SettingRow(
                        title = "🌙 الوضع الليلي",
                        checked = isDarkMode,
                        onCheckedChange = { onToggleTheme() }
                    )
                    SettingRow(
                        title = "📿 حفظ عداد المسبحة",
                        checked = saveTasbih,
                        onCheckedChange = {
                            saveTasbih = it
                            prefs.saveTasbihEnabled = it
                        }
                    )
                    SettingRow(
                        title = "🔊 صوت الأذان (عند موعد الصلاة فقط)",
                        checked = adhanSound,
                        onCheckedChange = {
                            adhanSound = it
                            prefs.adhanSoundEnabled = it
                        }
                    )
                }
            }

            // ================= FOOTER =================
            item {
                Spacer(modifier = Modifier.height(50.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(vertical = 36.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "حي على الفلاح",
                        fontFamily = FontFamily.Serif,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldLight
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "اللهم اجعلنا من أهل الصلاة والذكر والقرآن",
                        fontSize = 12.sp,
                        color = TextMutedDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "© 2026 حي على الفلاح — جميع الحقوق محفوظة",
                        fontSize = 10.sp,
                        color = TextMutedDark.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Azkar Dialog Modal
    selectedAzkarCategory?.let { category ->
        AzkarDialog(
            category = category,
            onDismiss = { selectedAzkarCategory = null }
        )
    }
}

@Composable
fun CountdownBox(value: Int, label: String) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "%02d", value),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextMutedDark
            )
        }
    }
}

@Composable
fun SectionHeader(eyebrow: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = eyebrow,
            color = IslamicGold,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = TextMutedDark,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Medium
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = IslamicGoldLight,
                    checkedTrackColor = IslamicGold.copy(alpha = 0.4f),
                    uncheckedThumbColor = Color(0xFFAAAAAA),
                    uncheckedTrackColor = Color(0xFF242824)
                )
            )
        }
    }
}
