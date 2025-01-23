package com.hyphenrf.shachi.ui.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hyphenrf.shachi.R
import com.hyphenrf.shachi.data.api.BooruApiImpl
import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.model.Modifier
import com.hyphenrf.shachi.data.model.ServerView
import com.hyphenrf.shachi.data.model.TagDetail
import com.hyphenrf.shachi.data.repository.search_history.SearchHistoryRepositoryImpl
import com.hyphenrf.shachi.data.repository.tag.TagRepositoryImpl
import com.hyphenrf.shachi.data.util.*
import com.hyphenrf.shachi.databinding.SearchFragmentBinding
import com.hyphenrf.shachi.databinding.TagsDetailsBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {
    companion object {
        const val TAG = "SearchFragment"
    }

    private lateinit var binding: SearchFragmentBinding
    private lateinit var tagDetailsBinding: TagsDetailsBinding

    private val viewModel: SearchSimpleViewModel by viewModels {
        val server: ServerView? =
            requireArguments().getParcelable("server") as ServerView?
        val db = AppDatabase.build(requireContext())
        SearchViewModelFactory(server, TagRepositoryImpl(BooruApiImpl(),
            db), SearchHistoryRepositoryImpl(db), this)
    }

    private lateinit var searchSuggestionAdapter: SearchSuggestionAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = SearchFragmentBinding.inflate(inflater, container, false)
        tagDetailsBinding = TagsDetailsBinding.bind(binding.root)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        binding.searchTagsInputText.bind()
        binding.suggestionTagsRecyclerView.bind()

        val initialTags: String = requireArguments().get("tags") as String
        val initialPage: Int? = requireArguments().get("page") as Int?
        viewModel.setPage(initialPage)

        bindSelectedTags(initialTags)

        lifecycleScope.launch {
            viewModel.state.collect { state ->

            }
        }

        binding.manualSearchChip.setOnClickListener {
            val tags = viewModel.state.value.selectedTags
            viewModel.setMode(viewModel.state.value.selectedTags is SelectedTags.Simple)
            if (tags is SelectedTags.Manual) {
                binding.searchTagsInputText.text = null
            } else if (tags is SelectedTags.Simple) {
                binding.searchTagsInputText.text =
                    SpannableStringBuilder(tags.tags.joinToString(" "))
            }
        }

        binding.pageChip.setOnClickListener {
            binding.pageChip.isChecked = viewModel.state.value.page != null
            MaterialAlertDialogBuilder(requireContext()).setTitle("Start at page")
                .setView(R.layout.page_input_layout).setPositiveButton("Ok") { dialog, _ ->
                    (dialog as Dialog).findViewById<TextInputEditText>(R.id.pageInputText).text?.toString()
                        ?.toIntOrNull()?.let {
                            viewModel.setPage(it)
                        }
                }.setNegativeButton("Clear") { _, _ ->
                    viewModel.setPage(null)
                }.show()
        }

        binding.pageChip.setOnCheckedChangeListener { chip, checked ->
            (chip as Chip).closeIcon = if (checked) {
                null
            } else {
                ResourcesCompat.getDrawable(resources,
                    R.drawable.ic_baseline_arrow_drop_down_24,
                    activity?.theme)
            }
        }

        val adapter = SearchHistoryAdapter(
            onClick = { searchHistoryServer ->
                bindSelectedTags(searchHistoryServer.searchHistory.tags)
            },
            onDelete = { searchHistoryServer ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Are you Sure?")
                    .setPositiveButton("Yes"
                    ) { _, _ -> viewModel.deleteSearchHistory(searchHistoryServer.searchHistory) }
                    .setNegativeButton("No", null).show()
            }
        )
        binding.searchHistoryRecyclerView.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
        binding.searchHistoryRecyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.searchHistoriesFlow.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                val tags = state.selectedTags

                if (tags is SelectedTags.Simple) {
                    binding.selectedTagsHeader.isVisible = tags.isNotEmpty()
                    tagDetailsBinding.hideAll()
                    tagDetailsBinding.clearAllGroup()

                    tags.tags.groupBy { it.type }.forEach { (type, tags) ->
                        val (group, header, textColor) = tagDetailsBinding.getGroupHeaderTextColor(
                            type)

                        header.isVisible =
                            tags.any { it.modifier != Modifier.Minus }
                        group.isVisible = tags.isNotEmpty()

                        tags.forEach { tag ->
                            val chip = Chip(requireContext())
                            chip.bind(group, textColor, tag)
                        }
                    }
                    binding.loadingIndicator.isVisible = false
                    if (tags.isNotEmpty()) {
                        binding.searchHistoryLayout.hide()
                        binding.selectedTagsLayout.show()
                        binding.suggestionTagLayout.hide()
                    }
                } else if (tags is SelectedTags.Manual) {
                    binding.manualSearchChip.isChecked = true
                    if (tags.isNotEmpty()) {
                        binding.searchHistoryLayout.hide()
                        binding.selectedTagsLayout.hide()
                        binding.suggestionTagLayout.show()
                    }
                }

                if (!tags.isNotEmpty()) {
                    binding.searchHistoryLayout.show()
                    binding.selectedTagsLayout.hide()
                    binding.suggestionTagLayout.hide()
                }

                binding.pageChip.isChecked = state.page != null
                if (state.page != null) {
                    binding.pageChip.text = "Page: ${state.page}"
                } else {
                    binding.pageChip.text = "Page"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.suggestionTags.collectLatest {
                binding.loadingIndicator.isVisible = false
                it?.let { tags ->
                    searchSuggestionAdapter.submitList(tags)
                }
            }
        }
    }

    private fun bindSelectedTags(initialTags: String) {
        Log.d(TAG, "bindSelectedTags initialTags=$initialTags")
        binding.loadingIndicator.isVisible = !initialTags.isManualSearchTags()
        viewModel.setInitialTags(initialTags)
        if (initialTags.isManualSearchTags()) {
            binding.searchTagsInputText.text = SpannableStringBuilder(initialTags)
        }
    }

    private fun RecyclerView.bind() {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(this.context,
            linearLayoutManager.orientation)
        this.addItemDecoration(dividerItemDecoration)
        searchSuggestionAdapter = SearchSuggestionAdapter(
            setTextColor = {
                ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                    it,
                    requireActivity().theme))

            },
            onClick = {
                if (viewModel.state.value.selectedTags is SelectedTags.Manual) {
                    val text = binding.searchTagsInputText.text?.toString()
                    if (text.isNullOrEmpty()) {
                        binding.searchTagsInputText.text = SpannableStringBuilder(it.name)
                        binding.searchTagsInputText.setSelection(it.name.length)
                    } else {
                        val selectionStart = binding.searchTagsInputText.selectionStart - 1
                        val start =
                            StringUtil.findTokenStart(text, selectionStart)
                        val end = StringUtil.findTokenEnd(text, selectionStart)
                        Log.d("SearchFragment",
                            "selectionStart=$selectionStart text=$text replace start=$start to end=$end with=${it.name}")
                        binding.searchTagsInputText.text?.replace(start, end + 1, "${it.name} ")
                        binding.searchTagsInputText.setSelection(start + it.name.length)
                    }
                } else {
                    viewModel.insertTag(it)
                    binding.searchTagsInputText.text?.clear()
                }
            }
        )
        this.apply {
            adapter = searchSuggestionAdapter
            layoutManager = linearLayoutManager
        }
    }

    private fun prepareAppBar() {
        binding.searchTopAppBar.menu.clear()
        binding.searchTopAppBar.inflateMenu(R.menu.search_menu)
        binding.searchTopAppBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.searchTopAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.searchTopAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clear_button -> {
                    binding.searchTagsInputText.text?.clear()
                    true
                }
                R.id.apply_button -> {
                    applySearch()
                    true
                }
                else -> false
            }
        }
    }

    private fun applySearch() {
        val value = when (val selectedTags = viewModel.state.value.selectedTags) {
            is SelectedTags.Simple -> {
                Log.d("SearchFragment", "Simple browse: ${selectedTags.tags}")
                selectedTags.tags.joinToString(" ") { it.toString() }
            }
            is SelectedTags.Manual -> {
                Log.d("SearchFragment", "Advance browse: ${selectedTags.tags}")
                selectedTags.tags
            }
        }

        val bundle = bundleOf("server" to viewModel.state.value.server,
            "tags" to value, "start" to viewModel.state.value.page)
        findNavController().navigate(R.id.action_search_to_browse, bundle)
    }

    private fun LinearLayoutCompat.show() {
        animate().alpha(1f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationEnd(animation)
                    visibility = View.VISIBLE
                }
            })
    }

    private fun LinearLayoutCompat.hide() {
        animate().alpha(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    visibility = View.GONE
                }
            })
    }

    private fun EditText.bind() {
        val serverTitle = viewModel.state.value.server?.title
        hint = if (serverTitle != null) {
            "Search $serverTitle"
        } else {
            "Search"
        }
        setImeActionLabel("Add", KeyEvent.KEYCODE_ENTER)
        doOnTextChanged { text, start, _, _ ->
            if (viewModel.state.value.selectedTags is SelectedTags.Manual) {
                text?.toString()?.trim()?.let {
                    if (it.isNotEmpty()) {
                        val tag = StringUtil.getCurrentToken(it, start)
                        viewModel.accept(UiAction.SearchTag(tag))
                        viewModel.insertTagByName(it)
                    }
                }
            } else {
                text?.toString()?.trim()?.let {
                    if (it.isNotEmpty()) {
                        viewModel.accept(UiAction.SearchTag(it))
                        binding.searchHistoryLayout.hide()
                        binding.selectedTagsLayout.hide()
                        binding.suggestionTagLayout.show()
                    }
                }
            }
        }
        setOnEditorActionListener { textView, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                /*
                If advanced search is active, action done will apply search
                instead of insert tag into selected tags or apply search if empty
                on simple mode
                 */
                if (viewModel.state.value.selectedTags is SelectedTags.Manual) {
                    applySearch()
                } else {
                    val text = textView.text.toString()
                    if (text.isNotEmpty()) {
                        viewModel.insertTagByName(text)
                        binding.searchTagsInputText.text?.clear()
                    } else if (text.isEmpty() && viewModel.state.value.selectedTags.isNotEmpty()) {
                        applySearch()
                    }
                }
                true
            } else {
                false
            }
        }
        requestFocus()
    }

    private fun Chip.bind(group: ChipGroup, textColor: Int?, tag: TagDetail) {
        text = tag.toString()
        isCloseIconVisible = true
        textColor?.let {
            setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                textColor,
                requireActivity().theme)))
        }
        setOnCloseIconClickListener {
            Log.d("SearchSimpleViewModel", "onClose")
            viewModel.removeTag(tag)
        }
        setOnClickListener {
            viewModel.toggleTag(tag.name)
        }

        if (tag.modifier == Modifier.Minus) {
            tagDetailsBinding.blacklistTagsHeader.isVisible = true
            tagDetailsBinding.blacklistTagsChipGroup.isVisible = true
            tagDetailsBinding.blacklistTagsChipGroup.addView(this)
        } else {
            group.addView(this)
        }
    }
}