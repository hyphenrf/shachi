package com.hyphenrf.shachi.data.util.interceptor

import android.util.Log
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

private const val TAG = "TransformInterceptor"

// This interceptor is only ever used for XML API responses, non-xml responses are errors, and
// should not silently pass through.
object XmlToJsonInterceptor : Interceptor {
    private val xml = XmlMapper()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        // XXX: can we ever hit a null case? says the body will be null if it's returned from
        //      one of: `priorResponse`, `networkResponse`, `cacheResponse`
        val contents = response.body!!.string() // consume the body and close the connection
        var json: String

        if (response.body!!.contentType()?.subtype == "xml") try {
            json = xml.readTree(contents).toString()
            Log.d(TAG, json)
        } catch (e: JsonParseException) {
            Log.e(TAG, "!!! MALFORMED XML !!! -- ${Json.encodeToString(contents)}\n$e")
            // TODO: find out if there are more malformed XML instances than '\" inside attr value'
            // TODO: be more efficient in the way you recover, don't repeat parsing work
            json = xml.readTree(contents.replace("\\\"", "&quot;")).toString()
        } else {
            json = """{"error":{"type":"NoXML","detail":${Json.encodeToString(contents)}}}"""
            Log.e(TAG, "!!! NOT XML !!! -- $json")
        }

        return response.newBuilder()
            .headers(response.headers)
            .header("Content-Type", "application/json")
            .body(json.toResponseBody(contentType = "application/json".toMediaType()))
            .build()
    }

}
