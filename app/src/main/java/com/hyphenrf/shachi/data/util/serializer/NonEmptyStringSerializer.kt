package com.hyphenrf.shachi.data.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NonEmptyStringSerializer : KSerializer<String?> {
    override fun deserialize(decoder: Decoder) = decoder.decodeString().ifEmpty { null }
    override fun serialize(encoder: Encoder, value: String?) = encoder.encodeString(value ?: "")
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NonEmptyStringSerializer", PrimitiveKind.STRING)
}
