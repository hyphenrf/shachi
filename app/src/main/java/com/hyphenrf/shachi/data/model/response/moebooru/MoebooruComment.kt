package com.hyphenrf.shachi.data.model.response.moebooru

import com.hyphenrf.shachi.data.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class MoebooruComment @OptIn(ExperimentalSerializationApi::class) constructor(
    val id: Int = 0,
    @JsonNames("post_id") val postId: Int = 0,
    val body: String = "",
    val creator: String = "",
    @JsonNames("creator_id") val creatorId: Int = 0,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime? = null,
)