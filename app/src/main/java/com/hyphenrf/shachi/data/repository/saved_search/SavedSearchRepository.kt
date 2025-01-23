package com.hyphenrf.shachi.data.repository.saved_search

import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.model.SavedSearch
import com.hyphenrf.shachi.data.model.SavedSearchServer
import kotlinx.coroutines.flow.Flow

interface SavedSearchRepository {
    val db: AppDatabase

    fun getAllFlow(): Flow<List<SavedSearchServer>> = db.savedSearchDao().getAllFlow()

    suspend fun getAll(): List<SavedSearch> = db.savedSearchDao().getAll()

    suspend fun insert(tags: String, title: String, serverId: Int) =
        db.savedSearchDao().insert(tags, title, serverId)

    suspend fun update(savedSearch: SavedSearch) = db.savedSearchDao().update(savedSearch)

    suspend fun delete(savedSearch: SavedSearch) = db.savedSearchDao().delete(savedSearch)
}