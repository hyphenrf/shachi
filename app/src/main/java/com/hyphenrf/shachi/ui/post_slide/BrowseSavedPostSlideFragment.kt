package com.hyphenrf.shachi.ui.post_slide

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.navGraphViewModels
import androidx.paging.filter
import com.hyphenrf.shachi.R
import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.model.Post
import com.hyphenrf.shachi.data.model.Rating
import com.hyphenrf.shachi.data.repository.favorite.FavoriteRepositoryImpl
import com.hyphenrf.shachi.data.repository.post.PostRepositoryImpl
import com.hyphenrf.shachi.data.repository.saved_search.SavedSearchRepositoryImpl
import com.hyphenrf.shachi.data.repository.search_history.SearchHistoryRepositoryImpl
import com.hyphenrf.shachi.data.repository.server.ServerRepositoryImpl
import com.hyphenrf.shachi.data.api.BooruApiImpl
import com.hyphenrf.shachi.ui.browse.BrowseViewModel
import com.hyphenrf.shachi.ui.browse.BrowseViewModelFactory
import com.hyphenrf.shachi.ui.post_slide.base.BasePostSlideFragment

class BrowseSavedPostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "PostSlideFragment"
    }

    private val viewModel: BrowseViewModel by navGraphViewModels(R.id.saved) {
        val db = AppDatabase.build(requireContext())
        BrowseViewModelFactory(
            PostRepositoryImpl(BooruApiImpl()),
            ServerRepositoryImpl(db),
            FavoriteRepositoryImpl(db),
            SavedSearchRepositoryImpl(db),
            SearchHistoryRepositoryImpl(db),
            this)
    }

    override suspend fun collectPagingData(showQuestionable: Boolean, showExplicit: Boolean) {
        viewModel.pagingDataFlow.collect {
            postSlideAdapter.submitData(it.filter { post ->
                when (post.rating) {
                    Rating.Questionable -> showQuestionable
                    Rating.Explicit -> showExplicit
                    Rating.Safe -> true
                    Rating.General -> true
                }
            })
        }
    }

    override fun navigateToPostDetailBundle(post: Post?): Bundle {
        return bundleOf("post" to post,
            "server" to viewModel.state.value.server,
            "tags" to viewModel.state.value.tags)
    }

    override fun deleteFavoritePost(post: Post) {
        viewModel.deleteFavoritePost(post)
    }

    override fun favoritePost(post: Post) {
        viewModel.favoritePost(post)
    }
}