package com.example.data.remote

import androidx.core.text.HtmlCompat
import com.example.data.model.NewsCategory
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object PaoClassifier {
    private val elLocale = Locale.forLanguageTag("el-GR")
    private val diacriticsRegex = Regex("\\p{InCombiningDiacriticalMarks}+")
    private val whitespaceRegex = Regex("\\s+")
    private val imgTagRegex = Regex(
        """<img[^>]+src\s*=\s*["']([^"']+)["']""",
        RegexOption.IGNORE_CASE
    )

    private val paoKeywords = listOf(
        "παναθηναικ",
        "panathinaik",
        "τριφυλλ",
        "οακα",
        "oaka",
        "λεωφορ",
        "βοτανικ",
        "αταμαν",
        "ataman",
        "σλουκ",
        "sloukas",
        "ιωαννιδ",
        "μπακασετ",
        "αλαφουζ",
        "γιαννακοπουλ",
        "βιτορια",
        "νουν",
        "nunn",
        "λεσορ",
        "lessort",
        "παο ",
        " παο",
        "pao "
    )

    private val transferKeywords = listOf(
        "μεταγραφ",
        "δανεισμ",
        "υπογραφ",
        "συμβολαι",
        "ανανεωσ",
        "προτασ",
        "αποκτ",
        "εκλεισ",
        "κλειν",
        "στοχοσ",
        "ελευθερ",
        "πρωτοσελιδ",
        "παρεμειν",
        "αποχωρ",
        "transfer",
        "deal"
    )

    private val basketballKeywords = listOf(
        "μπασκετ",
        "euroleague",
        "ευρωλιγκ",
        "αταμαν",
        "οακα",
        "σλουκ",
        "ναν",
        "nunn",
        "λεσορ",
        "χουαντσο",
        "ερνανγκομεζ",
        "οσμαν",
        "γιουρτσεβεν",
        "γραντ",
        "grant",
        "μητογλου",
        "gbl",
        "καλαθ",
        "τριποντ",
        "ριμπαουντ",
        "φαιναλ φορ",
        "final four",
        "aktor"
    )

    private val amateurKeywords = listOf(
        "ερασιτεχν",
        "βολεϊ",
        "βολει",
        "πολο",
        "χαντμπολ",
        "στιβ",
        "πυγμαχ",
        "ξιφασκ",
        "κολυμβ",
        "γυναικ",
        "πινγκ πονγκ",
        "παναθηναικος αο",
        "α.ο.",
        "αο "
    )

    private val footballKeywords = listOf(
        "ποδοσφαιρ",
        "super league",
        "σουπερ λιγκ",
        "λεωφορ",
        "βοτανικ",
        "βιτορια",
        "ιωαννιδ",
        "μπακασετ",
        "τετε",
        "πελιστρι",
        "ινγκασον",
        "μαξιμοβιτσ",
        "ντραγκοφσκι",
        "φωτης",
        "γκολ",
        "πεναλτι",
        "europa",
        "conference",
        "champions",
        "κυπελλο",
        "ντερμπι"
    )

    fun normalizeGreek(input: String): String {
        val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
        return diacriticsRegex.replace(decomposed, "")
            .lowercase(elLocale)
            .replace('ς', 'σ')
            .replace(whitespaceRegex, " ")
            .trim()
    }

    fun isPanathinaikosRelated(title: String, summary: String): Boolean {
        val hay = " ${normalizeGreek("$title $summary")} "
        return paoKeywords.any { hay.contains(it) }
    }

    fun classifyCategory(
        title: String,
        summary: String,
        defaultCategory: String
    ): NewsCategory {
        val hay = normalizeGreek("$title $summary")
        return when {
            transferKeywords.any { hay.contains(it) } -> NewsCategory.TRANSFERS
            amateurKeywords.any { hay.contains(it) } -> NewsCategory.AMATEUR
            basketballKeywords.any { hay.contains(it) } -> NewsCategory.BASKETBALL
            footballKeywords.any { hay.contains(it) } -> NewsCategory.FOOTBALL
            else -> NewsCategory.fromKey(defaultCategory)
        }
    }

    fun cleanHtmlText(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val withoutImg = raw.replace(Regex("<img[^>]*>", RegexOption.IGNORE_CASE), "")
        val plain = HtmlCompat.fromHtml(withoutImg, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        return plain
            .replace('\u00A0', ' ')
            .replace("\uFFFC", "")
            .replace(whitespaceRegex, " ")
            .trim()
    }

    fun extractFirstImageUrl(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val match = imgTagRegex.find(html) ?: return null
        val candidate = match.groupValues.getOrNull(1)?.trim() ?: return null
        return if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
            candidate
        } else {
            null
        }
    }

    fun formatRelativeTimeEl(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        if (timestampMillis <= 0L) return "Πρόσφατα"
        val diff = (nowMillis - timestampMillis).coerceAtLeast(0L)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Μόλις τώρα"
            minutes == 1L -> "Πριν 1 λεπτό"
            minutes < 60 -> "Πριν $minutes λεπτά"
            hours == 1L -> "Πριν 1 ώρα"
            hours < 24 -> "Πριν $hours ώρες"
            days == 1L -> "Χθες"
            days < 7 -> "Πριν $days ημέρες"
            else -> {
                val sdf = SimpleDateFormat("d MMM yyyy", elLocale)
                sdf.format(Date(timestampMillis))
            }
        }
    }

    fun formatFullDateEl(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return ""
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy • HH:mm", elLocale)
        return sdf.format(Date(timestampMillis))
    }
}
