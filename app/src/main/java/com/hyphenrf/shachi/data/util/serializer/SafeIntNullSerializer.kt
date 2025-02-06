package com.hyphenrf.shachi.data.util.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SafeIntNullSerializer : KSerializer<Int?> {
    override fun deserialize(decoder: Decoder): Int? = try {
        decoder.decodeInt()
    } catch (_: SerializationException) {
        null
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Int?) {
        if (value != null) {
            encoder.encodeNotNullMark()
            encoder.encodeInt(value)
        } else {
            encoder.encodeNull()
        }
    }

    override val descriptor =
        PrimitiveSerialDescriptor("SafeIntNullSerializer", PrimitiveKind.INT)
}
