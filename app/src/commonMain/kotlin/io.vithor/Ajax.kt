package io.vithor

object Ajax {
//    open fun request(
//        method: String,
//        endPoint: String,
//        accept: String,
//        body: Any,
//        timeout: Number? = TODO(),
//        ontimeout: Any? = TODO(),
//        callback: ((response: Any?) -> Unit)? = TODO()
//    ): Unit = TODO()
//
//    open fun xdomainRequest(
//        req: Any,
//        method: String,
//        endPoint: String,
//        body: Any,
//        timeout: Number? = TODO(),
//        ontimeout: Any? = TODO(),
//        callback: ((response: Any?) -> Unit)? = TODO()
//    ): Unit = TODO()
//
//    open fun xhrRequest(
//        req: Any,
//        method: String,
//        endPoint: String,
//        accept: String,
//        body: Any,
//        timeout: Number? = TODO(),
//        ontimeout: Any? = TODO(),
//        callback: ((response: Any?) -> Unit)? = TODO()
//    ): Unit = TODO()
//
//    open fun parseJSON(resp: String): JSON = TODO()

//    fun formatQueryParams(params: Map<String, String>): String {
//        return params.entries.map { p -> "${p.key}=${p.value}" }
//            .reduce { p1, p2 -> "$p1&$p2" }
//            .takeIf { it.isNotBlank() }?.let { s -> "?$s" } ?: ""
//    }
//
//    fun formatQueryParams(params: Map<String, Any?>): String {
//        return params.entries.map { p ->
//            val value = when (p.value) {
//                is CharSequence, is Char, is Number, is Boolean -> p.value.toString()
//                is Array<*> -> return@map formatQueryParams(p.key, p.value as Array<*>)
//                is Collection<*> -> return@map formatQueryParams(p.key, p.value as Collection<*>)
//                null -> null
//                else -> TODO()
//            }
//
//            return@map "${p.key}=${value}"
//        }.reduce { p1, p2 -> "$p1&$p2" }
//            .takeIf { it.isNotBlank() }?.let { s -> "?$s" } ?: ""
//    }
//
//    private fun formatQueryParams(key: String, arrayOfAnys: Array<*>): String {
//        return arrayOfAnys.map { value -> "${key}=${value}" }
//            .reduce { p1, p2 -> "$p1&$p2" }
////            .takeIf { it.isNotBlank() }?.let { s -> "?$s" } ?: ""
//    }
//
//    private fun formatQueryParams(key: String, arrayOfAnys: Collection<*>): String {
//        return arrayOfAnys.map { value -> "${key}=${value}" }
//            .reduce { p1, p2 -> "$p1&$p2" }
////            .takeIf { it.isNotBlank() }?.let { s -> "?$s" } ?: ""
//    }
//
////    open fun serialize(obj: Map<String, Any>, parentKey: String): String {
////        return formatQueryParams(obj)
////    }

    open fun serialize(obj: Map<String, Any?>?, parentKey: String? = null): String {
        if (obj == null) {
            return ""
        }

        val queryStr = mutableListOf<String>()
        for (key in obj.keys) {
            if (!obj.containsKey(key)) {
                continue
            }

            val paramKey = if (parentKey.isNullOrBlank()) "$parentKey[$key]" else key
            val paramVal = obj[key] ?: continue

            if (paramVal is Collection<*>) {
                queryStr += paramVal.map { encodeURIComponent(paramKey) + "=" + encodeURIComponent(it.toString()) }
            } else if (paramVal is Map<*, *>) {
                queryStr += this.serialize(paramVal.mapKeys { it.toString() }, paramKey)
            } else {
                queryStr += encodeURIComponent(paramKey) + "=" + encodeURIComponent(paramVal.toString())
            }
        }
        return queryStr.joinToString("&")
    }

    private fun encodeURIComponent(value: String): String {
        return value // TODO: Encode it
    }

    open fun appendParams(url: String, params: Map<String, Any>): String {
        if (params.isEmpty()) {
            return url
        }

        val prefix = if (url.matches("/\\?/".toRegex())) "&" else "?"
        return "${url}${prefix}${this.serialize(params)}"
    }
}