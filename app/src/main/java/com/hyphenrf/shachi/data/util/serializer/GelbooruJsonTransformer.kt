package com.hyphenrf.shachi.data.util.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * A transparent wrapper around [List] for minimal and un-intrusive serialization behavior override.
 */
@Serializable(GelbooruJsonTransformer::class)
@JvmInline
value class GelList<T>(private val contents: List<T>) : List<T> by contents

// {@attributes:{...}, <rootname>:[...]} => [...]
// if we get [...], it is left unchanged. if we get xml, fallback
// alternatively, one could do the opposite and wrap the array response, perhaps because that'll
// perform better, but it's gonna result in a lot more intrusive change in the codebase and an
// asymmetry with dan/moe response types.
@OptIn(ExperimentalSerializationApi::class)
private class GelbooruJsonTransformer<T : Any>(listSerializer: KSerializer<List<T>>) :
    KSerializer<GelList<T>> {

    // XXX: there has to be a better way...
    private val root = listSerializer
        .descriptor.getElementDescriptor(0)
        .annotations.filterIsInstance<XmlSerialName>()
        .first()
        .value

    private fun transformDeserialize(element: JsonElement): JsonElement = when (element) {
        // V2: get the root object from the main object
        is JsonObject -> element[root] ?: JsonArray(listOf())
        is JsonArray -> element
        is JsonPrimitive ->
            throw IllegalArgumentException("Got a primitive for Gelbooru JSON response")
    }

    private val serializer = ListSerializerAsGelList(listSerializer)

    override fun deserialize(decoder: Decoder): GelList<T> = when (decoder) {
        is JsonDecoder -> {
            val element = decoder.decodeJsonElement()
            decoder.json.decodeFromJsonElement(serializer, transformDeserialize(element))
        }
        else -> serializer.deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: GelList<T>) =
        serializer.serialize(encoder, value)

    override val descriptor: SerialDescriptor = SerialDescriptor(
    "com.hyphenrf.shachi.data.util.serializer.GelbooruJsonTransformer",
        listSerializer.descriptor
    )
}

// Exactly the same as [ListSerializer]. Nothing noteworthy here, just type plumbing.
@OptIn(ExperimentalSerializationApi::class)
private class ListSerializerAsGelList<T>(private val listSerializer: KSerializer<List<T>>) :
    KSerializer<GelList<T>> {

    override fun deserialize(decoder: Decoder): GelList<T> =
        GelList(listSerializer.deserialize(decoder))

    override fun serialize(encoder: Encoder, value: GelList<T>) =
        listSerializer.serialize(encoder, value)

    override val descriptor: SerialDescriptor = SerialDescriptor(
        "com.hyphenrf.shachi.data.util.serializer.GelList", listSerializer.descriptor
    )
}
