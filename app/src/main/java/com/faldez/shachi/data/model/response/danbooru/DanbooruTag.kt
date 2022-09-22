package com.faldez.shachi.data.model.response.danbooru

import com.faldez.shachi.data.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class DanbooruTag @OptIn(ExperimentalSerializationApi::class) constructor(
    val id: Int = 0,
    val name: String = "",
    @JsonNames("post_count") val postCount: Int = 0,
    val category: Int = 0,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime?,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("updated_at") val updatedAt: ZonedDateTime?,
    @JsonNames("is_locked") val isLocked: Boolean = false,
)



