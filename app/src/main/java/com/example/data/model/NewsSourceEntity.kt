package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class NewsSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val feedUrl: String,
    val websiteUrl: String,
    val description: String,
    val defaultCategory: String,
    val requiresPaoKeywordFilter: Boolean,
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val accentColorHex: Long = 0xFF006837,
    val lastSyncTime: Long? = null,
    val lastArticleCount: Int = 0,
    val lastError: String? = null
)
