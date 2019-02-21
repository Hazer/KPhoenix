package io.vithor

interface Transport {
    fun close(code: Int, reason: String)
    fun close()
    fun send(rawPayload: String)

    var timeout: Int
    var onopen: () -> Unit
    val readyState: SocketStates
    var onclose: (ConnEvent?) -> Unit
    val skipHeartbeat: Boolean
    var onerror: (ConnEvent) -> Unit
    var onmessage: (ConnEvent) -> Unit

}
