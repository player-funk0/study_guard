package com.obrynex.studyguard.islamic.domain.model

/** A hadith with its full chain of narration (isnad) and source reference */
data class Hadith(
    val id          : Int,
    val category    : HadithCategory,
    val arabicText  : String,
    val translation : String,        // الترجمة بالعربي الفصيح / التفسير
    val narrator    : String,        // الراوي مثل: عن أبي هريرة رضي الله عنه
    val isnad       : String,        // السند كاملاً
    val source      : String,        // المصدر مثل: صحيح البخاري
    val bookNumber  : String,        // رقم الكتاب والحديث
    val grade       : HadithGrade
)

enum class HadithCategory(val label: String, val emoji: String) {
    FADAIL_ILM  ("فضل العلم",       "📚"),
    AKHLAQ      ("الأخلاق",         "🌸"),
    IKHLAS      ("الإخلاص والنية",   "💎"),
    SALAH       ("الصلاة",           "🕌"),
    QURAN       ("فضل القرآن",       "📖"),
    DHIKR       ("الذكر والدعاء",    "🤲"),
    SIYAM       ("الصيام",           "🌙"),
    SADAQA      ("الصدقة",           "💛"),
    AMANAH      ("الأمانة",          "🛡️"),
    RAHMA       ("الرحمة",           "❤️")
}

enum class HadithGrade(val label: String, val color: Long) {
    SAHIH   ("صحيح",       0xFF00C9A7L),
    HASAN   ("حسن",        0xFFFFC107L),
    DAIF    ("ضعيف",       0xFFFF5C5CL)
}

/** ذكر من أذكار الصباح أو المساء أو مطلق */
data class Dhikr(
    val id          : Int,
    val category    : DhikrCategory,
    val arabicText  : String,
    val transliteration: String,
    val meaning     : String,
    val repetitions : Int,
    val source      : String,
    val virtue      : String         // الفضل والثواب
)

enum class DhikrCategory(val label: String, val emoji: String) {
    SABAH   ("أذكار الصباح",   "🌅"),
    MASAA   ("أذكار المساء",   "🌙"),
    SALAH   ("أذكار الصلاة",   "🕌"),
    MUTLAQ  ("أذكار مطلقة",    "✨"),
    NAWM    ("أذكار النوم",     "😴")
}

/** عداد التسبيح */
data class TasbihItem(
    val id      : Int,
    val text    : String,
    val target  : Int,
    val virtue  : String
)
