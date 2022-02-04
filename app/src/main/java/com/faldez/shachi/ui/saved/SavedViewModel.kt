package com.faldez.shachi.ui.saved

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SavedViewModel(
    private val savedSearchRepository: SavedSearchRepository,
    private val postRepository: PostRepository,
) : ViewModel() {
    val savedSearches: Flow<List<SavedSearchServer>?>
    val state: MutableStateFlow<Map<SavedSearchServer, List<Post>?>> = MutableStateFlow(mapOf())

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()


        savedSearches =
            actionStateFlow.filterIsInstance<UiAction.GetSavedSearch>().distinctUntilChanged()
                .onStart { emit(UiAction.GetSavedSearch) }
                .flatMapLatest {
                    Log.d("SavedViewModel", "$it")
                    savedSearchRepository.getAll()
                }
                .shareIn(scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    replay = 1)

        viewModelScope.launch {
            savedSearches.collect { list ->
                list?.forEach { savedSearch ->
                    val posts =
                        postRepository.getSavedSearchPosts(Action.SearchSavedSearchPost(savedSearch))
                    state.getAndUpdate { state ->
                        state.toMutableMap().let { map ->
                            map[posts.first] =
                                posts.second
                            return@getAndUpdate map.toMap()
                        }
                    }
                    Log.d("SavedViewModel", "${state.value}")
                }
            }
        }
    }

    fun refreshAll() = viewModelScope.launch {
        state.getAndUpdate { state ->
            state.toMutableMap().let { map ->
                map.map { (savedSearch, _) ->
                    val posts =
                        postRepository.getSavedSearchPosts(Action.SearchSavedSearchPost(savedSearch))
                    map[posts.first] = posts.second
                }
                return@getAndUpdate map.toMap()
            }
        }
    }

    fun clearPosts() = viewModelScope.launch {
        state.getAndUpdate { state ->
            state.toMutableMap().let { map ->
                map.onEach { (savedSearch, _) ->
                    map[savedSearch] = null
                }
            }
        }
    }

    fun delete(savedSearch: SavedSearch) = viewModelScope.launch {
        savedSearchRepository.delete(savedSearch)
    }
}

data class SavedSearchPost(
    val savedSearch: SavedSearchServer,
    val posts: List<Post>?,
)

data class UiState(
    val posts: Map<SavedSearch, List<Post>?>? = null,
)

sealed class UiAction {
    object GetSavedSearch : UiAction()
    data class GetSavedSearchPost(val savedSearch: SavedSearch) : UiAction()
}