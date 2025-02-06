package com.hyphenrf.shachi.data.model.response

import com.hyphenrf.shachi.data.model.Comment
import com.hyphenrf.shachi.data.model.response.danbooru.DanbooruComment
import com.hyphenrf.shachi.data.model.response.gelbooru.GelbooruComment
import com.hyphenrf.shachi.data.model.response.moebooru.MoebooruComment

@JvmName("gelbooruMapToComment")
fun List<GelbooruComment>.mapToComments() = this.map {
    Comment(id = it.id,
        body = it.body,
        creator = it.creator,
        createdAt = it.createdAt
    )
}

@JvmName("moebooruMapToComment")
fun List<MoebooruComment>.mapToComments() = this.map {
    Comment(id = it.id,
        body = it.body,
        creator = it.creator,
        createdAt = it.createdAt?.toLocalDateTime()
    )
}

@JvmName("danbooruMapToComment")
fun List<DanbooruComment>.mapToComments() = this.map {
    Comment(id = it.id,
        body = it.body,
        creator = it.creator?.name ?: "",
        createdAt = it.createdAt?.toLocalDateTime()
    )
}
