package com.hyphenrf.shachi.data.api

import com.hyphenrf.shachi.data.model.SavedSearchServer
import com.hyphenrf.shachi.data.model.Server
import com.hyphenrf.shachi.data.model.ServerView
import com.hyphenrf.shachi.data.repository.post.PostRepository.Companion.NETWORK_PAGE_SIZE
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

sealed class Action {
    /*
    Search post
     */
    data class SearchPost(
        val server: ServerView,
        val tags: String = "*",
        val limit: Int = NETWORK_PAGE_SIZE,
        val start: Int? = null,
    ) :
        Action() {
        fun buildGelbooruUrl(page: Int): HttpUrl? {
            return server.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "post")
                    .addQueryParameter("pid", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", tags)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("user_id", it.username).build()

            }
        }

        fun buildMoebooruUrl(page: Int): HttpUrl? {
            return server.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("post.json")
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", tags)
                    .addQueryParameter("password_hash", it.password)
                    .addQueryParameter("login", it.username).build()
            }
        }

        fun buildDanbooruUrl(page: Int): HttpUrl? {
            return server.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("posts.json")
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", tags)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("login", it.username).build()
            }
        }
    }

    /*
    Search post of saved search tags
     */
    data class SearchSavedSearchPost(val savedSearch: SavedSearchServer) : Action() {
        fun buildGelbooruUrl(page: Int, limit: Int = NETWORK_PAGE_SIZE): HttpUrl? {
            return savedSearch.server.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "post")
                    .addQueryParameter("pid", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", savedSearch.savedSearch.tags)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("user_id", it.username).build()
            }
        }

        fun buildMoebooruUrl(page: Int, limit: Int = NETWORK_PAGE_SIZE): HttpUrl? {
            return savedSearch.server.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("post.json")
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", savedSearch.savedSearch.tags)
                    .addQueryParameter("password_hash", it.password)
                    .addQueryParameter("login", it.username).build()
            }
        }

        fun buildDanbooruUrl(page: Int, limit: Int = NETWORK_PAGE_SIZE): HttpUrl? {
            return savedSearch.server.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("posts.json")
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", savedSearch.savedSearch.tags)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("login", it.username).build()
            }
        }
    }

    /*
    Search tag with pattern
     */
    data class SearchTag(val server: Server?, val tag: String, val limit: Int = 10) : Action() {
        fun buildGelbooruUrl(): HttpUrl? {
            return server?.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index")
                    .addQueryParameter("s", "tag")
                    .addQueryParameter("name_pattern", "$tag%")
                    .addQueryParameter("order", "DESC")
                    .addQueryParameter("orderby", "count")
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("user_id", it.username).build()
            }
        }

        fun buildMoebooruUrl(): HttpUrl? {
            return server?.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("tag.json")
                    .addQueryParameter("name", "$tag*")
                    .addQueryParameter("order", "count")
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("password_hash", it.password)
                    .addQueryParameter("login", it.username).build()
            }
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return server?.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("tags.json")
                    .addEncodedQueryParameter("search[name_like]", "$tag*")
                    .addEncodedQueryParameter("search[order]", "count")
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("login", it.username).build()
            }
        }
    }

    /*
    Get single exact tag details
     */
    data class GetTag(val server: Server?, val tag: String) : Action() {
        fun buildGelbooruUrl(): HttpUrl? {
            return server?.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "tag")
                    .addQueryParameter("name", tag)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("user_id", it.username)
                    .build()
            }
        }

        fun buildMoebooruUrl(): HttpUrl? {
            return server?.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("tag.json")
                    .addQueryParameter("name", "$tag*")
                    .addQueryParameter("limit", "1")
                    .addQueryParameter("password_hash", it.password)
                    .addQueryParameter("login", it.username)
                    .build()
            }
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return server?.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("tags.json")
                    .addEncodedQueryParameter("search[name]", tag)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("login", it.username)
                    .build()
            }
        }
    }

    /*
    Get multiple tags details
     */
    data class GetTags(val server: Server?, val tags: String) : Action() {
        fun buildGelbooruUrl(): HttpUrl? {
            return server?.let {
                it.url.toHttpUrl().newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "tag")
                    .addQueryParameter("names", tags)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("user_id", it.username)
                    .build()
            }
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return server?.let {
                val tagsList = tags.split(" ")
                it.url.toHttpUrl().newBuilder().addPathSegment("tags.json").apply {
                    tagsList.forEach { tag ->
                        addEncodedQueryParameter("search[name_array][]", tag)
                    }
                    addQueryParameter("limit", tagsList.size.toString())
                }.addQueryParameter("api_key", it.password)
                    .addQueryParameter("login", it.username).build()

            }
        }
    }

    data class GetTagsSummary(val server: Server?) : Action() {
        fun buildMoebooruUrl(): HttpUrl? = server?.let {
            it.url.toHttpUrl().newBuilder().addPathSegment("tag").addPathSegment("summary.json")
                .addQueryParameter("password_hash", it.password)
                .addQueryParameter("login", it.username)
                .build()
        }
    }

    data class GetComments(val server: Server, val postId: Int) : Action() {
        fun buildGelbooruUrl(): HttpUrl? {
            return server.url.toHttpUrl().newBuilder().addPathSegment("index.php")
                .addQueryParameter("page", "dapi")
                .addQueryParameter("q", "index").addQueryParameter("s", "comment")
                .addQueryParameter("post_id", postId.toString())
                .addQueryParameter("api_key", server.password)
                .addQueryParameter("user_id", server.username)
                .build()
        }

        fun buildMoebooruUrl(): HttpUrl? {
            return server.url.toHttpUrl().newBuilder().addPathSegment("comment.json")
                .addQueryParameter("post_id", postId.toString())
                .addQueryParameter("password_hash", server.password)
                .addQueryParameter("login", server.username).build()
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return server.url.toHttpUrl().newBuilder().addPathSegment("comments.json")
                .addQueryParameter("group_by", "comment")
                .addEncodedQueryParameter("search[post_id]", postId.toString())
                .addEncodedQueryParameter("only",
                    "id,post_id,body,score,created_at,updated_at,updater_id,do_not_bump_post,is_deleted,is_sticky,creator[id,name]")
                .addQueryParameter("api_key", server.password)
                .addQueryParameter("login", server.username)
                .build()
        }
    }
}
