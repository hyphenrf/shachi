package com.faldez.bonito.ui.server_edit

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.model.Server
import java.lang.IllegalArgumentException

class ServerEditViewModelFactory constructor(
    private val server: Server?,
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(ServerEditViewModel::class.java)) {
            ServerEditViewModel(server, this.postRepository, this.serverRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}