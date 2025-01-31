package com.hyphenrf.shachi.data.util.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.jsonjava.XML


// This interceptor is only ever used for XML API responses, non-xml responses are errors, and
// should not silently pass through.
class XmlToJsonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        var body = response.body
        val converted = if (body?.contentType()?.subtype == "xml") {
            val json = XML.toJSONObject(body.string())
            Log.d("TransformInterceptor", json.toString())
            json.toString()
        } else {
            val str = body?.string() ?: "No Response"
            Log.e("TransformInterceptor", "NOT XML: $str")
            """{"error": "$str"}"""
        }
        body = ResponseBody.create("application/json".toMediaTypeOrNull(), converted)
        val builder = response.newBuilder()
        return builder.header("Content-Type", "application/json").headers(response.headers)
            .body(body).build()
    }

}