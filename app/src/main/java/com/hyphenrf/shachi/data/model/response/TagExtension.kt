package com.hyphenrf.shachi.data.model.response

import com.hyphenrf.shachi.data.model.Category
import com.hyphenrf.shachi.data.model.Tag
import com.hyphenrf.shachi.data.model.TagDetail
import com.hyphenrf.shachi.data.model.response.danbooru.DanbooruTag
import com.hyphenrf.shachi.data.model.response.gelbooru.GelbooruTag
import com.hyphenrf.shachi.data.model.response.moebooru.MoebooruTag

@JvmName("gelbooruMapToTagDetails")
fun List<GelbooruTag>.mapToTagDetails() =
    this.map {
        TagDetail(
            name = it.name,
            count = it.count,
            type = parseTag(it.type)
        )
    }

@JvmName("gelbooruMapToTags")
fun List<GelbooruTag>.mapToTags(serverId: Int) =
    this.map {
        Tag(
            name = it.name,
            type = parseTag(it.type),
            serverId = serverId
        )
    }

@JvmName("gelbooruMapToTag")
fun List<GelbooruTag>.mapToTag(serverId: Int) =
    this.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.type),
            serverId = serverId
        )
    }

@JvmName("danbooruMapToTagDetails")
fun List<DanbooruTag>.mapToTagDetails() =
    this.map {
        TagDetail(
            name = it.name,
            count = it.postCount,
            type = parseTag(it.category)
        )
    }

@JvmName("danbooruMapToTags")
fun List<DanbooruTag>.mapToTags(serverId: Int) =
    this.map {
        Tag(
            name = it.name,
            type = parseTag(it.category),
            serverId = serverId
        )
    }

@JvmName("danbooruMapToTag")
fun List<DanbooruTag>.mapToTag(serverId: Int) =
    this.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.category),
            serverId = serverId
        )
    }

@JvmName("moebooruMapToTagDetails")
fun List<MoebooruTag>.mapToTagDetails() =
    this.map {
        TagDetail(
            name = it.name,
            count = it.count,
            type = parseTag(it.type)
        )
    }

@JvmName("moebooruMapToTag")
fun List<MoebooruTag>.mapToTag(serverId: Int) =
    this.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.type),
            serverId = serverId
        )
    }

fun parseTag(type: Int) = when (type) {
    0 -> Category.General
    1 -> Category.Artist
    3 -> Category.Copyright
    4 -> Category.Character
    5 -> Category.Metadata
    6 -> Category.Deprecated
    else -> throw IllegalAccessException("$type is not exist")
}
