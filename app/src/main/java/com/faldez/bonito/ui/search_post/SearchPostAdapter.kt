package com.faldez.bonito.ui.search_post

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faldez.bonito.databinding.PostCardItemBinding
import com.faldez.bonito.model.Post

class SearchPostAdapter(private val onClick: (List<Post?>, Int) -> Unit) :
    PagingDataAdapter<Post, SearchPostViewHolder>(POST_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPostViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = PostCardItemBinding.inflate(inflater, parent, false)
        return SearchPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchPostViewHolder, position: Int) {
        val post = getItem(position)
        Log.d("Adapter", post.toString())
        post?.let {
            val imageView = holder.binding.imageView
            Glide.with(imageView.context).load(it.previewUrl)
                .placeholder(BitmapDrawable(imageView.resources,
                    Bitmap.createBitmap(it.previewWidth!!,
                        it.previewHeight!!,
                        Bitmap.Config.ARGB_8888))).into(imageView)

            holder.binding.root.setOnClickListener { _ ->
                onClick(snapshot().toList(), position)
            }
        }
    }

    companion object {
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.postId == newItem.postId && oldItem.serverUrl == newItem.serverUrl


            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem

        }
    }
}

class SearchPostViewHolder(val binding: PostCardItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

}
