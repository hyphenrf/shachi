package com.hyphenrf.shachi.ui.blacklisted_tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hyphenrf.shachi.data.repository.blacklist_tag.BlacklistTagRepository
import com.hyphenrf.shachi.data.repository.ServerRepository

class BlacklistedTagViewModelFactory(
    private val serverRepository: ServerRepository,
    private val blacklistTagRepository: BlacklistTagRepository,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(BlacklistedTagViewModel::class.java)) {
            BlacklistedTagViewModel(serverRepository, blacklistTagRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }
}