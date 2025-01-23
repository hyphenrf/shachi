package com.hyphenrf.shachi.ui.post_detail

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hyphenrf.shachi.data.model.Post
import com.hyphenrf.shachi.data.repository.ServerRepository
import com.hyphenrf.shachi.data.repository.tag.TagRepository

class PostDetailViewModelFactory constructor(
    private val post: Post,
    private val initialSearchTags: String,
    private val serverRepository: ServerRepository,
    private val tagRepository: TagRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            PostDetailViewModel(
                post,
                initialSearchTags,
                serverRepository,
                tagRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}