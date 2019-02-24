package io.vithor.facades

import com.google.gson.Gson
import kotlin.reflect.KClass

actual object DefaultJson : JSON {
    val gson = Gson()

    override fun <T> stringify(data: T): String {
        return gson.toJson(data)
    }

    override fun <T : Any> parse(rawPayload: String, type: KClass<T>): T? {
        return gson.fromJson<T>(rawPayload, type.java)
    }
}