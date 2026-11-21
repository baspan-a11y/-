package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object FeedRoute

@Serializable
data object SourcesRoute

@Serializable
data object SavedRoute

@Serializable
data class ArticleDetailRoute(val articleId: String)
