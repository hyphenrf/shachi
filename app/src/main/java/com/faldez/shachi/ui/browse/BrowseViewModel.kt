package com.faldez.shachi.ui.browse

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.SearchHistory
import com.faldez.shachi.data.model.ServerView
import com.faldez.shachi.data.model.TagDetail
import com.faldez.shachi.data.repository.*
import com.faldez.shachi.service.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class BrowseViewModel constructor(
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedSearchRepository: SavedSearchRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val state: StateFlow<UiState>
    val pagingDataFlow: Flow<PagingData<Post>>
    val accept: (UiAction) -> Unit

    init {
        Log.d("PostsViewModel",
            "init " + savedStateHandle.get(LAST_SEARCH_TAGS) + " " + savedStateHandle.get(
                LAST_TAGS_SCROLLED))
        val initialTags: String =
            savedStateHandle.get(LAST_SEARCH_TAGS)
                ?: ""
        val lastTagsScrolled: String = savedStateHandle.get(LAST_TAGS_SCROLLED) ?: ""
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches =
            actionStateFlow.filterIsInstance<UiAction.Search>().distinctUntilChanged()
        val tagsScrolled =
            actionStateFlow.filterIsInstance<UiAction.Scroll>().distinctUntilChanged()
                .onStart {
                    emit(UiAction.Scroll(null, currentTags = lastTagsScrolled))
                }
                .shareIn(scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    replay = 1)

        val getServer = getSelectedServer()

        state =
            combine(getServer, searches, tagsScrolled, ::Triple).map { (server, search, scroll) ->
                Log.d("BrowseViewModel",
                    "serverId=${server?.serverId} ${search.tags} != ${scroll.currentTags}) || (${server?.url} != ${scroll.currentServerUrl})")

                UiState(
                    server = server,
                    tags = search.tags,
                    lastTagsScrolled = scroll.currentTags,
                    hasNotScrolledForCurrentTag = (search.tags != scroll.currentTags) || (server?.url != scroll.currentServerUrl)
                )
            }.stateIn(scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = UiState()
            )

        pagingDataFlow =
            state.filter { it.server != null }
                .flatMapLatest {
                    searchPosts(it.server!!, tags = it.tags).map {
                        it.map { post ->
                            val postId =
                                favoriteRepository.queryByServerUrlAndPostId(post.serverId,
                                    post.postId)
                            post.favorite = postId != null
                            post
                        }
                    }
                }.cachedIn(viewModelScope)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                state.collect {
                    if (it.server != null && it.tags.isNotEmpty()) {
                        val tags = it.tags
                        Log.d("BrowseViewModel",
                            "insert ${it.server.serverId} $tags to history")
                        insertTagsTagsToHistory(it.server.serverId, tags)
                    }
                }
            }
        }
    }

    private fun getSelectedServer(): Flow<ServerView?> {
        return serverRepository.getSelectedServer()
    }

    fun selectServer(server: ServerView) {
        CoroutineScope(Dispatchers.IO).launch {
            serverRepository.setSelectedServer(server.serverId)
        }
    }


    private fun searchPosts(server: ServerView, tags: String): Flow<PagingData<Post>> {
        val action = Action.SearchPost(server, tags)
        return postRepository.getSearchPostsResultStream(action)
    }

    fun favoritePost(favorite: Post) {
        viewModelScope.launch {
            favoriteRepository.insert(favorite.copy(dateAdded = ZonedDateTime.now().toInstant()
                .toEpochMilli()))
        }
    }

    fun deleteFavoritePost(favorite: Post) {
        viewModelScope.launch {
            favoriteRepository.delete(favorite)
        }
    }

    fun saveSearch(title: String?, tags: String) {
        viewModelScope.launch {
            state.value.server?.let { server ->
                savedSearchRepository.insert(serverId = server.serverId,
                    tags = tags,
                    title = title ?: state.value.tags.split(" ").first())
            }
        }
    }

    private fun insertTagsTagsToHistory(serverId: Int, tags: String) =
        CoroutineScope(Dispatchers.IO).launch {
            searchHistoryRepository.insert(SearchHistory(tags = tags,
                createdAt = ZonedDateTime.now().toInstant().toEpochMilli(),
                serverId = serverId))
        }

    override fun onCleared() {
        savedStateHandle.set(LAST_SEARCH_SERVER, state.value.server)
        savedStateHandle.set(LAST_SEARCH_TAGS, state.value.tags)
        savedStateHandle.set(LAST_TAGS_SCROLLED, state.value.lastTagsScrolled)
        super.onCleared()
        Log.d("PostsViewModel",
            "onCleared " + state.value.tags + " " + state.value.lastTagsScrolled)
    }

    private fun List<TagDetail>.toQuery(): String {
        return this.joinToString(" ") { it.toString() }
    }
}

sealed class UiAction {
    data class Search(val tags: String) : UiAction()
    data class Scroll(
        val currentServerUrl: String?,
        val currentTags: String,
    ) : UiAction()

    object GetSelectedServer : UiAction()
    object GetSearchHistory : UiAction()
}

data class UiState(
    val server: ServerView? = null,
    val tags: String = "",
    val lastTagsScrolled: String = "",
    val hasNotScrolledForCurrentTag: Boolean = false,
)

private const val LAST_SEARCH_SERVER: String = "last_search_server"
private const val LAST_SEARCH_TAGS: String = "last_search_tags"
private const val LAST_TAGS_SCROLLED: String = "last_tags_scrolled"