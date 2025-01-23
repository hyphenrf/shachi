package com.hyphenrf.shachi.ui.browse

import androidx.navigation.navGraphViewModels
import com.hyphenrf.shachi.R
import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.repository.favorite.FavoriteRepositoryImpl
import com.hyphenrf.shachi.data.repository.post.PostRepositoryImpl
import com.hyphenrf.shachi.data.repository.saved_search.SavedSearchRepositoryImpl
import com.hyphenrf.shachi.data.repository.search_history.SearchHistoryRepositoryImpl
import com.hyphenrf.shachi.data.repository.server.ServerRepositoryImpl
import com.hyphenrf.shachi.data.api.BooruApiImpl
import com.hyphenrf.shachi.ui.browse.base.BaseBrowseFragment

class BrowseFragment : BaseBrowseFragment() {
    override val viewModel: BrowseViewModel by navGraphViewModels(R.id.browse) {
        val db = AppDatabase.build(requireContext())
        BrowseViewModelFactory(
            PostRepositoryImpl(BooruApiImpl()),
            ServerRepositoryImpl(db),
            FavoriteRepositoryImpl(db),
            SavedSearchRepositoryImpl(db),
            SearchHistoryRepositoryImpl(db),
            this)
    }
}