package com.example.voigkampff.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace if a similar entry (e.g., same username, different timestamp) is somehow added, or adjust strategy
    suspend fun insertSearchEntry(entry: SearchHistoryEntry)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAllSearchHistory(): Flow<List<SearchHistoryEntry>> // Use Flow for reactive updates

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :count")
    fun getRecentSearchHistory(count: Int): Flow<List<SearchHistoryEntry>>

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchEntryById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAllSearchHistory()
}