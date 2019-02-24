package io.vithor.kphoenix.facades

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

actual fun decodeURIComponent(value: String?): String? {
    if (value == null) {
        return null
    }

    return try {
        URLDecoder.decode(value, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        value
    }
}

actual fun encodeURIComponent(value: String): String {
    return try {
        URLEncoder.encode(value, "UTF-8")
            .replace("\\+", "%20")
            .replace("\\%21", "!")
            .replace("\\%27", "'")
            .replace("\\%28", "(")
            .replace("\\%29", ")")
            .replace("\\%7E", "~")
    } catch (e: UnsupportedEncodingException) {
        value
    }
}