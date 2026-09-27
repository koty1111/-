package com.example.data

import com.example.model.Ayah
import com.example.model.Surah
import com.example.model.SurahDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class QuranRepository {

    private val surahCache = ConcurrentHashMap<Int, SurahDetail>()

    // Built-in complete list of all 114 Surahs
    val defaultSurahs: List<Surah> by lazy {
        generateDefaultSurahList()
    }

    suspend fun getSurahs(): List<Surah> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.alquran.cloud/v1/surah")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
            }
            if (connection.responseCode == 200) {
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(json)
                val data = root.getJSONArray("data")
                val list = mutableListOf<Surah>()
                for (i in 0 until data.length()) {
                    val obj = data.getJSONObject(i)
                    list.add(
                        Surah(
                            number = obj.getInt("number"),
                            name = obj.getString("name"),
                            englishName = obj.getString("englishName"),
                            englishNameTranslation = obj.optString("englishNameTranslation", ""),
                            numberOfAyahs = obj.getInt("numberOfAyahs"),
                            revelationType = obj.getString("revelationType")
                        )
                    )
                }
                if (list.size == 114) return@withContext list
            }
        } catch (_: Exception) {}
        defaultSurahs
    }

    suspend fun getSurahDetail(number: Int): Result<SurahDetail> = withContext(Dispatchers.IO) {
        // Check cache first
        surahCache[number]?.let { return@withContext Result.success(it) }

        val surahInfo = defaultSurahs.find { it.number == number }

        // Primary source: High-speed CDN with full Uthmani script for all 114 Surahs
        try {
            val cdnUrl = URL("https://cdn.jsdelivr.net/gh/fawazahmed0/quran-api@1/editions/ara-quranuthmanihaf/$number.json")
            val connection = (cdnUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 6000
                requestMethod = "GET"
            }
            if (connection.responseCode == 200) {
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(json)
                val chapterArray = root.getJSONArray("chapter")
                val ayahs = mutableListOf<Ayah>()
                for (i in 0 until chapterArray.length()) {
                    val item = chapterArray.getJSONObject(i)
                    val verseNum = item.getInt("verse")
                    var verseText = item.getString("text").trim().removePrefix("\uFEFF")
                    val audioUrl = String.format(
                        Locale.US,
                        "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/%03d%03d.mp3",
                        number,
                        verseNum
                    )
                    ayahs.add(
                        Ayah(
                            number = verseNum,
                            numberInSurah = verseNum,
                            text = verseText,
                            audioUrl = audioUrl
                        )
                    )
                }
                val detail = SurahDetail(
                    number = number,
                    name = surahInfo?.name ?: "سورة رقم $number",
                    englishName = surahInfo?.englishName ?: "Surah $number",
                    numberOfAyahs = ayahs.size,
                    revelationType = surahInfo?.revelationType ?: "Meccan",
                    ayahs = ayahs,
                    reciterName = "الشيخ ياسر الدوسري"
                )
                surahCache[number] = detail
                return@withContext Result.success(detail)
            }
        } catch (_: Exception) {}

        // Secondary source: api.alquran.cloud
        try {
            val url = URL("https://api.alquran.cloud/v1/surah/$number/quran-uthmani")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 7000
                requestMethod = "GET"
            }
            if (connection.responseCode == 200) {
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(json)
                val data = root.getJSONObject("data")
                val ayahsArray = data.getJSONArray("ayahs")
                val ayahs = mutableListOf<Ayah>()
                for (i in 0 until ayahsArray.length()) {
                    val a = ayahsArray.getJSONObject(i)
                    val verseNum = a.getInt("numberInSurah")
                    var verseText = a.getString("text").trim().removePrefix("\uFEFF")
                    val audioUrl = String.format(
                        Locale.US,
                        "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/%03d%03d.mp3",
                        number,
                        verseNum
                    )
                    ayahs.add(
                        Ayah(
                            number = a.getInt("number"),
                            numberInSurah = verseNum,
                            text = verseText,
                            audioUrl = audioUrl
                        )
                    )
                }
                val detail = SurahDetail(
                    number = data.getInt("number"),
                    name = data.getString("name"),
                    englishName = data.getString("englishName"),
                    numberOfAyahs = data.getInt("numberOfAyahs"),
                    revelationType = data.getString("revelationType"),
                    ayahs = ayahs,
                    reciterName = "الشيخ ياسر الدوسري"
                )
                surahCache[number] = detail
                return@withContext Result.success(detail)
            }
        } catch (_: Exception) {}

        // Fallback for offline usage
        val fallback = getOfflineSurahFallback(number)
        if (fallback != null) {
            surahCache[number] = fallback
            return@withContext Result.success(fallback)
        }

        Result.failure(Exception("تعذر تحميل السورة. يرجى التحقق من الاتصال بالإنترنت والمحاولة مجددًا."))
    }

    private fun getOfflineSurahFallback(number: Int): SurahDetail? {
        val surahInfo = defaultSurahs.find { it.number == number } ?: return null
        return when (number) {
            1 -> SurahDetail(
                number = 1,
                name = "سُورَةُ ٱلْفَاتِحَةِ",
                englishName = "Al-Faatiha",
                numberOfAyahs = 7,
                revelationType = "Meccan",
                ayahs = listOf(
                    Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/001001.mp3"),
                    Ayah(2, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/001002.mp3"),
                    Ayah(3, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/001003.mp3"),
                    Ayah(4, 4, "مَالِكِ يَوْمِ الدِّينِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/001004.mp3"),
                    Ayah(5, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/001005.mp3"),
                    Ayah(6, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/001006.mp3"),
                    Ayah(7, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/001007.mp3")
                )
            )
            108 -> SurahDetail(
                number = 108,
                name = "سُورَةُ الْكَوْثَرِ",
                englishName = "Al-Kawthar",
                numberOfAyahs = 3,
                revelationType = "Meccan",
                ayahs = listOf(
                    Ayah(1, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/108001.mp3"),
                    Ayah(2, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/108002.mp3"),
                    Ayah(3, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/108003.mp3")
                )
            )
            112 -> SurahDetail(
                number = 112,
                name = "سُورَةُ الإِخْلَاصِ",
                englishName = "Al-Ikhlaas",
                numberOfAyahs = 4,
                revelationType = "Meccan",
                ayahs = listOf(
                    Ayah(1, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/112001.mp3"),
                    Ayah(2, 2, "اللَّهُ الصَّمَدُ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/112002.mp3"),
                    Ayah(3, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/112003.mp3"),
                    Ayah(4, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/112004.mp3")
                )
            )
            113 -> SurahDetail(
                number = 113,
                name = "سُورَةُ الفَلَقِ",
                englishName = "Al-Falaq",
                numberOfAyahs = 5,
                revelationType = "Meccan",
                ayahs = listOf(
                    Ayah(1, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/113001.mp3"),
                    Ayah(2, 2, "مِن شَرِّ مَا خَلَقَ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/113002.mp3"),
                    Ayah(3, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/113003.mp3"),
                    Ayah(4, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/113004.mp3"),
                    Ayah(5, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/113005.mp3")
                )
            )
            114 -> SurahDetail(
                number = 114,
                name = "سُورَةُ النَّاسِ",
                englishName = "An-Naas",
                numberOfAyahs = 6,
                revelationType = "Meccan",
                ayahs = listOf(
                    Ayah(1, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/114001.mp3"),
                    Ayah(2, 2, "مَلِكِ النَّاسِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/114002.mp3"),
                    Ayah(3, 3, "إِلَٰهِ النَّاسِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/114003.mp3"),
                    Ayah(4, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/114004.mp3"),
                    Ayah(5, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/114005.mp3"),
                    Ayah(6, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/114006.mp3")
                )
            )
            else -> null
        }
    }

    private fun generateDefaultSurahList(): List<Surah> {
        val names = listOf(
            Triple(1, "الفاتحة", 7), Triple(2, "البقرة", 286), Triple(3, "آل عمران", 200),
            Triple(4, "النساء", 176), Triple(5, "المائدة", 120), Triple(6, "الأنعام", 165),
            Triple(7, "الأعراف", 206), Triple(8, "الأنفال", 75), Triple(9, "التوبة", 129),
            Triple(10, "يونس", 109), Triple(11, "هود", 123), Triple(12, "يوسف", 111),
            Triple(13, "الرعد", 43), Triple(14, "إبراهيم", 52), Triple(15, "الحجر", 99),
            Triple(16, "النحل", 128), Triple(17, "الإسراء", 111), Triple(18, "الكهف", 110),
            Triple(19, "مريم", 98), Triple(20, "طه", 135), Triple(21, "الأنبياء", 112),
            Triple(22, "الحج", 78), Triple(23, "المؤمنون", 118), Triple(24, "النور", 64),
            Triple(25, "الفرقان", 77), Triple(26, "الشعراء", 227), Triple(27, "النمل", 93),
            Triple(28, "القصص", 88), Triple(29, "العنكبوت", 69), Triple(30, "الروم", 60),
            Triple(31, "لقمان", 34), Triple(32, "السجدة", 30), Triple(33, "الأحزاب", 73),
            Triple(34, "سبأ", 54), Triple(35, "فاطر", 45), Triple(36, "يس", 83),
            Triple(37, "الصافات", 182), Triple(38, "ص", 88), Triple(39, "الزمر", 75),
            Triple(40, "غافر", 85), Triple(41, "فصلت", 54), Triple(42, "الشورى", 53),
            Triple(43, "الزخرف", 89), Triple(44, "الدخان", 59), Triple(45, "الجاثية", 37),
            Triple(46, "الأحقاف", 35), Triple(47, "محمد", 38), Triple(48, "الفتح", 29),
            Triple(49, "الحجرات", 18), Triple(50, "ق", 45), Triple(51, "الذاريات", 60),
            Triple(52, "الطور", 49), Triple(53, "النجم", 62), Triple(54, "القمر", 55),
            Triple(55, "الرحمن", 78), Triple(56, "الواقعة", 96), Triple(57, "الحديد", 29),
            Triple(58, "المجادلة", 22), Triple(59, "الحشر", 24), Triple(60, "الممتحنة", 13),
            Triple(61, "الصف", 14), Triple(62, "الجمعة", 11), Triple(63, "المنافقون", 11),
            Triple(64, "التغابن", 18), Triple(65, "الطلاق", 12), Triple(66, "التحريم", 12),
            Triple(67, "الملك", 30), Triple(68, "القلم", 52), Triple(69, "الحاقة", 52),
            Triple(70, "المعارج", 44), Triple(71, "نوح", 28), Triple(72, "الجن", 28),
            Triple(73, "المزمل", 20), Triple(74, "المدثر", 56), Triple(75, "القيامة", 40),
            Triple(76, "الإنسان", 31), Triple(77, "المرسلات", 50), Triple(78, "النبأ", 40),
            Triple(79, "النازعات", 46), Triple(80, "عبس", 42), Triple(81, "التكوير", 29),
            Triple(82, "الانفطار", 19), Triple(83, "المطففين", 36), Triple(84, "الانشقاق", 25),
            Triple(85, "البروج", 22), Triple(86, "الطارق", 17), Triple(87, "الأعلى", 19),
            Triple(88, "الغاشية", 26), Triple(89, "الفجر", 30), Triple(90, "البلد", 20),
            Triple(91, "الشمس", 15), Triple(92, "الليل", 21), Triple(93, "الضحى", 11),
            Triple(94, "الشرح", 8), Triple(95, "التين", 8), Triple(96, "العلق", 19),
            Triple(97, "القدر", 5), Triple(98, "البينة", 8), Triple(99, "الزلزلة", 8),
            Triple(100, "العاديات", 11), Triple(101, "القارعة", 11), Triple(102, "التكاثر", 8),
            Triple(103, "العصر", 3), Triple(104, "الهمزة", 9), Triple(105, "الفيل", 5),
            Triple(106, "قريش", 4), Triple(107, "الماعون", 7), Triple(108, "الكوثر", 3),
            Triple(109, "الكافرون", 6), Triple(110, "النصر", 3), Triple(111, "المسد", 5),
            Triple(112, "الإخلاص", 4), Triple(113, "الفلق", 5), Triple(114, "الناس", 6)
        )

        val medinanNumbers = setOf(2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 98, 110)

        return names.map { (num, name, ayahs) ->
            val isMedinan = medinanNumbers.contains(num)
            Surah(
                number = num,
                name = "سُورَةُ $name",
                englishName = getSurahEnglishName(num),
                englishNameTranslation = "",
                numberOfAyahs = ayahs,
                revelationType = if (isMedinan) "Medinan" else "Meccan"
            )
        }
    }

    private fun getSurahEnglishName(num: Int): String {
        val english = listOf(
            "Al-Fatiha", "Al-Baqarah", "Aal-i-Imran", "An-Nisa", "Al-Ma'idah", "Al-An'am",
            "Al-A'raf", "Al-Anfal", "At-Tawbah", "Yunus", "Hud", "Yusuf", "Ar-Ra'd",
            "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra", "Al-Kahf", "Maryam", "Ta-Ha",
            "Al-Anbiya", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara",
            "An-Naml", "Al-Qasas", "Al-Ankabut", "Ar-Rum", "Luqman", "As-Sajdah",
            "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar",
            "Ghafir", "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah",
            "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf", "Adh-Dhariyat",
            "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah", "Al-Hadid",
            "Al-Mujadila", "Al-Hashr", "Al-Mumtahanah", "As-Saff", "Al-Jumu'ah",
            "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk",
            "Al-Qalam", "Al-Haqqah", "Al-Ma'arij", "Nuh", "Al-Jinn", "Al-Muzzammil",
            "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba",
            "An-Nazi'at", "Abasa", "At-Takwir", "Al-Infitar", "Al-Mutaffifin",
            "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A'la", "Al-Ghashiyah",
            "Al-Fajr", "Al-Balad", "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh",
            "At-Tin", "Al-Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-Adiyat",
            "Al-Qari'ah", "At-Takathur", "Al-Asr", "Al-Humazah", "Al-Fil", "Quraysh",
            "Al-Ma'un", "Al-Kawthar", "Al-Kafirun", "An-Nasr", "Al-Masad", "Al-Ikhlas",
            "Al-Falaq", "An-Nas"
        )
        return if (num in 1..english.size) english[num - 1] else "Surah $num"
    }
}
