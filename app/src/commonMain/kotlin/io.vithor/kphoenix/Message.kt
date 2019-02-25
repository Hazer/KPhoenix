@file:Suppress("NOTHING_TO_INLINE")
package io.vithor.kphoenix

data class Message(
    val topic: String = "",
    val event: String = "",
    val payload: SocketPayload = mutableMapOf(),
    val ref: String? = "",
    val joinRef: String? = null
) {
    inline var status: String?
        get() = payload["status"] as? String
        set(value) {
            payload["status"] = value as Any
        }

    inline operator fun Message.get(key: String): Any? = payload[key]

    inline operator fun Message.set(key: String, value: Any) {
        payload[key] = value
    }

    operator fun get(key: String): Any? = payload[key]

    constructor(vararg data: Pair<String, Any>) : this(payload = data.toMap().toMutableMap())
}
