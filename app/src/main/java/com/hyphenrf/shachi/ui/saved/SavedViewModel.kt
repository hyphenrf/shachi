package com.hyphenrf.shachi.ui.saved

import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.hyphenrf.shachi.data.model.Post
import com.hyphenrf.shachi.data.model.SavedSearch
import com.hyphenrf.shachi.data.model.SavedSearchServer
import com.hyphenrf.shachi.data.model.applyFilters
import com.hyphenrf.shachi.data.preference.Filter
import com.hyphenrf.shachi.data.repository.favorite.FavoriteRepository
import com.hyphenrf.shachi.data.repository.post.PostRepository
import com.hyphenrf.shachi.data.repository.saved_search.SavedSearchRepository
import com.hyphenrf.shachi.data.api.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class SavedViewModel(
    private val savedSearchRepository: SavedSearchRepository,
    private val postRepository: PostRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val savedSearchFlow: Flow<List<SavedSearchPost>>
    val scrollState: MutableStateFlow<SparseArray<Int>> =
        savedStateHandle[LAST_SCROLL_POSITIONS] ?: MutableStateFlow(SparseArray<Int>())
    val state: Flow<UiState>

    val accept: (UiAction) -> Unit

    init {
        val actionSharedFlow = MutableSharedFlow<UiAction>()

        state =
            actionSharedFlow.filterIsInstance<UiAction.SetFilters>().distinctUntilChanged().map {
                UiState(
                    it.questionableFilter,
                    it.explicitFilter
                )
            }.shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                replay = 1
            )

        val listFlow = savedSearchRepository.getAllFlow().distinctUntilChanged()

        savedSearchFlow = combine(state, listFlow, ::Pair)
            .map { (filters, data) ->
                Log.d("SavedViewModel", "collect savedSearches")
                data.map { savedSearch ->
                    val posts = getSearchPosts(savedSearch).map {
                        it.applyFilters(savedSearch.server.blacklistedTags,
                            filters.questionableFilter == Filter.Mute,
                            filters.explicitFilter == Filter.Mute)
                    }.cachedIn(viewModelScope)
                    SavedSearchPost(savedSearch = savedSearch, posts = posts)
                }
            }.shareIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                replay = 1
            )

        accept = { action ->
            viewModelScope.launch {
                actionSharedFlow.emit(action)
            }
        }
    }

    private fun getSearchPosts(
        savedSearch: SavedSearchServer,
    ): Flow<PagingData<Post>> {
        return postRepository.getSearchPostsResultStream(Action.SearchPost(server = savedSearch.server,
            tags = savedSearch.savedSearch.tags, limit = 10)).map { pagingData ->
            pagingData.map { post ->
                val postId =
                    favoriteRepository.queryByServerUrlAndPostId(post.serverId,
                        post.postId)
                post.favorite = postId != null
                post
            }
        }
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

    fun posts(savedSearchId: Int): Flow<PagingData<Post>?> = savedSearchFlow.flatMapLatest { list ->
        list.find { it.savedSearch.savedSearch.savedSearchId == savedSearchId }?.posts!!
    }

    fun delete(savedSearch: SavedSearch) = viewModelScope.launch {
        savedSearchRepository.delete(savedSearch)
    }

    fun putScroll(position: Int, scroll: Int) {
        scrollState.value.put(position, scroll)
    }

    fun saveSearch(savedSearch: SavedSearch) {
        viewModelScope.launch {
            savedSearchRepository.update(savedSearch)
        }
    }

    override fun onCleared() {
        savedStateHandle[LAST_SCROLL_POSITIONS] = scrollState.value
        super.onCleared()
    }
}

data class SavedSearchPost(
    val savedSearch: SavedSearchServer,
    val posts: Flow<PagingData<Post>?>,
)

sealed class UiAction {
    data class SetFilters(
        val questionableFilter: Filter,
        val explicitFilter: Filter,
    ) : UiAction()
}

data class UiState(
    val questionableFilter: Filter,
    val explicitFilter: Filter,
)

private const val LAST_SCROLL_POSITIONS: String = "last_scroll_positions"
