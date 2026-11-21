package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.navigation.ArticleDetailRoute
import com.example.ui.navigation.FeedRoute
import com.example.ui.navigation.SavedRoute
import com.example.ui.navigation.SourcesRoute
import com.example.ui.screens.ArticleDetailScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.SourcesScreen
import com.example.ui.theme.PrasinaNeaTheme
import com.example.ui.viewmodel.NewsViewModel

private data class TopTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any,
    val tag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrasinaNeaTheme {
                PrasinaNeaApp()
            }
        }
    }
}

@Composable
fun PrasinaNeaApp(
    viewModel: NewsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isDetailScreen = currentDestination?.hasRoute<ArticleDetailRoute>() == true

    val tabs = listOf(
        TopTab(
            label = "Ροή",
            selectedIcon = Icons.Filled.Newspaper,
            unselectedIcon = Icons.Outlined.Newspaper,
            route = FeedRoute,
            tag = "nav_tab_feed"
        ),
        TopTab(
            label = "Πηγές",
            selectedIcon = Icons.Filled.RssFeed,
            unselectedIcon = Icons.Outlined.RssFeed,
            route = SourcesRoute,
            tag = "nav_tab_sources"
        ),
        TopTab(
            label = "Αποθηκευμένα",
            selectedIcon = Icons.Filled.Bookmarks,
            unselectedIcon = Icons.Outlined.Bookmarks,
            route = SavedRoute,
            tag = "nav_tab_saved"
        )
    )

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useNavRail && !isDetailScreen) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEach { tab ->
                            val selected = when (tab.route) {
                                is FeedRoute -> currentDestination?.hasRoute<FeedRoute>() == true
                                is SourcesRoute -> currentDestination?.hasRoute<SourcesRoute>() == true
                                is SavedRoute -> currentDestination?.hasRoute<SavedRoute>() == true
                                else -> false
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    TabIconWithOptionalBadge(
                                        tab = tab,
                                        selected = selected,
                                        savedCount = uiState.savedArticles.size
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail && !isDetailScreen) {
                    NavigationRail(
                        modifier = Modifier.statusBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEach { tab ->
                            val selected = when (tab.route) {
                                is FeedRoute -> currentDestination?.hasRoute<FeedRoute>() == true
                                is SourcesRoute -> currentDestination?.hasRoute<SourcesRoute>() == true
                                is SavedRoute -> currentDestination?.hasRoute<SavedRoute>() == true
                                else -> false
                            }
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    TabIconWithOptionalBadge(
                                        tab = tab,
                                        selected = selected,
                                        savedCount = uiState.savedArticles.size
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = FeedRoute
                    ) {
                        composable<FeedRoute> {
                            FeedScreen(
                                uiState = uiState,
                                onRefresh = viewModel::refreshAll,
                                onSelectCategory = viewModel::selectCategory,
                                onClearSourceFilter = { viewModel.selectSourceFilter(null) },
                                onToggleSearch = viewModel::toggleSearch,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onDismissBanner = viewModel::dismissBanner,
                                onArticleClick = { article ->
                                    viewModel.markArticleRead(article.id)
                                    navController.navigate(ArticleDetailRoute(article.id))
                                },
                                onToggleSave = viewModel::toggleSaveArticle
                            )
                        }

                        composable<SourcesRoute> {
                            SourcesScreen(
                                uiState = uiState,
                                onToggleSource = viewModel::toggleSourceEnabled,
                                onRefreshAll = viewModel::refreshAll,
                                onOpenSourceFeed = { sourceId ->
                                    viewModel.selectSourceFilter(sourceId)
                                    navigateToTab(FeedRoute)
                                },
                                onAddCustomSource = viewModel::addCustomSource,
                                onDeleteCustomSource = viewModel::deleteCustomSource
                            )
                        }

                        composable<SavedRoute> {
                            SavedScreen(
                                savedArticles = uiState.savedArticles,
                                onArticleClick = { article ->
                                    viewModel.markArticleRead(article.id)
                                    navController.navigate(ArticleDetailRoute(article.id))
                                },
                                onToggleSave = viewModel::toggleSaveArticle
                            )
                        }

                        composable<ArticleDetailRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<ArticleDetailRoute>()
                            val article by viewModel
                                .observeArticle(route.articleId)
                                .collectAsStateWithLifecycle(initialValue = null)

                            LaunchedEffect(route.articleId) {
                                viewModel.markArticleRead(route.articleId)
                            }

                            ArticleDetailScreen(
                                article = article,
                                onBack = { navController.popBackStack() },
                                onToggleSave = viewModel::toggleSaveArticle
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabIconWithOptionalBadge(
    tab: TopTab,
    selected: Boolean,
    savedCount: Int
) {
    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
    if (tab.route is SavedRoute && savedCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(if (savedCount > 99) "99+" else savedCount.toString())
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = tab.label)
        }
    } else {
        Icon(imageVector = icon, contentDescription = tab.label)
    }
}
