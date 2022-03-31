package com.faldez.shachi.data.model.response

import com.faldez.shachi.data.util.serializer.NumberAsBooleanTypeSerializer
import com.faldez.shachi.data.util.serializer.SingleObjectAsArraySerializer
import kotlinx.serialization.Serializable

@Serializable
data class GelbooruTagResponse(
    val tags: GelbooruTags? = null,
)

@Serializable
data class GelbooruTags(
    @Serializable(with = GelbooruTagSerializer::class)
    val tag: List<GelbooruTag>? = null,
    val type: String,
    val count: Int? = null,
    val offset: Int? = null,
    val limit: Int? = null,
)

@Serializable
data class GelbooruTag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    @Serializable(NumberAsBooleanTypeSerializer::class)
    val ambiguous: Boolean,
)

object GelbooruTagSerializer : SingleObjectAsArraySerializer<GelbooruTag>(GelbooruTag.serializer())