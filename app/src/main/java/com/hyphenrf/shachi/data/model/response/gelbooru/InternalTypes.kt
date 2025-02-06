package com.hyphenrf.shachi.data.model.response.gelbooru

import com.hyphenrf.shachi.data.util.serializer.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal typealias SafeInt = @Serializable(SafeIntNullSerializer::class) Int?
internal typealias NonEmptyString = @Serializable(NonEmptyStringSerializer::class) String?
internal typealias TimeStamp = @Serializable(LocalDateTimeSerializer::class) LocalDateTime?
internal typealias TimeStampWithZone = @Serializable(ZonedDateTimeSerializer::class) ZonedDateTime?
