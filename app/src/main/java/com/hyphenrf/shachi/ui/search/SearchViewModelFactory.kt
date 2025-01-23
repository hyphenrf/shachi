package com.hyphenrf.shachi.ui.search

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hyphenrf.shachi.data.model.ServerView
import com.hyphenrf.shachi.data.repository.search_history.SearchHistoryRepository
import com.hyphenrf.shachi.data.repository.tag.TagRepository

class SearchViewModelFactory constructor(
    private val server: ServerView?,
    private val tagRepository: TagRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(SearchSimpleViewModel::class.java)) {
            SearchSimpleViewModel(server, tagRepository, searchHistoryRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}