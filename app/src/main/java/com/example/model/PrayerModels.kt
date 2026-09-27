package com.example.model

data class PrayerTimings(
    val fajr: String = "05:00",
    val sunrise: String = "06:25",
    val dhuhr: String = "12:55",
    val asr: String = "16:25",
    val maghrib: String = "19:00",
    val isha: String = "20:25"
) {
    fun toList(nextPrayerKey: String): List<PrayerItem> {
        return listOf(
            PrayerItem("Fajr", "الفجر", "🌙", fajr, nextPrayerKey == "Fajr"),
            PrayerItem("Sunrise", "الشروق", "🌅", sunrise, false),
            PrayerItem("Dhuhr", "الظهر", "☀️", dhuhr, nextPrayerKey == "Dhuhr"),
            PrayerItem("Asr", "العصر", "🌤️", asr, nextPrayerKey == "Asr"),
            PrayerItem("Maghrib", "المغرب", "🌇", maghrib, nextPrayerKey == "Maghrib"),
            PrayerItem("Isha", "العشاء", "🌙", isha, nextPrayerKey == "Isha")
        )
    }
}

data class PrayerItem(
    val key: String,
    val name: String,
    val icon: String,
    val time: String,
    val isNext: Boolean
)

data class NextPrayerCountdown(
    val prayerKey: String = "Maghrib",
    val prayerName: String = "المغرب",
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0
)
