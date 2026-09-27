package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import com.example.audio.AudioPlayerManager
import com.example.data.PreferencesManager
import com.example.data.PrayerRepository
import com.example.data.QuranRepository
import com.example.model.NextPrayerCountdown
import com.example.model.PrayerTimings
import com.example.model.Surah
import com.example.model.SurahDetail
import com.example.sensor.CompassSensorManager
import com.example.ui.HomeScreen
import com.example.ui.IntroScreen
import com.example.ui.QuranReaderView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val prayerRepository = PrayerRepository()
    private val quranRepository = QuranRepository()
    private val audioManager = AudioPlayerManager()
    private lateinit var compassSensorManager: CompassSensorManager
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = PreferencesManager(this)
        compassSensorManager = CompassSensorManager(this)

        setContent {
            var isDarkMode by remember { mutableStateOf(prefs.darkModeEnabled) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = DarkBackground
                    ) {
                        AppContent(
                            prayerRepository = prayerRepository,
                            quranRepository = quranRepository,
                            audioManager = audioManager,
                            compassSensorManager = compassSensorManager,
                            prefs = prefs,
                            isDarkMode = isDarkMode,
                            onToggleTheme = {
                                isDarkMode = !isDarkMode
                                prefs.darkModeEnabled = isDarkMode
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
        compassSensorManager.stopListening()
    }
}

@Composable
fun AppContent(
    prayerRepository: PrayerRepository,
    quranRepository: QuranRepository,
    audioManager: AudioPlayerManager,
    compassSensorManager: CompassSensorManager,
    prefs: PreferencesManager,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showIntro by remember { mutableStateOf(true) }

    // Start/stop compass sensor listening
    DisposableEffect(Unit) {
        compassSensorManager.startListening()
        onDispose {
            compassSensorManager.stopListening()
        }
    }

    val compassHeading by compassSensorManager.currentHeading.collectAsState()
    val isCompassSensorAvailable by compassSensorManager.isSensorAvailable.collectAsState()

    // Coordinates and city
    var currentLat by remember { mutableDoubleStateOf(prefs.lastLat.toDouble()) }
    var currentLon by remember { mutableDoubleStateOf(prefs.lastLon.toDouble()) }
    var cityName by remember { mutableStateOf(prefs.lastCity) }

    // Prayer times state
    var prayerTimings by remember { mutableStateOf(PrayerTimings()) }
    var nextPrayer by remember { mutableStateOf(NextPrayerCountdown()) }
    var qiblaBearing by remember { mutableDoubleStateOf(prayerRepository.calculateQiblaBearing(currentLat, currentLon)) }

    // Quran Surahs list
    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var isSurahsLoading by remember { mutableStateOf(true) }

    // Surah Reader state
    var selectedSurahNumber by remember { mutableStateOf<Int?>(null) }
    var currentSurahDetail by remember { mutableStateOf<SurahDetail?>(null) }
    var isSurahDetailLoading by remember { mutableStateOf(false) }

    // Hijri date string
    val hijriDateStr = remember {
        getFormattedHijriDate()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Request Location function
    fun requestDeviceLocation() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null &&
            (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
             ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        ) {
            val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (location != null) {
                currentLat = location.latitude
                currentLon = location.longitude
                cityName = "موقعك الحالي"
                prefs.lastCity = cityName
                prefs.lastLat = location.latitude.toFloat()
                prefs.lastLon = location.longitude.toFloat()
                qiblaBearing = prayerRepository.calculateQiblaBearing(currentLat, currentLon)
                scope.launch {
                    prayerTimings = prayerRepository.getPrayerTimes(currentLat, currentLon)
                }
                Toast.makeText(context, "تم تحديد موقعك للقبلة والمواقيت بنجاح 📍", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "جاري استخدام موقع القاهرة كمرجع", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestDeviceLocation()
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        prayerTimings = prayerRepository.getPrayerTimes(currentLat, currentLon)
        qiblaBearing = prayerRepository.calculateQiblaBearing(currentLat, currentLon)
        surahs = quranRepository.getSurahs()
        isSurahsLoading = false
    }

    // Refresh surahs function
    fun refreshSurahs() {
        scope.launch {
            isSurahsLoading = true
            surahs = quranRepository.getSurahs()
            isSurahsLoading = false
            Toast.makeText(context, "تم تحديث السور (114 سورة)", Toast.LENGTH_SHORT).show()
        }
    }

    // Open Surah in reader
    fun openSurah(number: Int) {
        selectedSurahNumber = number
        isSurahDetailLoading = true
        scope.launch {
            val result = quranRepository.getSurahDetail(number)
            currentSurahDetail = result.getOrNull()
            isSurahDetailLoading = false
        }
    }

    var lastPlayedAdhanKey by remember { mutableStateOf("") }

    // Countdown ticker loop (updates every second)
    LaunchedEffect(prayerTimings) {
        while (true) {
            val countdown = prayerRepository.calculateNextPrayer(prayerTimings)
            nextPrayer = countdown
            // Auto-adhan trigger ONLY when exact prayer time enters, and only once per prayer per day
            if (prefs.adhanSoundEnabled) {
                val due = prayerRepository.checkDueAdhan(prayerTimings, lastPlayedAdhanKey)
                if (due != null) {
                    val (key, prayerName) = due
                    lastPlayedAdhanKey = key
                    audioManager.playAdhan()
                    Toast.makeText(context, "حان الآن موعد أذان صلاة $prayerName", Toast.LENGTH_LONG).show()
                }
            }
            delay(1000)
        }
    }

    Crossfade(
        targetState = showIntro to (selectedSurahNumber != null),
        label = "screen_transition"
    ) { (isIntro, isReaderOpen) ->
        when {
            isIntro -> {
                IntroScreen(
                    onFinished = { showIntro = false }
                )
            }
            isReaderOpen -> {
                QuranReaderView(
                    surahDetail = currentSurahDetail,
                    isLoading = isSurahDetailLoading,
                    audioManager = audioManager,
                    onClose = {
                        selectedSurahNumber = null
                        currentSurahDetail = null
                    },
                    onPreviousSurah = {
                        val currentNum = selectedSurahNumber ?: 1
                        if (currentNum > 1) {
                            openSurah(currentNum - 1)
                        }
                    },
                    onNextSurah = {
                        val currentNum = selectedSurahNumber ?: 1
                        if (currentNum < 114) {
                            openSurah(currentNum + 1)
                        }
                    }
                )
            }
            else -> {
                HomeScreen(
                    prayerTimings = prayerTimings,
                    nextPrayer = nextPrayer,
                    qiblaBearing = qiblaBearing,
                    compassHeading = compassHeading,
                    isCompassSensorAvailable = isCompassSensorAvailable,
                    surahs = surahs,
                    isSurahsLoading = isSurahsLoading,
                    cityName = cityName,
                    hijriDateStr = hijriDateStr,
                    prefs = prefs,
                    audioManager = audioManager,
                    onOpenSurah = { openSurah(it) },
                    onRefreshSurahs = { refreshSurahs() },
                    onRequestLocation = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onToggleTheme = onToggleTheme,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

private fun getFormattedHijriDate(): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hijrahDate = HijrahDate.now()
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy هـ", Locale("ar"))
            hijrahDate.format(formatter)
        } else {
            "1447 هـ"
        }
    } catch (_: Exception) {
        "1447 هـ"
    }
}
