package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    indices = [
        Index(value = ["publishedAt"]),
        Index(value = ["sourceId"]),
        Index(value = ["category"]),
        Index(value = ["isSaved"])
    ]
)
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val fullContent: String,
    val url: String,
    val imageUrl: String?,
    val sourceId: String,
    val sourceName: String,
    val category: String,
    val publishedAt: Long,
    val fetchedAt: Long,
    val isSaved: Boolean = false,
    val isRead: Boolean = false
)
