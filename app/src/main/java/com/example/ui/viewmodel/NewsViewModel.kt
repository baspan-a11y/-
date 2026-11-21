package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ArticleEntity
import com.example.data.model.NewsCategory
import com.example.data.model.NewsSourceEntity
import com.example.data.remote.PaoClassifier
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val articles: List<ArticleEntity> = emptyList(),
    val savedArticles: List<ArticleEntity> = emptyList(),
    val sources: List<NewsSourceEntity> = emptyList(),
    val categoryCounts: Map<NewsCategory, Int> = emptyMap(),
    val selectedCategory: NewsCategory = NewsCategory.ALL,
    val selectedSourceId: String? = null,
    val selectedSourceName: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val lastUpdatedMillis: Long? = null,
    val statusBannerMessage: String? = null,
    val isOfflineOrError: Boolean = false
)

private data class FilterState(
    val category: NewsCategory = NewsCategory.ALL,
    val sourceId: String? = null,
    val query: String = "",
    val isSearchActive: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val lastUpdatedMillis: Long? = null,
    val statusBannerMessage: String? = null,
    val isOfflineOrError: Boolean = false
)

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = NewsRepository(
        articleDao = database.articleDao(),
        sourceDao = database.sourceDao()
    )

    private val filterState = MutableStateFlow(FilterState())

    val uiState: StateFlow<NewsUiState> = combine(
        repository.allArticles,
        repository.savedArticles,
        repository.allSources,
        filterState
    ) { allArticles, savedArticles, sources, filter ->
        val normalizedQuery = PaoClassifier.normalizeGreek(filter.query)

        val sourceFiltered = if (filter.sourceId != null) {
            allArticles.filter { it.sourceId == filter.sourceId }
        } else {
            allArticles
        }

        val queryFiltered = if (normalizedQuery.isNotBlank()) {
            sourceFiltered.filter { article ->
                PaoClassifier.normalizeGreek(article.title).contains(normalizedQuery) ||
                    PaoClassifier.normalizeGreek(article.summary).contains(normalizedQuery) ||
                    PaoClassifier.normalizeGreek(article.sourceName).contains(normalizedQuery)
            }
        } else {
            sourceFiltered
        }

        val counts = buildMap {
            put(NewsCategory.ALL, queryFiltered.size)
            NewsCategory.entries
                .filter { it != NewsCategory.ALL }
                .forEach { cat ->
                    put(cat, queryFiltered.count { it.category == cat.name })
                }
        }

        val finalArticles = if (filter.category == NewsCategory.ALL) {
            queryFiltered
        } else {
            queryFiltered.filter { it.category == filter.category.name }
        }

        val activeSourceName = filter.sourceId?.let { id ->
            sources.firstOrNull { it.id == id }?.name
        }

        NewsUiState(
            articles = finalArticles,
            savedArticles = savedArticles,
            sources = sources,
            categoryCounts = counts,
            selectedCategory = filter.category,
            selectedSourceId = filter.sourceId,
            selectedSourceName = activeSourceName,
            searchQuery = filter.query,
            isSearchActive = filter.isSearchActive,
            isRefreshing = filter.isRefreshing,
            hasLoadedOnce = filter.hasLoadedOnce,
            lastUpdatedMillis = filter.lastUpdatedMillis ?: sources.mapNotNull { it.lastSyncTime }.maxOrNull(),
            statusBannerMessage = filter.statusBannerMessage,
            isOfflineOrError = filter.isOfflineOrError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NewsUiState(isRefreshing = true)
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultSources()
            refreshAll()
        }
    }

    fun observeArticle(articleId: String): Flow<ArticleEntity?> =
        repository.observeArticle(articleId)

    fun refreshAll() {
        if (filterState.value.isRefreshing) return
        viewModelScope.launch {
            filterState.update {
                it.copy(isRefreshing = true, statusBannerMessage = null, isOfflineOrError = false)
            }
            val summary = repository.refreshAllEnabledSources()
            val now = System.currentTimeMillis()
            val banner: String?
            val isErr: Boolean
            when {
                summary.totalSourcesAttempted == 0 -> {
                    banner = "Δεν έχετε ενεργοποιήσει κάποια πηγή ειδήσεων."
                    isErr = false
                }
                summary.succeededSources == 0 -> {
                    banner = "Αδυναμία σύνδεσης. Προβάλλονται τα αποθηκευμένα νέα."
                    isErr = true
                }
                summary.failedSources.isNotEmpty() -> {
                    banner = "Ενημερώθηκαν ${summary.succeededSources}/${summary.totalSourcesAttempted} πηγές."
                    isErr = false
                }
                else -> {
                    banner = null
                    isErr = false
                }
            }
            filterState.update {
                it.copy(
                    isRefreshing = false,
                    hasLoadedOnce = true,
                    lastUpdatedMillis = now,
                    statusBannerMessage = banner,
                    isOfflineOrError = isErr
                )
            }
        }
    }

    fun selectCategory(category: NewsCategory) {
        filterState.update { it.copy(category = category) }
    }

    fun selectSourceFilter(sourceId: String?) {
        filterState.update {
            it.copy(sourceId = sourceId, category = NewsCategory.ALL)
        }
    }

    fun toggleSearch(active: Boolean) {
        filterState.update {
            it.copy(
                isSearchActive = active,
                query = if (active) it.query else ""
            )
        }
    }

    fun updateSearchQuery(query: String) {
        filterState.update { it.copy(query = query) }
    }

    fun dismissBanner() {
        filterState.update { it.copy(statusBannerMessage = null) }
    }

    fun toggleSaveArticle(article: ArticleEntity) {
        viewModelScope.launch {
            repository.toggleSaveArticle(article.id, article.isSaved)
        }
    }

    fun markArticleRead(articleId: String) {
        viewModelScope.launch {
            repository.markArticleRead(articleId)
        }
    }

    fun toggleSourceEnabled(sourceId: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleSourceEnabled(sourceId, enabled)
        }
    }

    fun refreshSingleSource(sourceId: String) {
        viewModelScope.launch {
            filterState.update { it.copy(isRefreshing = true) }
            repository.refreshSingleSource(sourceId)
            filterState.update {
                it.copy(isRefreshing = false, lastUpdatedMillis = System.currentTimeMillis())
            }
        }
    }

    fun addCustomSource(name: String, feedUrl: String, category: NewsCategory) {
        viewModelScope.launch {
            filterState.update { it.copy(isRefreshing = true) }
            repository.addCustomSource(name, feedUrl, category)
            filterState.update {
                it.copy(isRefreshing = false, lastUpdatedMillis = System.currentTimeMillis())
            }
        }
    }

    fun deleteCustomSource(sourceId: String) {
        viewModelScope.launch {
            if (filterState.value.sourceId == sourceId) {
                filterState.update { it.copy(sourceId = null) }
            }
            repository.deleteCustomSource(sourceId)
        }
    }
}
