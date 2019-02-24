package io.vithor

import io.vithor.facades.DefaultJson
import io.vithor.facades.JSON
import io.vithor.facades.parse

typealias DecoderMethod = (rawPayload: String, callback: (message: Message?) -> Unit) -> Unit

abstract class Serializer(private val jsonFacade: JSON) {
    fun encode(msg: Message, callback: (String) -> Unit) {
//        val payload = listOf(
//            msg.joinRef, msg.ref, msg.topic, msg.event, msg.payload
//        )
        return callback(jsonFacade.stringify(msg))
    }

    fun decode(rawPayload: String, callback: (message: Message?) -> Unit) {
        return callback(jsonFacade.parse(rawPayload))
    }

    companion object : Serializer(DefaultJson)
}