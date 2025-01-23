package com.hyphenrf.shachi.data.model.response

import com.hyphenrf.shachi.data.model.Comment
import com.hyphenrf.shachi.data.model.response.danbooru.DanbooruComment
import com.hyphenrf.shachi.data.model.response.moebooru.MoebooruComment

fun GelbooruCommentResponse.mapToComments() = this.comments?.comment?.map {
    Comment(id = it.id,
        postId = it.postId,
        body = it.body,
        creator = it.creator,
        creatorId = it.creatorId,
        createdAt = it.createdAt
    )
}

@JvmName("moebooruMapToComment")
fun List<MoebooruComment>?.mapToComments() = this?.map {
    Comment(id = it.id,
        postId = it.postId,
        body = it.body,
        creator = it.creator,
        creatorId = it.creatorId,
        createdAt = it.createdAt?.toLocalDateTime()
    )
}

@JvmName("danbooruMapToComment")
fun List<DanbooruComment>?.mapToComments() = this?.map {
    Comment(id = it.id,
        postId = it.postId,
        body = it.body,
        creator = it.creator?.name ?: "",
        creatorId = it.creator?.id?: 0,
        createdAt = it.createdAt?.toLocalDateTime()
    )
}