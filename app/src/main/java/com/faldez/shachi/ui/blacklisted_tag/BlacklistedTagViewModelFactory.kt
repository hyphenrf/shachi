package com.faldez.shachi.ui.blacklisted_tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.faldez.shachi.repository.BlacklistTagRepository
import com.faldez.shachi.repository.ServerRepository

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