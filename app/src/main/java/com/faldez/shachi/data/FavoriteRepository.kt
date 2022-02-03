package com.faldez.shachi.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(private val db: AppDatabase) {
    suspend fun insert(favorite: Post) {
        db.favoriteDao().insert(favorite)
    }


    suspend fun delete(favorite: Post) {
        db.favoriteDao().delete(favorite)
    }

    fun query(tags: String): Flow<PagingData<Post>> {
        val query = tags.map { "tags:$it" }.joinToString(separator = " ")
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (query.isEmpty()) {
                    db.favoriteDao().query()
                } else {
                    db.favoriteDao().queryByTags(query)
                }
            }
        ).flow
    }

    fun queryByServerUrlAndPostIds(serverId: Int, postIds: List<Int>): Flow<Set<Int>> {
        return db.favoriteDao().queryByServerUrlAndPostIds(serverId, postIds).map { it.toSet() }
    }

    suspend fun queryByServerUrlAndPostId(serverId: Int, postId: Int): Int? {
        return db.favoriteDao().queryByServerUrlAndPostId(serverId, postId)
    }
}