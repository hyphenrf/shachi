package com.hyphenrf.shachi.ui.post_slide

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.navGraphViewModels
import androidx.paging.filter
import androidx.paging.map
import com.hyphenrf.shachi.R
import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.model.Post
import com.hyphenrf.shachi.data.model.Rating
import com.hyphenrf.shachi.data.repository.favorite.FavoriteRepository
import com.hyphenrf.shachi.data.repository.favorite.FavoriteRepositoryImpl
import com.hyphenrf.shachi.ui.favorite.FavoriteViewModel
import com.hyphenrf.shachi.ui.favorite.FavoriteViewModelFactory
import com.hyphenrf.shachi.ui.post_slide.base.BasePostSlideFragment

class FavoritePostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "FavoritePostSlideFragment"
    }

    private val viewModel: FavoriteViewModel by navGraphViewModels(R.id.favorite) {
        val db = AppDatabase.build(requireContext())
        FavoriteViewModelFactory(FavoriteRepositoryImpl(db), this)
    }

    override suspend fun collectPagingData(showQuestionable: Boolean, showExplicit: Boolean) {
        viewModel.pagingDataFlow.collect {
            val data = it.filter { post ->
                when (post.rating) {
                    Rating.Questionable -> showQuestionable
                    Rating.Explicit -> showExplicit
                    Rating.Safe -> true
                    Rating.General -> true
                }
            }.map { post ->
                post.favorite = true
                post
            }
            postSlideAdapter.submitData(data)
        }
    }

    override fun navigateToPostDetailBundle(post: Post?): Bundle {
        return bundleOf("post" to post,
            "tags" to viewModel.state.value.tags)
    }


    override fun deleteFavoritePost(post: Post) {
        viewModel.deleteFavoritePost(post)
    }

    override fun favoritePost(post: Post) {
        viewModel.favoritePost(post)
    }
}