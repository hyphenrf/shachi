package com.hyphenrf.shachi.data.util.serializer

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.intOrNull

object SafeIntIdSerializer : JsonTransformingSerializer<Int>(Int.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        require(element is JsonPrimitive)
        return JsonPrimitive(element.intOrNull ?: -1)
    }
}
