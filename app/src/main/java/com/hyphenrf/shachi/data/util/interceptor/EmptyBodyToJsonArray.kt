package com.hyphenrf.shachi.data.util.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

object EmptyBodyToJsonArray : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body!!

        if (body.contentLength() != 0L) return response

        return response.newBuilder()
            .body("[]".toResponseBody("application/json".toMediaType()))
            .build()
    }
}
