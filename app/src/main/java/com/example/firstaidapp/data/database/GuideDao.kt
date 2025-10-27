package com.example.firstaidapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.firstaidapp.data.models.FirstAidGuide

// DAO for first aid guides: queries and CRUD operations
@Dao
interface GuideDao {
    // All guides ordered by category then title
    @Query("SELECT * FROM first_aid_guides ORDER BY category, title")
    fun getAllGuides(): LiveData<List<FirstAidGuide>>

    // Single guide by ID
    @Query("SELECT * FROM first_aid_guides WHERE id = :guideId")
    fun getGuideById(guideId: String): LiveData<FirstAidGuide?>

    // Guides in a category
    @Query("SELECT * FROM first_aid_guides WHERE category = :category ORDER BY title")
    fun getGuidesByCategory(category: String): LiveData<List<FirstAidGuide>>

    // Search guides (LiveData)
    @Query("SELECT * FROM first_aid_guides WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchGuides(query: String): LiveData<List<FirstAidGuide>>

    // Search guides (suspend list)
    @Query("SELECT * FROM first_aid_guides WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    suspend fun searchGuidesList(query: String): List<FirstAidGuide>

    // Favorite guides ordered by last accessed
    @Query("SELECT * FROM first_aid_guides WHERE isFavorite = 1 ORDER BY lastAccessedTimestamp DESC")
    fun getFavoriteGuides(): LiveData<List<FirstAidGuide>>

    // Distinct categories
    @Query("SELECT DISTINCT category FROM first_aid_guides ORDER BY category")
    fun getAllCategories(): LiveData<List<String>>

    // Count guides
    @Query("SELECT COUNT(*) FROM first_aid_guides")
    suspend fun getGuidesCount(): Int

    // Insert or replace a guide
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuide(guide: FirstAidGuide)

    // Bulk insert/replace guides
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(guides: List<FirstAidGuide>)

    // Update a guide
    @Update
    suspend fun updateGuide(guide: FirstAidGuide)

    // Update favorite flag
    @Query("UPDATE first_aid_guides SET isFavorite = :isFavorite WHERE id = :guideId")
    suspend fun updateFavoriteStatus(guideId: String, isFavorite: Boolean)

    // Update last accessed timestamp
    @Query("UPDATE first_aid_guides SET lastAccessedTimestamp = :timestamp WHERE id = :guideId")
    suspend fun updateLastAccessed(guideId: String, timestamp: Long)

    // Delete a guide
    @Delete
    suspend fun deleteGuide(guide: FirstAidGuide)

    // Delete all guides
    @Query("DELETE FROM first_aid_guides")
    suspend fun deleteAllGuides()
}
