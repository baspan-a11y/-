package com.example.data.repository

import com.example.data.local.ArticleDao
import com.example.data.local.SourceDao
import com.example.data.model.ArticleEntity
import com.example.data.model.NewsCategory
import com.example.data.model.NewsSourceEntity
import com.example.data.remote.DefaultSources
import com.example.data.remote.PaoClassifier
import com.example.data.remote.RssFetcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

data class SyncSummary(
    val totalSourcesAttempted: Int,
    val succeededSources: Int,
    val newOrUpdatedArticles: Int,
    val failedSources: List<String>
)

class NewsRepository(
    private val articleDao: ArticleDao,
    private val sourceDao: SourceDao,
    private val rssFetcher: RssFetcher = RssFetcher()
) {
    val allArticles: Flow<List<ArticleEntity>> = articleDao.observeAllArticles()
    val savedArticles: Flow<List<ArticleEntity>> = articleDao.observeSavedArticles()
    val allSources: Flow<List<NewsSourceEntity>> = sourceDao.observeAllSources()

    fun observeArticle(articleId: String): Flow<ArticleEntity?> =
        articleDao.observeArticleById(articleId)

    suspend fun ensureDefaultSources() {
        sourceDao.insertMissingSources(DefaultSources.entries)
    }

    suspend fun refreshAllEnabledSources(): SyncSummary = coroutineScope {
        ensureDefaultSources()
        val enabled = sourceDao.getEnabledSources()
        if (enabled.isEmpty()) {
            return@coroutineScope SyncSummary(0, 0, 0, emptyList())
        }

        val now = System.currentTimeMillis()
        val results = enabled.map { source ->
            async { source to rssFetcher.fetchFeed(source) }
        }.awaitAll()

        val savedIds = articleDao.getSavedIds().toHashSet()
        val readIds = articleDao.getReadIds().toHashSet()

        val collectedArticles = LinkedHashMap<String, ArticleEntity>()
        val seenNormalizedTitles = HashSet<String>()
        val failedNames = mutableListOf<String>()
        var succeeded = 0

        for ((source, result) in results) {
            if (result.error != null) {
                failedNames.add(source.name)
                sourceDao.updateSyncStatus(
                    id = source.id,
                    syncTime = now,
                    count = source.lastArticleCount,
                    error = result.error
                )
            } else {
                succeeded++
                sourceDao.updateSyncStatus(
                    id = source.id,
                    syncTime = now,
                    count = result.articles.size,
                    error = null
                )
                for (article in result.articles) {
                    val normTitle = PaoClassifier.normalizeGreek(article.title)
                    if (seenNormalizedTitles.add(normTitle)) {
                        val existing = collectedArticles[article.id]
                        val merged = article.copy(
                            isSaved = article.id in savedIds || (existing?.isSaved == true),
                            isRead = article.id in readIds || (existing?.isRead == true),
                            imageUrl = article.imageUrl ?: existing?.imageUrl
                        )
                        collectedArticles[article.id] = merged
                    }
                }
            }
        }

        val finalArticles = collectedArticles.values.toList()
        if (finalArticles.isNotEmpty()) {
            articleDao.upsertArticles(finalArticles)
        }

        val fourteenDaysAgo = now - TimeUnit.DAYS.toMillis(14)
        articleDao.pruneOldArticles(fourteenDaysAgo)

        SyncSummary(
            totalSourcesAttempted = enabled.size,
            succeededSources = succeeded,
            newOrUpdatedArticles = finalArticles.size,
            failedSources = failedNames
        )
    }

    suspend fun refreshSingleSource(sourceId: String): Boolean {
        val source = sourceDao.getSourceById(sourceId) ?: return false
        val now = System.currentTimeMillis()
        val result = rssFetcher.fetchFeed(source)
        if (result.error != null) {
            sourceDao.updateSyncStatus(source.id, now, source.lastArticleCount, result.error)
            return false
        }
        val savedIds = articleDao.getSavedIds().toHashSet()
        val readIds = articleDao.getReadIds().toHashSet()
        val merged = result.articles.map { article ->
            article.copy(
                isSaved = article.id in savedIds,
                isRead = article.id in readIds
            )
        }
        sourceDao.updateSyncStatus(source.id, now, merged.size, null)
        if (merged.isNotEmpty()) {
            articleDao.upsertArticles(merged)
        }
        return true
    }

    suspend fun toggleSourceEnabled(sourceId: String, enabled: Boolean) {
        sourceDao.setSourceEnabled(sourceId, enabled)
    }

    suspend fun addCustomSource(name: String, feedUrl: String, category: NewsCategory) {
        val normalizedUrl = if (feedUrl.startsWith("http://") || feedUrl.startsWith("https://")) {
            feedUrl.trim()
        } else {
            "https://${feedUrl.trim()}"
        }
        val website = normalizedUrl.substringBefore("/feed").ifBlank { normalizedUrl }
        val custom = NewsSourceEntity(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            feedUrl = normalizedUrl,
            websiteUrl = website,
            description = "Προσαρμοσμένη ροή RSS χρήστη",
            defaultCategory = category.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            isCustom = true,
            accentColorHex = 0xFF006837
        )
        sourceDao.upsertSource(custom)
        refreshSingleSource(custom.id)
    }

    suspend fun deleteCustomSource(sourceId: String) {
        articleDao.deleteUnsavedBySource(sourceId)
        sourceDao.deleteCustomSource(sourceId)
    }

    suspend fun toggleSaveArticle(articleId: String, currentlySaved: Boolean) {
        articleDao.setArticleSaved(articleId, !currentlySaved)
    }

    suspend fun markArticleRead(articleId: String) {
        articleDao.setArticleRead(articleId, true)
    }
}
