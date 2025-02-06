package com.hyphenrf.shachi.data.model.response.gelbooru

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("post")
data class GelbooruPost @OptIn(ExperimentalSerializationApi::class) constructor(
    val height: Int = 0,
    val width: Int = 0,
    val score: SafeInt = null,
    val title: String? = null,
    @SerialName("file_url")
    val fileUrl: String = "",
    @SerialName("parent_id")
    val parentId: SafeInt = null,
    @SerialName("sample_url")
    val sampleUrl: NonEmptyString = null,
    @SerialName("sample_width")
    val sampleWidth: Int? = null,
    @SerialName("sample_height")
    val sampleHeight: Int? = null,
    @SerialName("preview_url")
    val previewUrl: NonEmptyString = null,
    @SerialName("preview_width")
    val previewWidth: Int? = null,
    @SerialName("preview_height")
    val previewHeight: Int? = null,
    val rating: String = "",
    val tags: String = "",
    val id: SafeInt,
    val change: Int = 0,
    @JsonNames("hash", /* md5 also attempted */)
    val md5: String = "",
    @SerialName("creator_id")
    val creatorId: Int? = null,
    @SerialName("has_children")
    val hasChildren: Boolean = false,
    @SerialName("created_at")
    val createdAt: TimeStampWithZone = null,
    val status: String = "",
    val source: String = "",
    @SerialName("has_notes")
    val hasNotes: Boolean = false,
    @SerialName("has_comments")
    val hasComments: Boolean = false,
)
