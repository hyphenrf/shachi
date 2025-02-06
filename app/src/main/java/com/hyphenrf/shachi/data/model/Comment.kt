package com.hyphenrf.shachi.data.model

import java.time.LocalDateTime

data class Comment(
    val id: Int,
    val body: String,
    val creator: String,
    val createdAt: LocalDateTime?,
)
