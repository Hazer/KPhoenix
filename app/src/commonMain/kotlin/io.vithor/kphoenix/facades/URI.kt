package io.vithor.kphoenix.facades


/**
 * Encodes the passed String as UTF-8 using an algorithm that's compatible
 * with JavaScript's `encodeURIComponent` function. Returns
 * `null` if the String is `null`.
 *
 * @param value The String to be encoded
 * @return the encoded String
 */
expect fun encodeURIComponent(value: String): String

/**
 * Decodes the passed UTF-8 String using an algorithm that's compatible with
 * JavaScript's `decodeURIComponent` function. Returns
 * `null` if the String is `null`.
 *
 * @param value The UTF-8 encoded String to be decoded
 * @return the decoded String
 */
expect fun decodeURIComponent(value: String?): String?

fun serialize(obj: Map<String, Any?>?, parentKey: String? = null): String {
    if (obj == null) {
        return ""
    }

    val queryStr = mutableListOf<String>()
    for (key in obj.keys) {
        if (!obj.containsKey(key)) {
            continue
        }

        val paramKey = if (parentKey.isNullOrBlank()) key else "$parentKey[$key]"
        val paramVal = obj[key] ?: continue

        when (paramVal) {
            is Collection<*> ->
                queryStr += paramVal.map { encodeURIComponent(paramKey) + "=" + encodeURIComponent(it.toString()) }

            is Map<*, *> ->
                queryStr += serialize(paramVal.mapKeys { it.toString() }, paramKey)

            else -> queryStr += encodeURIComponent(paramKey) + "=" + encodeURIComponent(paramVal.toString())
        }
    }
    return queryStr.joinToString("&")
}

fun appendParams(url: String, params: Map<String, Any>): String {
    if (params.isEmpty()) {
        return url
    }

    val prefix = if (url.contains('?')) "&" else "?"
    return "$url$prefix${serialize(params)}"
}