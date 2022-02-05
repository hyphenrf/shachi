package com.faldez.shachi.ui.favorite

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.model.Post

class FavoriteAdapter(private val onClick: (Int) -> Unit) :
    PagingDataAdapter<Post, BrowseItemViewHolder>(POST_COMPARATOR) {
    private lateinit var binding: BrowseItemViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        binding = BrowseItemViewHolder(PostCardItemBinding.inflate(inflater, parent, false))
        return binding
    }

    fun getPosition(): Int {
        return binding.bindingAdapterPosition
    }

    override fun onBindViewHolder(holder: BrowseItemViewHolder, position: Int) {
        val post = getItem(position)

        post?.let {
            val imageView = holder.binding.imageView
            var glide = Glide.with(imageView.context).load(it.previewUrl)
            glide = if (post.previewWidth != null && post.previewHeight != null) {
                glide.placeholder(BitmapDrawable(imageView.resources,
                    Bitmap.createBitmap(post.previewWidth,
                        post.previewHeight,
                        Bitmap.Config.ARGB_8888))).override(post.previewWidth, post.previewHeight)
            } else {
                glide.fitCenter()
            }
            glide.into(imageView)

            holder.binding.root.setOnClickListener { _ ->
                onClick(position)
            }
        }
    }

    companion object {
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.postId == newItem.postId && oldItem.serverId == newItem.serverId


            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem

        }
    }
}

class BrowseItemViewHolder(val binding: PostCardItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

}
