package com.example.data.remote

import android.util.Xml
import com.example.data.model.ArticleEntity
import com.example.data.model.NewsSourceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class FeedFetchResult(
    val sourceId: String,
    val articles: List<ArticleEntity>,
    val error: String? = null
)

class RssFetcher {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val dateFormats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "dd MMM yyyy HH:mm:ss Z",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd HH:mm:ss"
    ).map { pattern ->
        SimpleDateFormat(pattern, Locale.ENGLISH).apply {
            isLenient = true
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    suspend fun fetchFeed(source: NewsSourceEntity): FeedFetchResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(source.feedUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36"
                )
                .header("Accept", "application/rss+xml, application/xml, text/xml;q=0.9, */*;q=0.8")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext FeedFetchResult(
                        sourceId = source.id,
                        articles = emptyList(),
                        error = "HTTP ${response.code}"
                    )
                }
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) {
                    return@withContext FeedFetchResult(
                        sourceId = source.id,
                        articles = emptyList(),
                        error = "Κενή απόκριση"
                    )
                }
                val parsed = parseXmlFeed(body, source)
                FeedFetchResult(sourceId = source.id, articles = parsed, error = null)
            }
        } catch (e: Exception) {
            FeedFetchResult(
                sourceId = source.id,
                articles = emptyList(),
                error = e.localizedMessage ?: "Αποτυχία σύνδεσης"
            )
        }
    }

    internal fun parseXmlFeed(xmlContent: String, source: NewsSourceEntity): List<ArticleEntity> {
        val articles = mutableListOf<ArticleEntity>()
        val now = System.currentTimeMillis()

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlContent.trimStart('\uFEFF', ' ', '\n', '\r', '\t')))

        var insideItem = false
        var isAtomEntry = false
        var rawTitle = ""
        var link = ""
        var rawDescription = ""
        var rawContentEncoded = ""
        var pubDateRaw = ""
        var imageUrl: String? = null
        var googleNewsSourceName = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.lowercase(Locale.US)
                    if (tag == "item" || tag == "entry") {
                        insideItem = true
                        isAtomEntry = (tag == "entry")
                        rawTitle = ""
                        link = ""
                        rawDescription = ""
                        rawContentEncoded = ""
                        pubDateRaw = ""
                        imageUrl = null
                        googleNewsSourceName = ""
                    } else if (insideItem) {
                        when (tag) {
                            "title" -> rawTitle = safeNextText(parser)
                            "link" -> {
                                val href = parser.getAttributeValue(null, "href")
                                if (isAtomEntry && !href.isNullOrBlank()) {
                                    val rel = parser.getAttributeValue(null, "rel")
                                    if (rel.isNullOrBlank() || rel == "alternate") {
                                        link = href.trim()
                                    }
                                    skipTagIfNeeded(parser)
                                } else {
                                    val textLink = safeNextText(parser)
                                    if (textLink.isNotBlank()) link = textLink.trim()
                                }
                            }
                            "description", "summary" -> {
                                val text = safeNextText(parser)
                                if (rawDescription.isBlank()) rawDescription = text
                            }
                            "content:encoded", "content" -> {
                                val text = safeNextText(parser)
                                if (text.length > rawContentEncoded.length) rawContentEncoded = text
                            }
                            "pubdate", "published", "updated", "dc:date" -> {
                                val text = safeNextText(parser)
                                if (pubDateRaw.isBlank()) pubDateRaw = text
                            }
                            "source" -> {
                                googleNewsSourceName = safeNextText(parser).trim()
                            }
                            "media:content", "media:thumbnail", "enclosure" -> {
                                val candidateUrl = parser.getAttributeValue(null, "url")?.trim()
                                val type = parser.getAttributeValue(null, "type")?.lowercase(Locale.US)
                                if (imageUrl == null && !candidateUrl.isNullOrBlank() &&
                                    (type == null || type.startsWith("image/") ||
                                        candidateUrl.contains(".jpg", ignoreCase = true) ||
                                        candidateUrl.contains(".jpeg", ignoreCase = true) ||
                                        candidateUrl.contains(".png", ignoreCase = true) ||
                                        candidateUrl.contains(".webp", ignoreCase = true))
                                ) {
                                    imageUrl = candidateUrl
                                }
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val tag = parser.name.lowercase(Locale.US)
                    if ((tag == "item" || tag == "entry") && insideItem) {
                        insideItem = false
                        val cleanedTitle = cleanGoogleTitle(
                            PaoClassifier.cleanHtmlText(rawTitle),
                            googleNewsSourceName
                        )
                        if (cleanedTitle.isNotBlank() && link.isNotBlank()) {
                            val richHtml = if (rawContentEncoded.isNotBlank()) rawContentEncoded else rawDescription
                            val extractedImg = imageUrl
                                ?: PaoClassifier.extractFirstImageUrl(richHtml)
                                ?: PaoClassifier.extractFirstImageUrl(rawDescription)

                            val cleanedDesc = PaoClassifier.cleanHtmlText(rawDescription)
                            val cleanedFull = PaoClassifier.cleanHtmlText(richHtml)

                            val summary = buildSummary(cleanedTitle, cleanedDesc, cleanedFull)
                            val fullContent = if (cleanedFull.length > summary.length) cleanedFull else summary

                            val passesFilter = !source.requiresPaoKeywordFilter ||
                                PaoClassifier.isPanathinaikosRelated(cleanedTitle, summary)

                            if (passesFilter) {
                                val publishedAt = parseDateMillis(pubDateRaw) ?: now
                                val category = PaoClassifier.classifyCategory(
                                    title = cleanedTitle,
                                    summary = "$summary $fullContent",
                                    defaultCategory = source.defaultCategory
                                )
                                val displaySource = googleNewsSourceName.ifBlank { source.name }
                                val id = stableId(link, cleanedTitle)
                                articles.add(
                                    ArticleEntity(
                                        id = id,
                                        title = cleanedTitle,
                                        summary = summary,
                                        fullContent = fullContent,
                                        url = link,
                                        imageUrl = extractedImg,
                                        sourceId = source.id,
                                        sourceName = displaySource,
                                        category = category.name,
                                        publishedAt = publishedAt,
                                        fetchedAt = now
                                    )
                                )
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return articles
    }

    private fun safeNextText(parser: XmlPullParser): String {
        return try {
            parser.nextText() ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun skipTagIfNeeded(parser: XmlPullParser) {
        try {
            var depth = 1
            while (depth > 0) {
                when (parser.next()) {
                    XmlPullParser.END_TAG -> depth--
                    XmlPullParser.START_TAG -> depth++
                    XmlPullParser.END_DOCUMENT -> break
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun cleanGoogleTitle(title: String, googlePublisher: String): String {
        if (googlePublisher.isBlank()) return title
        val suffix = " - $googlePublisher"
        return if (title.endsWith(suffix, ignoreCase = true)) {
            title.dropLast(suffix.length).trim()
        } else {
            title
        }
    }

    private fun buildSummary(title: String, desc: String, full: String): String {
        val candidate = when {
            desc.isNotBlank() && !desc.equals(title, ignoreCase = true) && !desc.startsWith(title) -> desc
            full.isNotBlank() && !full.equals(title, ignoreCase = true) -> full
            else -> ""
        }
        return if (candidate.length > 240) {
            candidate.take(237).trimEnd() + "…"
        } else {
            candidate
        }
    }

    private fun parseDateMillis(raw: String): Long? {
        if (raw.isBlank()) return null
        val trimmed = raw.trim()
        for (formatter in dateFormats) {
            try {
                synchronized(formatter) {
                    val parsed = formatter.parse(trimmed)
                    if (parsed != null) return parsed.time
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun stableId(url: String, title: String): String {
        val normalized = if (url.contains("news.google.com/rss/articles")) {
            // Google News URLs are unique per article, fallback to normalized title for cross-source stability
            PaoClassifier.normalizeGreek(title)
        } else {
            url.substringBefore('?').trimEnd('/').lowercase(Locale.US)
        }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(normalized.toByteArray(Charsets.UTF_8))
        return digest.take(12).joinToString("") { "%02x".format(it) }
    }
}
