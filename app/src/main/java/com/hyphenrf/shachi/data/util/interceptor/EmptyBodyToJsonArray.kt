package com.hyphenrf.shachi.data.util.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

object EmptyBodyToJsonArray : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        // body.contentLength sometimes returns -1 if unknown
        // Note: this check won't protect from a body that is whitespace or malformed json
        if (response.peekBody(1).source().use { it.request(1) })
            return response

        return response.newBuilder()
            .body("[]".toResponseBody("application/json".toMediaType()))
            .build()
    }
}
