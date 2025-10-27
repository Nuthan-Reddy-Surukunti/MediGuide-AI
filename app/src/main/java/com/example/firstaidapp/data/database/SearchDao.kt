package com.example.firstaidapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.firstaidapp.data.models.SearchHistory

// DAO for search history: recent searches and maintenance
@Dao
interface SearchDao {
    // Last 10 searches
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): LiveData<List<SearchHistory>>

    // Last 5 distinct query strings (alias to avoid SQL parser issues)
    @Query("SELECT DISTINCT query AS q FROM search_history ORDER BY timestamp DESC LIMIT 5")
    fun getRecentSearchQueries(): LiveData<List<String>>

    // Insert or replace a search record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory)

    // Delete searches older than cutoffTime
    @Query("DELETE FROM search_history WHERE timestamp < :cutoffTime")
    suspend fun deleteOldSearches(cutoffTime: Long)

    // Clear all search history
    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
