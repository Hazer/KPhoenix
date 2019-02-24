package io.vithor.kphoenix.facades

import io.vithor.kphoenix.SocketStates

interface Transport {
    val transportPath: String

    val skipHeartbeat: Boolean

    fun close(code: Long, reason: String)
    fun close()

    fun send(rawPayload: String)
    var timeout: Long
    var onopen: () -> Unit
    val readyState: SocketStates
    var onclose: (ConnClose?) -> Unit
    var onerror: (ConnEvent) -> Unit
    var onmessage: (ConnEvent) -> Unit
}
