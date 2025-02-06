package com.hyphenrf.shachi.data.model.response.gelbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("comment")
data class GelbooruComment(
    val id: Int,
    val body: String,
    val creator: String,
    @SerialName("created_at")
    val createdAt: TimeStamp,
)
