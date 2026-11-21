package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun observeAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isSaved = 1 ORDER BY publishedAt DESC")
    fun observeSavedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    fun observeArticleById(id: String): Flow<ArticleEntity?>

    @Query("SELECT id FROM articles WHERE isSaved = 1")
    suspend fun getSavedIds(): List<String>

    @Query("SELECT id FROM articles WHERE isRead = 1")
    suspend fun getReadIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArticles(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isSaved = :saved WHERE id = :id")
    suspend fun setArticleSaved(id: String, saved: Boolean)

    @Query("UPDATE articles SET isRead = :read WHERE id = :id")
    suspend fun setArticleRead(id: String, read: Boolean)

    @Query("DELETE FROM articles WHERE sourceId = :sourceId AND isSaved = 0")
    suspend fun deleteUnsavedBySource(sourceId: String)

    @Query("DELETE FROM articles WHERE isSaved = 0 AND publishedAt < :cutoffTimestamp")
    suspend fun pruneOldArticles(cutoffTimestamp: Long)
}
