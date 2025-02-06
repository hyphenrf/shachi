package com.hyphenrf.shachi.data.api

import com.hyphenrf.shachi.data.model.response.gelbooru.GelbooruComment
import com.hyphenrf.shachi.data.model.response.gelbooru.GelbooruPost
import com.hyphenrf.shachi.data.model.response.gelbooru.GelbooruTag
import com.hyphenrf.shachi.data.util.interceptor.EmptyBodyToJsonArray
import com.hyphenrf.shachi.data.util.interceptor.MalformedCreatorValueEscape
import com.hyphenrf.shachi.data.util.interceptor.TextErrorResponseToJson
import com.hyphenrf.shachi.data.util.serializer.GelList
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * For API quirks, see `doc/gelbooru-api-quirks.md` under this module directory.
 * Read before attempting any changes here or in the underlying implementations
 */
interface GelbooruApi {
    @GET
    suspend fun getPosts(@Url url: String): GelList<GelbooruPost>

    @GET
    suspend fun getTags(@Url url: String): GelList<GelbooruTag>

    @GET
    suspend fun getComments(@Url url: String): GelList<GelbooruComment>

    companion object {
        const val STARTING_PAGE_INDEX = 0

        private val retrofitApi: GelbooruApi by lazy {
            // Interceptor order matters, last added runs first
            val client = OkHttpClient().newBuilder()
                    .addInterceptor(TextErrorResponseToJson) // ?/html ?/plain (200) errors
                    .addInterceptor(MalformedCreatorValueEscape) // ?/xml comment/creator escaping
                    .addInterceptor(EmptyBodyToJsonArray) // empty body (200) => []
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build()

            Retrofit.Builder().client(client)
                .baseUrl("https://safebooru.org")
                .addConverterFactory(DynamicConverterFactory).build()
                .create(GelbooruApi::class.java)
        }

        fun getInstance(): GelbooruApi {
            return retrofitApi
        }
    }
}

/**
 * NOTE: all request bodies will not convert, but that's fine because we never `POST`.
 *   If this changes, we must fix a single type (preferably json?) for our requests and override
 *   [requestBodyConverter] here
 */
private object DynamicConverterFactory : Converter.Factory() {

    private val json: Json by lazy {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
    private val xml: XML by lazy {
        XML {
            defaultPolicy { ignoreUnknownChildren() }
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> = Converter { body ->
        val mediaType = body.contentType()?.subtype ?: "<empty type>"
        // you could use .contains instead or split on '+' then do the checks in the when arms, this
        // is if you find a server responding with `type/subtype+json` etc..
        val serializer = when (mediaType) {
            "json" -> json
            "xml" -> xml
            else -> throw IOException("Got a $mediaType body when we only support [*/xml, */json].")
        }
        val strategy = serializer.serializersModule.serializer(type)
        serializer.decodeFromString(GelList.serializer(strategy), body.string())
    }
}
