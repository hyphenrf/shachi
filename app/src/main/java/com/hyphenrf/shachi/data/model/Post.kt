package com.hyphenrf.shachi.data.model

import android.os.Parcelable
import androidx.room.*
import com.hyphenrf.shachi.data.preference.Quality
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
enum class Rating {
    General,
    Safe,
    Questionable,
    Explicit
}

@Serializable
@Parcelize
@Entity(tableName = "favorite",
    primaryKeys = ["server_id", "post_id"],
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        parentColumns = ["server_id"],
        childColumns = ["server_id"],
        entity = Server::class)])
data class Post(
    val height: Int,
    val width: Int,
    val score: Int?,
    @ColumnInfo(name = "file_url") val fileUrl: String,
    @ColumnInfo(name = "parent_id") val parentId: Int?,
    @ColumnInfo(name = "sample_url") val sampleUrl: String?,
    @ColumnInfo(name = "sample_width") val sampleWidth: Int?,
    @ColumnInfo(name = "sample_height") val sampleHeight: Int?,
    @ColumnInfo(name = "preview_url") val previewUrl: String?,
    @ColumnInfo(name = "preview_width") val previewWidth: Int?,
    @ColumnInfo(name = "preview_height") val previewHeight: Int?,
    val rating: Rating,
    val tags: String,
    @ColumnInfo(name = "post_id") val postId: Int,
    @ColumnInfo(name = "server_id") val serverId: Int,
    val change: Int,
    val md5: String,
    @ColumnInfo(name = "creator_id") val creatorId: Int?,
    @ColumnInfo(name = "has_children") val hasChildren: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    val status: String,
    val source: String,
    @ColumnInfo(name = "has_notes") val hasNotes: Boolean,
    @ColumnInfo(name = "has_comments") val hasComments: Boolean,
    @ColumnInfo(name = "post_url") val postUrl: String,
    @ColumnInfo(name = "date_added",
        defaultValue = "CURRENT_TIMESTAMP") val dateAdded: Long? = null,
) : Parcelable {
    @Ignore
    var favorite: Boolean = false

    @Ignore
    var quality: Quality? = null
}