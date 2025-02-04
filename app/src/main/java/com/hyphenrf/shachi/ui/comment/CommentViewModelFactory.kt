package com.hyphenrf.shachi.ui.comment

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hyphenrf.shachi.data.repository.comment.CommentRepository
import com.hyphenrf.shachi.data.repository.ServerRepository

class CommentViewModelFactory(
    private val serverRepository: ServerRepository,
    private val commentRepository: CommentRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
            return CommentViewModel(serverRepository, commentRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }
}