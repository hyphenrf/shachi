package com.hyphenrf.shachi.data.util

import android.webkit.MimeTypeMap

class MimeUtil {
    companion object {
        fun getMimeTypeFromUrl(url: String): String? {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }
}