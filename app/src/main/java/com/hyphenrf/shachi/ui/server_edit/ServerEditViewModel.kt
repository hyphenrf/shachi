package com.hyphenrf.shachi.ui.server_edit

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import com.hyphenrf.shachi.data.api.Action
import com.hyphenrf.shachi.data.model.Server
import com.hyphenrf.shachi.data.model.ServerType
import com.hyphenrf.shachi.data.repository.ServerRepository
import com.hyphenrf.shachi.data.repository.post.PostRepository
import com.hyphenrf.shachi.data.repository.tag.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ServerEditViewModel(
    initialServer: Server?,
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {
    private val isNew = initialServer == null
    val server: MutableStateFlow<Server?> = MutableStateFlow(initialServer)
    val state: MutableStateFlow<State> = MutableStateFlow(State.Idle)

    fun setTitle(title: String) {
        val value = server.value
        server.value = value?.copy(title = title)
            ?: Server(serverId = 0, title = title, type = ServerType.Gelbooru, url = "",
                username = null,
                password = null)
    }

    fun setUrl(url: String) {
        val value = server.value
        server.value = value?.copy(url = url) ?: Server(serverId = 0,
            title = "",
            type = ServerType.Gelbooru,
            url = url,
            username = null,
            password = null)
    }

    fun setType(type: ServerType) {
        val value = server.value
        server.value =
            value?.copy(type = type) ?: Server(serverId = 0, title = "", type = type, url = "",
                username = null,
                password = null)
    }

    fun setUsername(username: String) {
        val value = server.value
        server.value =
            value?.copy(username = username) ?: Server(serverId = 0,
                title = "",
                type = ServerType.Gelbooru,
                url = "",
                username = username,
                password = null)
    }

    fun setPassword(password: String) {
        val value = server.value
        server.value =
            value?.copy(password = password) ?: Server(serverId = 0,
                title = "",
                type = ServerType.Gelbooru,
                url = "",
                username = null,
                password = password)
    }

    fun validate(): Error? {
        val value = server.value ?: return Error("server is null")

        if (value.title.isEmpty()) {
            return Error("title can not be empty")
        }

        if (value.url.isEmpty()) {
            return Error("title can not be empty")
        }

        return null
    }

    fun test(server: Server) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("ServerEditViewModel", "Insert")
            try {
                postRepository.testSearchPost(Action.SearchPost(server.toServerView(), ""))
                if (isNew) {
                    val serverId = serverRepository.insert(server)
                    serverRepository.getServerById(serverId.toInt())?.let {
                        val tags =
                            tagRepository.getTagsSummary(Action.GetTagsSummary(it.toServer()))
                        if (tags != null) {
                            tagRepository.insertTags(tags)
                        }
                    }
                    if (serverRepository.getAllServers()?.size == 1) {
                        serverRepository.setSelectedServer(serverId.toInt())
                    }
                } else {
                    serverRepository.update(server)
                }
                state.value = State.Success
            } catch (e: Error) {
                Log.d("ServerEditViewModel/test", "$e")
                state.value = State.Failed("$e")
            } catch (e: SQLiteConstraintException) {
                Log.d("ServerEditViewModel/test", "$e")
                state.value = State.Failed("Server with same url exists")
            }
        }
    }
}

sealed class State {
    object Idle : State()
    object Success : State()
    data class Failed(val reason: String) : State()
}