package com.hyphenrf.shachi.data.repository.blacklist_tag

import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.model.BlacklistedTag
import com.hyphenrf.shachi.data.model.BlacklistedTagWithServer
import com.hyphenrf.shachi.data.model.ServerBlacklistedTagCrossRef
import kotlinx.coroutines.flow.Flow

interface BlacklistTagRepository {
    val db: AppDatabase

    fun getAllFlow(): Flow<List<BlacklistedTagWithServer>?> = db.blacklistedTagDao().getAllFlow()

    suspend fun getAll(): List<BlacklistedTagWithServer>? = db.blacklistedTagDao().getAll()

    suspend fun getByTags(tags: String): BlacklistedTag? = db.blacklistedTagDao().getByTags(tags)

    suspend fun insertBlacklistedTag(blacklistedTag: BlacklistedTag): Long =
        db.blacklistedTagDao().insertBlacklistedTag(blacklistedTag)

    suspend fun insertBlacklistedTag(serverBlacklistedTagCrossRefs: List<ServerBlacklistedTagCrossRef>) =
        db.blacklistedTagDao().insertBlacklistTag(serverBlacklistedTagCrossRefs)

    suspend fun deleteBlacklistedTag(blacklistedTag: BlacklistedTag) =
        db.blacklistedTagDao().deleteBlacklistTag(blacklistedTag)
}