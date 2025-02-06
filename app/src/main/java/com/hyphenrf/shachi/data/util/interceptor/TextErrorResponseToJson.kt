package com.hyphenrf.shachi.data.util.interceptor

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

object TextErrorResponseToJson : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        // XXX: can we ever hit a null case? says the body will be null if it's returned from
        //      one of: `priorResponse`, `networkResponse`, `cacheResponse`
        val body = response.body!!

        // assuming null content-type as application/octet-stream
        if (body.contentType()?.subtype !in listOf("html", "plain", null))
            return response

        val error = """{"error":${Json.encodeToString(body.string())}}"""
        return response.newBuilder()
            .body(error.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
