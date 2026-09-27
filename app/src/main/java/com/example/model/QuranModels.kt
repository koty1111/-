package com.example.model

import java.util.Locale

data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
) {
    val revelationArabic: String
        get() = if (revelationType.equals("Meccan", ignoreCase = true)) "مكية" else "مدنية"
}

data class Ayah(
    val number: Int,
    val numberInSurah: Int,
    val text: String,
    val audioUrl: String = ""
)

data class SurahDetail(
    val number: Int,
    val name: String,
    val englishName: String,
    val numberOfAyahs: Int,
    val revelationType: String,
    val ayahs: List<Ayah>,
    val reciterName: String = "الشيخ ياسر الدوسري"
) {
    val revelationArabic: String
        get() = if (revelationType.equals("Meccan", ignoreCase = true)) "مكية" else "مدنية"

    val fullAudioUrl: String
        get() = String.format(Locale.US, "https://server11.mp3quran.net/yasser/%03d.mp3", number)
}
