package io.vithor.kphoenix.facades

import kotlin.reflect.KClass

interface JSON {
    fun <T> stringify(data: T): String
    fun <T: Any> parse(rawPayload: String, type: KClass<T>): T?
}

inline fun <reified T: Any> JSON.parse(rawPayload: String): T? {
    return parse(rawPayload, T::class)
}