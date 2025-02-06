package com.hyphenrf.shachi.data.util.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/* [PROBLEM]
 * Gelbooru V1 api incorrectly escapes usernames with quotes using \" instead of &quot; (verified)
 *
 * [PLAN]
 * There are a couple of ways to fix this. I could try to learn how XSLT works. I could use a
 * streaming XML parser if xmlutil offers a way to do that. I could try and recover from the XML
 * parsing error the default parser throws if it allows that.
 *
 * However, I opted for the least intrusive and dumbest way for now. An interceptor and a regex on
 * the raw text. If this gives me too much headache with edge cases, I'll investigate something more
 * robust.
 *
 * In short: Match the problematic attribute and replace its surrounding quotes with single quotes.
 * This seems reasonable now but I can only imagine how horrible it'll be like 2 years down the line...
 *
 * [RATIONALE] (with some assumptions!)
 * This only(?) happens for usernames and no other user-submitted content.
 * - I tried a couple of tags and comments. tags are escaped, comment bodies too.
 * - I also tried signing up with a username containing a ' or a space and it failed. However, it
 *   succeeded with a username containing " and improperly escaped it with \" in API responses. I'm
 *   therefore ASSUMING NO USERS EXIST WITH A SINGLE QUOTE OR A SPACE IN THEIR USERNAME.
 * Funny values like creator="/>" should parse just fine with a compliant XML parser.
 */
object MalformedCreatorValueEscape : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body!!

        if (body.contentType()?.subtype != "xml")
            return response

        val rexml = attrRegex.replace(body.string()) { matches ->
            " creator='${matches.groups["username"]!!.value}' "
        }

        return response.newBuilder()
            .body(rexml.toResponseBody("application/xml".toMediaType()))
            .build()
    }

    private val attrRegex = Regex(attrPattern)
}

/* XXX: BRITTLE REGEX THAT DEPENDS ON ATTRIBUTE NOT BEING THE LAST IN THE ELEMENT (NO SPACE AFTER)
 *      BECAUSE I CAN'T BE BOTHERED TO DO THIS PROPERLY FOR NOW !!!!!
 *
 * XXX: The preceding space is optional (ensures we don't match weird_attr_creator="...\"..."), but
 *      the trailing space is mandatory in this pattern, this is for the least-any capture groups to
 *      work properly.
 *      Note that [ /] is LESS CORRECT than just a space because this user is unfortunately valid:
 *      'I\"mAHater\"/>' (single quotes not part of the username).
 *
 * PS: I HATE YOU LOZER T. USER
 */
private const val attrPattern = """ creator="(?<username>\S*?\\"\S*?)" """