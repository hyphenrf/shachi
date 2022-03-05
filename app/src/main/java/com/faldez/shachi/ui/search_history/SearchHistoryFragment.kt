package com.faldez.shachi.ui.search_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.databinding.SearchHistoryBottomSheetDialogFragmentBinding
import com.faldez.shachi.data.repository.SearchHistoryRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchHistoryFragment : BottomSheetDialogFragment() {
    private lateinit var binding: SearchHistoryBottomSheetDialogFragmentBinding
    private val viewModel: SearchHistoryViewModel by viewModels {
        SearchHistoryViewModelFactory(SearchHistoryRepository(AppDatabase.build(requireContext())),
            this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = SearchHistoryBottomSheetDialogFragmentBinding.inflate(inflater,
            container,
            false)

        binding.bind()

        return binding.root
    }

    private fun SearchHistoryBottomSheetDialogFragmentBinding.bind() {
        val adapter = SearchHistoryAdapter(
            onClick = { searchHistoryServer ->
                val bundle = bundleOf("server" to searchHistoryServer.server,
                    "tags" to searchHistoryServer.searchHistory.tags)
                findNavController().navigate(R.id.action_searchhistory_to_browse, bundle)
            },
            onDelete = { searchHistoryServer ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Are you Sure?")
                    .setPositiveButton("Yes"
                    ) { _, _ -> viewModel.delete(searchHistoryServer.searchHistory) }
                    .setNegativeButton("No", null).show()
            }
        )
        searchHistoryRecyclerView.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
        searchHistoryRecyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.state.collectLatest {
                adapter.submitData(it)
            }
        }
    }
}