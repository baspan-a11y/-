package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.ArticleEntity
import com.example.data.model.NewsCategory
import com.example.ui.components.ArticleListCard
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EditorialTopHeader
import com.example.ui.components.FeaturedArticleCard
import com.example.ui.components.StatusBanner
import com.example.ui.viewmodel.NewsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    uiState: NewsUiState,
    onRefresh: () -> Unit,
    onSelectCategory: (NewsCategory) -> Unit,
    onClearSourceFilter: () -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDismissBanner: () -> Unit,
    onArticleClick: (ArticleEntity) -> Unit,
    onToggleSave: (ArticleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth()) {
            EditorialTopHeader(
                articleCount = uiState.articles.size,
                lastUpdatedMillis = uiState.lastUpdatedMillis,
                isRefreshing = uiState.isRefreshing,
                isSearchActive = uiState.isSearchActive,
                onToggleSearch = { onToggleSearch(!uiState.isSearchActive) },
                onRefresh = onRefresh
            )

            AnimatedVisibility(visible = uiState.isSearchActive) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Αναζήτηση (π.χ. Ναν, Ιωαννίδης, ΟΑΚΑ)…") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Καθαρισμός αναζήτησης"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("search_input_field")
                )
            }

            CategoryFilterRow(
                selectedCategory = uiState.selectedCategory,
                categoryCounts = uiState.categoryCounts,
                onSelectCategory = onSelectCategory,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            if (!uiState.selectedSourceName.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InputChip(
                        selected = true,
                        onClick = onClearSourceFilter,
                        label = { Text("Πηγή: ${uiState.selectedSourceName}") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Αφαίρεση φίλτρου πηγής",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("active_source_filter_chip")
                    )
                }
            }

            StatusBanner(
                message = uiState.statusBannerMessage,
                isError = uiState.isOfflineOrError,
                onDismiss = onDismissBanner
            )
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            when {
                uiState.articles.isEmpty() && uiState.isRefreshing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Συλλογή ειδήσεων Παναθηναϊκού…",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ανάκτηση από όλα τα ελληνικά αθλητικά μέσα",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                uiState.articles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Newspaper,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != NewsCategory.ALL || uiState.selectedSourceId != null) {
                                    "Δεν βρέθηκαν άρθρα με αυτά τα κριτήρια"
                                } else {
                                    "Δεν υπάρχουν διαθέσιμες ειδήσεις"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != NewsCategory.ALL || uiState.selectedSourceId != null) {
                                    OutlinedButton(
                                        onClick = {
                                            onSearchQueryChange("")
                                            onSelectCategory(NewsCategory.ALL)
                                            onClearSourceFilter()
                                        },
                                        modifier = Modifier.testTag("reset_filters_button")
                                    ) {
                                        Text("Καθαρισμός φίλτρων")
                                    }
                                }
                                Button(
                                    onClick = onRefresh,
                                    modifier = Modifier.testTag("empty_retry_button")
                                ) {
                                    Text("Ανανέωση")
                                }
                            }
                        }
                    }
                }

                else -> {
                    val featured = uiState.articles.first()
                    val rest = uiState.articles.drop(1)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("news_feed_list"),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "featured_${featured.id}") {
                            FeaturedArticleCard(
                                article = featured,
                                onClick = { onArticleClick(featured) },
                                onToggleSave = { onToggleSave(featured) }
                            )
                        }
                        items(rest, key = { it.id }) { article ->
                            ArticleListCard(
                                article = article,
                                onClick = { onArticleClick(article) },
                                onToggleSave = { onToggleSave(article) },
                                onShare = { shareArticle(context, article) }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun shareArticle(context: Context, article: ArticleEntity) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.title)
        putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.url}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Κοινοποίηση είδησης"))
}
