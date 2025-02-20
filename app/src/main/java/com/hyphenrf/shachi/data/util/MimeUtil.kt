package com.hyphenrf.shachi.data.util

import android.webkit.MimeTypeMap

object MimeUtil {
    private val map = MimeTypeMap.getSingleton()

    fun getMimeTypeFromUrl(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return map.getMimeTypeFromExtension(extension)
    }
}