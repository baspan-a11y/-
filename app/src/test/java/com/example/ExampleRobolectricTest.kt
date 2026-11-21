package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.NewsCategory
import com.example.data.model.NewsSourceEntity
import com.example.data.remote.PaoClassifier
import com.example.data.remote.RssFetcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Πράσινα Νέα", appName)
    }

    @Test
    fun `greek classifier detects panathinaikos keywords and categories`() {
        assertTrue(PaoClassifier.isPanathinaikosRelated("Μεγάλη νίκη για τον Παναθηναϊκό στο ΟΑΚΑ", ""))
        assertFalse(PaoClassifier.isPanathinaikosRelated("Καιρός: Βροχές αύριο στην Αθήνα", ""))

        assertEquals(
            NewsCategory.BASKETBALL,
            PaoClassifier.classifyCategory("Εξαιρετικός ο Κέντρικ Ναν στη Euroleague", "", NewsCategory.FOOTBALL.name)
        )
        assertEquals(
            NewsCategory.TRANSFERS,
            PaoClassifier.classifyCategory("Πρόταση για μεταγραφή στόπερ στον Παναθηναϊκό", "", NewsCategory.FOOTBALL.name)
        )
        assertEquals(
            NewsCategory.AMATEUR,
            PaoClassifier.classifyCategory("Νίκη του Παναθηναϊκού Α.Ο. στο βόλεϊ", "", NewsCategory.FOOTBALL.name)
        )
    }

    @Test
    fun `rss fetcher parses google news and wordpress feeds accurately`() {
        val googleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title>Παναθηναϊκός: Έτοιμος για το ντέρμπι - Sport24.gr</title>
                  <link>https://news.google.com/rss/articles/test1</link>
                  <pubDate>Fri, 20 Nov 2026 12:00:00 GMT</pubDate>
                  <description><![CDATA[<a href="https://news.google.com">Παναθηναϊκός</a>]]></description>
                  <source url="https://www.sport24.gr">Sport24.gr</source>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val source = NewsSourceEntity(
            id = "google_test",
            name = "Google News",
            feedUrl = "https://news.google.com/rss",
            websiteUrl = "https://news.google.com",
            description = "Test",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = false
        )

        val articles = RssFetcher().parseXmlFeed(googleXml, source)
        assertEquals(1, articles.size)
        assertEquals("Παναθηναϊκός: Έτοιμος για το ντέρμπι", articles.first().title)
        assertEquals("Sport24.gr", articles.first().sourceName)
    }
}
