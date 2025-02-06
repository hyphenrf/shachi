package com.hyphenrf.shachi.data.repository.comment

import android.util.Log
import com.hyphenrf.shachi.data.model.Comment
import com.hyphenrf.shachi.data.model.ServerType
import com.hyphenrf.shachi.data.model.response.mapToComments
import com.hyphenrf.shachi.data.api.Action
import com.hyphenrf.shachi.data.api.BooruApi

interface CommentRepository {
    val booruApi: BooruApi

    suspend fun getComments(action: Action.GetComments): List<Comment> =
        when (action.server.type) {
            ServerType.Gelbooru -> {
                action.buildGelbooruUrl().toString().let {
                    Log.d("CommentRepository/Gelbooru", it)
                    booruApi.gelbooru.getComments(it).mapToComments()
                }
            }
            ServerType.Moebooru -> {
                action.buildMoebooruUrl().toString().let {
                    Log.d("CommentRepository/Moebooru", it)
                    booruApi.moebooru.getComments(it).mapToComments()
                }
            }
            ServerType.Danbooru -> {
                action.buildDanbooruUrl().toString().let {
                    Log.d("CommentRepository/Danbooru", it)
                    booruApi.danbooru.getComments(it).mapToComments()
                }
            }
        }
}
