package io.vithor

data class Message(
    val topic: String = "",
    val event: String = "",
    val payload: SocketPayload = mutableMapOf(),
    val ref: String? = "",
    val joinRef: String? = null
) {
    var status: String?
        get() = payload["status"] as? String
        set(value) {
            payload["status"] = value as Any
        }
}
