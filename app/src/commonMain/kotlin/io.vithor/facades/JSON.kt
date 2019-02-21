package io.vithor

interface JSON {
    fun <T> stringify(data: T): String
    fun <T> parse(rawPayload: String): T?
}
