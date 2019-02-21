package io.vithor

data class Location(
    val host: String,
    val protocol: String,
    val path: String,
    val port: Int?
) {

    private val portString: String
        get() = if (port == null || port == 80) "" else ":$port"

    val endPoint: String
        get() = "$host$portString/$path"

    fun with(vararg extraParams: Map<String, String>): String {

        val uri = extraParams.fold(this.endPoint) { url, params ->
            Ajax.appendParams(url, params)
        }

        if (uri[0] != '/') {
            return uri
        }
        if (uri[1] == '/') {
            return "${this.protocol}:${uri}"
        }

        return "${this.protocol}://${uri}"
    }

    constructor(url: String) : this(

    )
}
