package io.vithor.kphoenix

import io.vithor.kphoenix.facades.DefaultJson
import io.vithor.kphoenix.facades.JSON
import io.vithor.kphoenix.facades.parse

typealias DecoderMethod = (rawPayload: String, callback: (message: Message?) -> Unit) -> Unit

abstract class Serializer(private val jsonFacade: JSON) {
    fun encode(msg: Message, callback: (String) -> Unit) {
        return callback(jsonFacade.stringify(msg))
    }

    fun decode(rawPayload: String, callback: (message: Message?) -> Unit) {
        return callback(jsonFacade.parse(rawPayload))
    }

    companion object : Serializer(DefaultJson)
}