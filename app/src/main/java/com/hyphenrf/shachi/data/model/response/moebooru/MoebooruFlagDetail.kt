package com.hyphenrf.shachi.data.model.response.moebooru

import com.hyphenrf.shachi.data.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class MoebooruFlagDetail @OptIn(ExperimentalSerializationApi::class) constructor(
    @JsonNames("post_id") var postId: Int? = null,
    @JsonNames("reason") var reason: String? = null,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("created_at") var createdAt: ZonedDateTime? = null,
    @JsonNames("user_id") var userId: Int? = null,
    @JsonNames("flagged_by") var flaggedBy: String? = null,
)