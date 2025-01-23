package com.hyphenrf.shachi.ui.saved

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hyphenrf.shachi.data.repository.favorite.FavoriteRepository
import com.hyphenrf.shachi.data.repository.post.PostRepository
import com.hyphenrf.shachi.data.repository.saved_search.SavedSearchRepository

class SavedViewModelFactory(
    private val savedSearchRepository: SavedSearchRepository,
    private val postRepository: PostRepository,
    private val favoriteRepository: FavoriteRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(SavedViewModel::class.java)) {
            SavedViewModel(this.savedSearchRepository,
                postRepository,
                favoriteRepository,
                handle) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }
}