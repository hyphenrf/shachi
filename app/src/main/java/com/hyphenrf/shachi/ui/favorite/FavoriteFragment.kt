package com.hyphenrf.shachi.ui.favorite

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hyphenrf.shachi.R
import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.preference.*
import com.hyphenrf.shachi.data.repository.favorite.FavoriteRepositoryImpl
import com.hyphenrf.shachi.databinding.FavoriteFragmentBinding
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {
    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    private lateinit var binding: FavoriteFragmentBinding
    private val viewModel: FavoriteViewModel by navGraphViewModels(R.id.favorite) {
        val db = AppDatabase.build(requireContext())
        FavoriteViewModelFactory(FavoriteRepositoryImpl(db), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FavoriteFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        val questionableFilter =
            preferences.getString(ShachiPreference.KEY_FILTER_QUESTIONABLE_CONTENT, null)
                ?.toFilter() ?: Filter.Disable
        val explicitFilter =
            preferences.getString(ShachiPreference.KEY_FILTER_EXPLICIT_CONTENT, null)?.toFilter()
                ?: Filter.Disable

        val tags = arguments?.get("tags") as String? ?: ""
        if (tags.isNotEmpty()) {
            binding.favoriteTopappbar.subtitle = tags
        }
        viewModel.accept(UiAction.SearchFavorite(tags, questionableFilter, explicitFilter))

        binding.bind()
    }

    private fun prepareAppBar() {
        binding.favoriteAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        binding.favoriteTopappbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.search_button -> {
                    navigateToSearch()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToSearch() {
        val bundle = bundleOf("tags" to viewModel.state.value.tags)
        findNavController().navigate(R.id.action_browse_to_search, bundle)
    }

    private fun FavoriteFragmentBinding.bind(
    ) {
        val gridCount = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> preferences.getString(ShachiPreference.KEY_GRID_COLUMN_LANDSCAPE,
                null)?.toInt() ?: 5
            else -> preferences.getString(ShachiPreference.KEY_GRID_COLUMN_PORTRAIT, null)?.toInt()
                ?: 3
        }
        val gridMode = preferences.getString(ShachiPreference.KEY_GRID_MODE, null)?.toGridMode()
            ?: GridMode.Staggered
        val quality = preferences.getString(ShachiPreference.KEY_PREVIEW_QUALITY, null)?.toQuality()
            ?: Quality.Preview

        val favoriteAdapter = FavoriteAdapter(
            gridMode,
            quality,
            hideQuestionable = viewModel.state.value.questionableFilter == Filter.Hide,
            hideExplicit = viewModel.state.value.explicitFilter == Filter.Hide,
            onClick = { position ->
                val bundle = bundleOf("position" to position)
                findNavController().navigate(R.id.action_favorite_to_favoritepostslide, bundle)
            }
        )

        favoriteAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        favoriteRecyclerView.adapter = favoriteAdapter

        favoriteRecyclerView.layoutManager = if (gridMode == GridMode.Staggered) {
            val layoutManager =
                StaggeredGridLayoutManager(gridCount, StaggeredGridLayoutManager.VERTICAL)
            layoutManager.gapStrategy =
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            layoutManager
        } else {
            GridLayoutManager(requireContext(), gridCount)
        }

        favoriteRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) {
                    Log.d("BrowseFragment", "getPosition ${favoriteAdapter.getPosition()}")
                    viewModel.accept(UiAction.Scroll(currentTags = viewModel.state.value.tags))
                }
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pagingDataFlow.collect {
                    favoriteAdapter.submitData(it)
                }
            }
        }
    }
}