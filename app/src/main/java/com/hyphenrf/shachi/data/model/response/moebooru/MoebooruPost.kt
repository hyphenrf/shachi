package com.hyphenrf.shachi.data.model.response.moebooru

import com.hyphenrf.shachi.data.util.serializer.TimestampDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class MoebooruPost @OptIn(ExperimentalSerializationApi::class) constructor(
    val id: Int = 0,
    val tags: String = "",
    @Serializable(TimestampDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime? = null,
    @Serializable(TimestampDateTimeSerializer::class) @JsonNames("updated_at") val updatedAt: ZonedDateTime? = null,
    @JsonNames("creator_id") val creatorId: Int? = null,
    val author: String = "",
    val change: Int = 0,
    val source: String = "",
    val score: Int = 0,
    val md5: String = "",
    @JsonNames("flag_detail") val flagDetail: MoebooruFlagDetail? = null,
    @JsonNames("file_size") val fileSize: Int = 0,
    @JsonNames("file_url") val fileUrl: String = "",
    @JsonNames("file_ext") val fileExt: String? = null,
    @JsonNames("is_shown_in_index") val isShownInIndex: Boolean = false,
    @JsonNames("preview_url") val previewUrl: String = "",
    @JsonNames("preview_width") val previewWidth: Int = 0,
    @JsonNames("preview_height") val previewHeight: Int = 0,
    @JsonNames("actual_preview_width") val actualPreviewWidth: Int = 0,
    @JsonNames("actual_preview_height") val actualPreviewHeight: Int = 0,
    @JsonNames("sample_url") val sampleUrl: String = "",
    @JsonNames("sample_width") val sampleWidth: Int = 0,
    @JsonNames("sample_height") val sampleHeight: Int = 0,
    @JsonNames("sample_file_size") val sampleFileSize: Int = 0,
    @JsonNames("jpeg_url") val jpegUrl: String = "",
    @JsonNames("jpeg_width") val jpegWidth: Int = 0,
    @JsonNames("jpeg_height") val jpegHeight: Int = 0,
    @JsonNames("jpeg_file_size") val jpegFileSize: Int = 0,
    val rating: String = "",
    @JsonNames("has_children") val hasChildren: Boolean = false,
    @JsonNames("parent_id") val parentId: Int? = null,
    @JsonNames("approver_id") val approverId: Int? = null,
    val status: String = "",
    val width: Int = 0,
    val height: Int = 0,
    @JsonNames("is_held") val isHeld: Boolean = false,
    @JsonNames("frames_pending_string") val framesPendingString: String = "",
    @JsonNames("frames_pending") val framesPending: List<String> = listOf(),
    @JsonNames("frames_string") val framesString: String = "",
    val frames: List<String> = listOf(),
    @JsonNames("is_rating_locked") val isRatingLocked: Boolean? = false,
    @JsonNames("is_note_locked") val isNoteLocked: Boolean? = false,
    @JsonNames("is_pending") val isPending: Boolean? = false,
    @Serializable(TimestampDateTimeSerializer::class) @JsonNames("last_noted_at") val lastNotedAt: ZonedDateTime? = null,
    @Serializable(TimestampDateTimeSerializer::class) @JsonNames("last_commented_at") val lastCommentedAt: ZonedDateTime? = null,
)