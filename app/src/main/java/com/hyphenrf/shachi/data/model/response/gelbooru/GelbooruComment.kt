package com.hyphenrf.shachi.data.model.response

import com.hyphenrf.shachi.data.util.serializer.EmptyStringAsIntNullTypeSerializer
import com.hyphenrf.shachi.data.util.serializer.LocalDateTimeSerializer
import com.hyphenrf.shachi.data.util.serializer.SingleObjectAsArraySerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@Serializable
data class GelbooruCommentResponse(
    val comments: GelbooruComments? = null,
    // val error: String? = null, // TODO: is it a good idea to propagate API response/error to user?
)

@Serializable
data class GelbooruComments(
    @Serializable(with = GelbooruCommentSerializer::class)
    val comment: List<GelbooruComment>? = null,
)

@Serializable
data class GelbooruComment @OptIn(ExperimentalSerializationApi::class) constructor(
    val id: Int,
    @JsonNames("post_id") val postId: Int,
    val body: String,
    val creator: String,
    @Serializable(EmptyStringAsIntNullTypeSerializer::class) @JsonNames("creator_id") val creatorId: Int?,
    @Serializable(LocalDateTimeSerializer::class) @JsonNames("created_at") val createdAt: LocalDateTime?,
)

object GelbooruCommentSerializer :
    SingleObjectAsArraySerializer<GelbooruComment>(GelbooruComment.serializer())