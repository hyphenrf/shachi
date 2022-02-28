package com.faldez.shachi.ui.browse

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.request.RequestOptions
import com.faldez.shachi.R
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.util.MimeUtil
import com.faldez.shachi.util.glide.GlideApp


class BrowseAdapter(
    private val gridMode: String,
    private val quality: String,
    private val hideQuestionable: Boolean,
    private val hideExplicit: Boolean,
    private val onClick: (Int) -> Unit,
) :
    PagingDataAdapter<Post, BrowseItemViewHolder>(POST_COMPARATOR) {
    private lateinit var binding: BrowseItemViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        binding =
            BrowseItemViewHolder(PostCardItemBinding.inflate(inflater, parent, false),
                gridMode,
                quality,
                hideQuestionable,
                hideExplicit,
                onClick)
        return binding
    }

    override fun onBindViewHolder(holder: BrowseItemViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
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

class BrowseItemViewHolder(
    val binding: PostCardItemBinding,
    private val gridMode: String,
    private val quality: String,
    private val hideQuestionable: Boolean,
    private val hideExplicit: Boolean,
    val onClick: (Int) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post?) {
        val imageView = binding.imageView

        if(post != null) {
            val previewWidth = post.previewWidth ?: 150
            val previewHeight = if (gridMode == "staggered") {
                (previewWidth * (post.height.toFloat() / post.width.toFloat())).toInt()
            } else {
                previewWidth
            }
            if (hideQuestionable && post.rating == Rating.Questionable || hideExplicit && post.rating == Rating.Explicit) {
                val drawable = ResourcesCompat.getDrawable(binding.root.resources,
                    R.drawable.nsfw_placeholder,
                    null)
                    ?.toBitmap(previewWidth, previewHeight)

                Glide.with(imageView.context).load(drawable)
                    .into(imageView)
            } else {
                val url = when (quality) {
                    "sample" -> post.sampleUrl ?: post.previewUrl
                    "original" -> post.fileUrl
                    else -> post.previewUrl ?: post.sampleUrl
                } ?: post.fileUrl

                var glide = Glide.with(imageView.context).load(url)
                    .placeholder(BitmapDrawable(imageView.resources,
                        Bitmap.createBitmap(previewWidth,
                            previewHeight,
                            Bitmap.Config.ARGB_8888)))
                    .override(previewWidth, previewHeight)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    glide = glide.apply(RequestOptions().set(Downsampler.ALLOW_HARDWARE_CONFIG, true))
                }
                glide.into(imageView)
            }

            binding.favoriteIcon.isVisible = post.favorite
            binding.movieTypeIcon.isVisible =
                MimeUtil.getMimeTypeFromUrl(post.fileUrl)?.startsWith("video") ?: false
            binding.root.setOnClickListener { _ ->
                onClick(bindingAdapterPosition)
            }
        } else {
            GlideApp.with(imageView.context).load(BitmapDrawable(imageView.resources,
                Bitmap.createBitmap(150,
                    150,
                    Bitmap.Config.ARGB_8888))).into(imageView)
        }
    }
}
