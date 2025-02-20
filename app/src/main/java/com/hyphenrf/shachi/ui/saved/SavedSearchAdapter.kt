package com.hyphenrf.shachi.ui.saved

import android.text.SpannableStringBuilder
import android.util.Log
import android.util.SparseArray
import android.view.*
import androidx.core.util.forEach
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hyphenrf.shachi.data.model.Post
import com.hyphenrf.shachi.data.model.SavedSearchServer
import com.hyphenrf.shachi.data.preference.Filter
import com.hyphenrf.shachi.data.preference.GridMode
import com.hyphenrf.shachi.data.preference.Quality
import com.hyphenrf.shachi.databinding.PostCardItemBinding
import com.hyphenrf.shachi.databinding.SavedSearchItemBinding
import com.hyphenrf.shachi.data.util.MimeUtil
import com.hyphenrf.shachi.data.util.bindPostImagePreview

class SavedSearchAdapter(
    private val scrollPositions: SparseArray<Int>,
    private val listener: SavedSearchAdapterListener,
    private val gridMode: GridMode,
    private val quality: Quality,
    private val questionableFilter: Filter,
    private val explicitFilter: Filter,
) : ListAdapter<SavedSearchPost, SavedSearchAdapter.SavedSearchViewHolder>(COMPARATOR) {
    private val viewPool = RecyclerView.RecycledViewPool()
    val adapters: MutableMap<Int, SavedSearchPostAdapter> = mutableMapOf()

    fun setScrollPositions(newScrollPositions: SparseArray<Int>) {
        newScrollPositions.forEach { key, value ->
            val oldValue = scrollPositions.get(key)
            scrollPositions.put(key, newScrollPositions[key])
            if (oldValue != value) {
                notifyItemChanged(key)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSearchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedSearchItemBinding.inflate(inflater, parent, false)

        binding.savedSearchItemRecyclerView.apply {
            layoutManager = LinearLayoutManager(binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false).apply {
                recycleChildrenOnDetach = true
                initialPrefetchItemCount = 10
            }
            setRecycledViewPool(viewPool)
            setHasFixedSize(true)
        }

        return SavedSearchViewHolder(
            binding,
            listener,
        )
    }

    override fun onBindViewHolder(holder: SavedSearchViewHolder, position: Int) {
        val item = getItem(position)

        val adapter = SavedSearchPostAdapter(
            listener = listener,
            savedSearchServer = item.savedSearch,
            gridMode = gridMode,
            quality = quality,
            questionableFilter = questionableFilter,
            explicitFilter = explicitFilter,
        )

        adapters[position] = adapter
        listener.onBind(adapter, item, questionableFilter, explicitFilter)

        holder.bind(adapter, item, scrollPositions)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<SavedSearchPost>() {
            override fun areItemsTheSame(
                oldItem: SavedSearchPost,
                newItem: SavedSearchPost,
            ): Boolean =
                oldItem.savedSearch.savedSearch.savedSearchId == newItem.savedSearch.savedSearch.savedSearchId

            override fun areContentsTheSame(
                oldItem: SavedSearchPost,
                newItem: SavedSearchPost,
            ): Boolean = oldItem.savedSearch == newItem.savedSearch
        }
    }

    class SavedSearchViewHolder(
        val binding: SavedSearchItemBinding,
        private val listener: SavedSearchAdapterListener,
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(
            adapter: SavedSearchPostAdapter,
            item: SavedSearchPost,
            scrollPositions: SparseArray<Int>,
        ) {
            binding.savedSearchTagsTextView.text = item.savedSearch.savedSearch.savedSearchTitle
            binding.savedSearchServerTextView.text = item.savedSearch.server.title
            binding.tagsTextView.text = SpannableStringBuilder(item.savedSearch.savedSearch.tags)

            binding.savedSearchItemRecyclerView.apply {
                swapAdapter(adapter, true)
            }

            binding.root.setOnClickListener {
                listener.onBrowse(item.savedSearch)
            }

            scrollPositions[item.savedSearch.savedSearch.savedSearchId]?.let { scroll ->
                if (scroll > 0) {
                    binding.savedSearchItemRecyclerView.post {
                        binding.savedSearchItemRecyclerView.layoutManager?.scrollToPosition(
                            scroll)
                    }
                }
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?,
        ) {
            val refresh = menu?.add(Menu.NONE, 1, 1, "Refresh")
            val edit = menu?.add(Menu.NONE, 2, 2, "Edit")
            val delete = menu?.add(Menu.NONE, 3, 3, "Delete")

            refresh?.setOnMenuItemClickListener {
                listener.onRefresh(bindingAdapterPosition)
                true
            }

            edit?.setOnMenuItemClickListener {
                listener.onEdit(bindingAdapterPosition)
                true
            }
            delete?.setOnMenuItemClickListener {
                listener.onDelete(bindingAdapterPosition)
                true
            }
        }
    }
}

class SavedSearchPostAdapter(
    private val listener: SavedSearchAdapterListener,
    private val savedSearchServer: SavedSearchServer? = null,
    private val gridMode: GridMode,
    private val quality: Quality,
    private val questionableFilter: Filter,
    private val explicitFilter: Filter,
) : PagingDataAdapter<Post, SavedSearchPostAdapter.SavedSearchPostViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSearchPostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PostCardItemBinding.inflate(inflater, parent, false)
        return SavedSearchPostViewHolder(
            binding,
            listener,
            gridMode,
            quality,
            hideQuestionable = questionableFilter == Filter.Hide,
            hideExplicit = explicitFilter == Filter.Hide,
        )
    }

    override fun onBindViewHolder(holder: SavedSearchPostViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, savedSearchServer!!)
    }

    override fun onViewRecycled(holder: SavedSearchPostViewHolder) {
        super.onViewRecycled(holder)
        try {
            Glide.with(holder.binding.root).clear(holder.binding.imageView)
        } catch (e: Exception) {
            Log.e("SavedSearchAdapter", "onViewRecycled $e")
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(
                oldItem: Post,
                newItem: Post,
            ): Boolean = oldItem.postId == newItem.postId && oldItem.serverId == newItem.serverId

            override fun areContentsTheSame(
                oldItem: Post,
                newItem: Post,
            ): Boolean = oldItem == newItem
        }
    }

    class SavedSearchPostViewHolder(
        val binding: PostCardItemBinding,
        private val listener: SavedSearchAdapterListener,
        private val gridMode: GridMode,
        private val quality: Quality,
        private val hideQuestionable: Boolean,
        private val hideExplicit: Boolean,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Post?, savedSearchServer: SavedSearchServer) {
            val rootLayoutParams = binding.root.layoutParams
            rootLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            rootLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.root.layoutParams = rootLayoutParams

            val coordinatorLayoutParams = binding.coordinatorLayout.layoutParams
            coordinatorLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            coordinatorLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.coordinatorLayout.layoutParams = coordinatorLayoutParams

            val imageView = binding.imageView
            val imageLayoutParams = imageView.layoutParams
            imageLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            imageLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            imageView.layoutParams = imageLayoutParams

            if (item != null) {
                // setting isGone necessary?? No but it's an optimization (see: b7325a8)
                // TODO:
                //  investigate what exactly this optimizes???
                //  investigate whether spurious setting of those variables is worse for perf
                if (item.favorite) binding.favoriteIcon.isVisible = true
                val (type, subtype) = (MimeUtil.getMimeTypeFromUrl(item.fileUrl) ?: "/").split('/')
                when {
                    type == "video" -> binding.movieTypeIcon.isVisible = true
                    subtype == "gif" -> binding.gifTypeIcon.isVisible = true
                }
                binding.root.setOnClickListener {
                    listener.onClick(savedSearchServer, bindingAdapterPosition)
                }
            }

            bindPostImagePreview(
                binding.root.context,
                imageView,
                item,
                gridMode,
                quality,
                hideQuestionable,
                hideExplicit)
        }
    }
}

interface SavedSearchAdapterListener {
    fun onBrowse(savedSearchServer: SavedSearchServer)
    fun onClick(savedSearchServer: SavedSearchServer, position: Int)
    fun onRefresh(position: Int)
    fun onDelete(position: Int)
    fun onEdit(position: Int)
    fun onScroll(position: Int, scroll: Int)
    fun onBind(
        adapter: SavedSearchPostAdapter,
        item: SavedSearchPost,
        questionableFilter: Filter,
        explicitFilter: Filter,
    )
}