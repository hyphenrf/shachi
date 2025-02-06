package com.hyphenrf.shachi.data.model.response.gelbooru

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("tag")
data class GelbooruTag(
    val name: String = "",
    val count: Int = 0,
    val type: Int = 0,
)
