@file:Suppress("NOTHING_TO_INLINE")

package io.vithor.kphoenix.facades

import com.google.gson.*
import io.vithor.kphoenix.Message
import java.lang.reflect.Type

class MessageV2Deserializer : JsonDeserializer<Message?>, JsonSerializer<Message> {

    override fun serialize(msg: Message, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val payload = listOf(
            msg.joinRef, msg.ref, msg.topic, msg.event, msg.payload
        )
        return context.serialize(payload)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Message? {
        when {
            json.isJsonArray -> {
                val (join_ref, ref, topic, event, payload) = json.asJsonArray
                return Message(
                    topic = topic,
                    event = event,
                    ref = ref,
                    joinRef = join_ref,
                    payload = context.deserialize(payload)
                )
            }
            json.isJsonObject -> {
                val (join_ref, ref, topic, event, payload) = json.asJsonObject
                return Message(
                    topic = topic,
                    event = event,
                    ref = ref,
                    joinRef = join_ref,
                    payload = context.deserialize(payload)
                )
            }
            json.isJsonNull -> return null
            else -> throw IllegalStateException("Failed deserializing message: $json")
        }

    }

    private inline fun <reified T> JsonDeserializationContext.deserialize(json: JsonElement): T {
        return deserialize(json, T::class.java)
    }

}


private inline operator fun JsonObject.component5(): JsonElement =
    getAsJsonObject("payload")

private inline operator fun JsonObject.component4(): String =
    getAsJsonPrimitive("event").asStringOrNull ?: ""

private inline operator fun JsonObject.component3(): String =
    getAsJsonPrimitive("topic").asStringOrNull ?: ""

private inline operator fun JsonObject.component2(): String? =
    getAsJsonPrimitive("ref").asStringOrNull ?: ""

private inline operator fun JsonObject.component1(): String? =
    getAsJsonPrimitive("join_ref").asStringOrNull

/**
 * join_ref
 */
private inline operator fun JsonArray.component1(): String? = this[0]?.asStringOrNull

/**
 * ref
 */
private inline operator fun JsonArray.component2(): String = this[1]?.asStringOrNull ?: ""

/**
 * topic
 */
private inline operator fun JsonArray.component3(): String = this[2]?.asStringOrNull ?: ""

/**
 * event
 */
private inline operator fun JsonArray.component4(): String = this[3]?.asStringOrNull ?: ""

/**
 * payload
 */
private inline operator fun JsonArray.component5(): JsonObject = this[4]!!.asJsonObject

private inline val JsonElement.asStringOrNull: String?
    get() {
        if (isJsonNull) return null
        return asString
    }
