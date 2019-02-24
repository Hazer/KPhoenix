package io.vithor.kphoenix

import io.vithor.kphoenix.facades.appendParams

data class Location(
    val url: String
//    val host: String,
//    val protocol: String,
//    val path: String,
//    val port: Int?
) {

//    private val portString: String
//        get() = if (port == null || port == 80) "" else ":$port"
//
//    val endPoint: String
//        get() = "$host$portString/$path"

    fun with(vararg extraParams: Map<String, String>): String {
        var newUrl = url
        for (params in extraParams) {
            newUrl = appendParams(newUrl, params)
        }
        return newUrl

//        if (uri[0] != '/') {
//            return uri
//        }
//        if (uri[1] == '/') {
//            return "${this.protocol}:${uri}"
//        }
//
//        return "${this.protocol}://${uri}"
    }
}
