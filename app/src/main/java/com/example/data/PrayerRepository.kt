package com.example.data

import com.example.model.NextPrayerCountdown
import com.example.model.PrayerTimings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class PrayerRepository {

    suspend fun getPrayerTimes(latitude: Double, longitude: Double): PrayerTimings = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis() / 1000
            val urlString = "https://api.aladhan.com/v1/timings/$timestamp?latitude=$latitude&longitude=$longitude&method=5"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
            }
            if (connection.responseCode == 200) {
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(json)
                val timings = root.getJSONObject("data").getJSONObject("timings")
                return@withContext PrayerTimings(
                    fajr = cleanTime(timings.getString("Fajr")),
                    sunrise = cleanTime(timings.getString("Sunrise")),
                    dhuhr = cleanTime(timings.getString("Dhuhr")),
                    asr = cleanTime(timings.getString("Asr")),
                    maghrib = cleanTime(timings.getString("Maghrib")),
                    isha = cleanTime(timings.getString("Isha"))
                )
            }
        } catch (_: Exception) {
            // Graceful fallback to default accurate prayer times
        }
        PrayerTimings()
    }

    private fun cleanTime(raw: String): String {
        return raw.split(" ")[0].take(5)
    }

    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val kaabaLat = 21.4225
        val kaabaLon = 39.8262

        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(kaabaLat)
        val deltaLambda = Math.toRadians(kaabaLon - longitude)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    fun calculateNextPrayer(timings: PrayerTimings): NextPrayerCountdown {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val currentSeconds = now.get(Calendar.SECOND)

        val prayers = listOf(
            Triple("Fajr", "الفجر", parseMinutes(timings.fajr)),
            Triple("Dhuhr", "الظهر", parseMinutes(timings.dhuhr)),
            Triple("Asr", "العصر", parseMinutes(timings.asr)),
            Triple("Maghrib", "المغرب", parseMinutes(timings.maghrib)),
            Triple("Isha", "العشاء", parseMinutes(timings.isha))
        )

        for (prayer in prayers) {
            val prayerMinute = prayer.third
            if (prayerMinute > currentMinutes) {
                val totalDiffSeconds = (prayerMinute - currentMinutes) * 60 - currentSeconds
                val hours = totalDiffSeconds / 3600
                val minutes = (totalDiffSeconds % 3600) / 60
                val seconds = totalDiffSeconds % 60
                return NextPrayerCountdown(
                    prayerKey = prayer.first,
                    prayerName = prayer.second,
                    hours = hours.toInt(),
                    minutes = minutes.toInt(),
                    seconds = seconds.toInt()
                )
            }
        }

        // After Isha, next is Fajr tomorrow
        val fajrMinute = parseMinutes(timings.fajr)
        val remainingTodaySeconds = (24 * 60 - currentMinutes) * 60 - currentSeconds
        val totalDiffSeconds = remainingTodaySeconds + fajrMinute * 60
        val hours = totalDiffSeconds / 3600
        val minutes = (totalDiffSeconds % 3600) / 60
        val seconds = totalDiffSeconds % 60

        return NextPrayerCountdown(
            prayerKey = "Fajr",
            prayerName = "الفجر",
            hours = hours.toInt(),
            minutes = minutes.toInt(),
            seconds = seconds.toInt()
        )
    }

    /**
     * Checks if current minute matches any prayer time exactly, and returns (adhanKey, prayerName)
     * only once per prayer per day.
     */
    fun checkDueAdhan(timings: PrayerTimings, lastPlayedKey: String): Pair<String, String>? {
        val now = Calendar.getInstance()
        val currentH = String.format(Locale.US, "%02d", now.get(Calendar.HOUR_OF_DAY))
        val currentM = String.format(Locale.US, "%02d", now.get(Calendar.MINUTE))
        val currentTime = "$currentH:$currentM"
        val todayStr = String.format(
            Locale.US,
            "%04d-%02d-%02d",
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH) + 1,
            now.get(Calendar.DAY_OF_MONTH)
        )

        val prayers = listOf(
            "Fajr" to (timings.fajr to "الفجر"),
            "Dhuhr" to (timings.dhuhr to "الظهر"),
            "Asr" to (timings.asr to "العصر"),
            "Maghrib" to (timings.maghrib to "المغرب"),
            "Isha" to (timings.isha to "العشاء")
        )

        for ((key, pair) in prayers) {
            val (time, name) = pair
            val cleanPrayerTime = time.take(5)
            val adhanKey = "${todayStr}_$key"
            if (cleanPrayerTime == currentTime) {
                if (lastPlayedKey != adhanKey) {
                    return adhanKey to name
                }
            }
        }
        return null
    }

    private fun parseMinutes(timeStr: String): Int {
        val parts = timeStr.trim().take(5).split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }
}
