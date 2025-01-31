package com.hyphenrf.shachi.data.util.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.jsonjava.JSONException
import org.json.jsonjava.JSONStringer
import org.json.jsonjava.XML

private const val TAG = "TransformInterceptor"

// This interceptor is only ever used for XML API responses, non-xml responses are errors, and
// should not silently pass through.
class XmlToJsonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        // XXX: can we ever hit a null case? says the body will be null if it's returned from
        //      one of: `priorResponse`, `networkResponse`, `cacheResponse`
        val body = response.body!!
        val contents = body.string() // consume the body and close the connection
        var json: String

        if (body.contentType()?.subtype == "xml") try {
            json = XML.toJSONObject(contents).toString()
            Log.d(TAG, json)
        } catch (e: JSONException) {
            Log.e(TAG, "!!! MALFORMED XML !!! -- ${JSONStringer.valueToString(contents)}", e)
            // TODO: find out if there are more malformed XML instances than '\" inside attr value'
            // TODO: be more efficient in the way you recover, don't repeat parsing work
            json = XML.toJSONObject(contents.replace("\\\"", "&quot;")).toString()
        } else {
            json = """{"error":{"type":"NoXML","detail":${JSONStringer.valueToString(contents)}}}"""
            Log.e(TAG, "!!! NOT XML !!! -- $json")
        }

        return response.newBuilder()
            .headers(response.headers)
            .header("Content-Type", "application/json")
            .body(json.toResponseBody(contentType = "application/json".toMediaType()))
            .build()
    }

}