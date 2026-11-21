package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NewsSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY isCustom ASC, name ASC")
    fun observeAllSources(): Flow<List<NewsSourceEntity>>

    @Query("SELECT * FROM sources WHERE isEnabled = 1")
    suspend fun getEnabledSources(): List<NewsSourceEntity>

    @Query("SELECT * FROM sources WHERE id = :id LIMIT 1")
    suspend fun getSourceById(id: String): NewsSourceEntity?

    @Query("SELECT COUNT(*) FROM sources")
    suspend fun getSourceCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissingSources(sources: List<NewsSourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSource(source: NewsSourceEntity)

    @Query("UPDATE sources SET isEnabled = :enabled WHERE id = :id")
    suspend fun setSourceEnabled(id: String, enabled: Boolean)

    @Query(
        "UPDATE sources SET lastSyncTime = :syncTime, lastArticleCount = :count, lastError = :error WHERE id = :id"
    )
    suspend fun updateSyncStatus(id: String, syncTime: Long, count: Int, error: String?)

    @Query("DELETE FROM sources WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomSource(id: String)
}
