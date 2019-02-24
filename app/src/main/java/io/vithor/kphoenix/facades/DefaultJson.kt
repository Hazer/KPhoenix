package io.vithor.kphoenix.facades

import com.google.gson.*
import io.vithor.kphoenix.Message
import kotlin.reflect.KClass

actual object DefaultJson : JSON {
    val gson = GsonBuilder()
        .serializeNulls()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Message::class.java, MessageV2Deserializer())
        .create()

    override fun <T> stringify(data: T): String {
        return gson.toJson(data)
    }

    override fun <T : Any> parse(rawPayload: String, type: KClass<T>): T? {
        return gson.fromJson<T>(rawPayload, type.java)
    }
}