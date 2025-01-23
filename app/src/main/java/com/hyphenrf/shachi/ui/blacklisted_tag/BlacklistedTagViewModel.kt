package com.hyphenrf.shachi.ui.blacklisted_tag

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyphenrf.shachi.data.model.BlacklistedTag
import com.hyphenrf.shachi.data.model.BlacklistedTagWithServer
import com.hyphenrf.shachi.data.model.ServerBlacklistedTagCrossRef
import com.hyphenrf.shachi.data.model.ServerView
import com.hyphenrf.shachi.data.repository.blacklist_tag.BlacklistTagRepository
import com.hyphenrf.shachi.data.repository.ServerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BlacklistedTagViewModel(
    private val serverRepository: ServerRepository,
    private val blacklistTagRepository: BlacklistTagRepository,
) :
    ViewModel() {
    val serverList: MutableStateFlow<List<ServerView>?> = MutableStateFlow(null)
    val blacklistedTagFlow: StateFlow<List<BlacklistedTagWithServer>?>

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        blacklistedTagFlow =
            actionStateFlow.filterIsInstance<UiAction.LoadBlacklistedTags>()
                .onStart { emit(UiAction.LoadBlacklistedTags) }
                .flatMapLatest { blacklistTagRepository.getAllFlow() }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = null
                )

        viewModelScope.launch {
            serverRepository.getAllServersFlow().collect {
                Log.d("Blacklist", "$it")
                serverList.value = it
            }
        }
    }

    fun insertBlacklistTag(blacklistedTagWithServer: BlacklistedTagWithServer) =
        viewModelScope.launch {
            val blacklistedTagsId =
                blacklistTagRepository.insertBlacklistedTag(blacklistedTagWithServer.blacklistedTag)
            val crossRef = blacklistedTagWithServer.servers.map {
                ServerBlacklistedTagCrossRef(serverId = it.serverId,
                    blacklistedTagId = blacklistedTagsId.toInt())
            }
            blacklistTagRepository.insertBlacklistedTag(crossRef)
        }

    fun deleteBlacklistTag(blacklistedTag: BlacklistedTag) = viewModelScope.launch {
        blacklistTagRepository.deleteBlacklistedTag(blacklistedTag)
    }
}

sealed class UiAction {
    object LoadBlacklistedTags : UiAction()
    object LoadServers : UiAction()
}