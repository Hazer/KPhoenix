package io.vithor.kphoenix.facades

import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import com.neovisionaries.ws.client.WebSocketState
import io.vithor.kphoenix.SocketStates
import io.vithor.kphoenix.WebSocket


class NVWebSocket(endpoint: String, override var timeout: Long) : WebSocket(endpoint) {

    private val factory = WebSocketFactory()

    private val ws = factory.createSocket(endpoint, timeout.toInt())

    init {
        ws.addListener(object : NVWebSocketAdapter() {
            override fun onConnected(
                websocket: com.neovisionaries.ws.client.WebSocket,
                headers: Map<String, List<String>>
            ) {
                super.onConnected(websocket, headers)
                onopen.invoke()
            }

            override fun onDisconnected(
                websocket: com.neovisionaries.ws.client.WebSocket,
                serverCloseFrame: WebSocketFrame,
                clientCloseFrame: WebSocketFrame,
                closedByServer: Boolean
            ) {
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
                onclose.invoke(object : ConnClose {
                    override val code: Long
                        get() = if (closedByServer) serverCloseFrame.closeCode.toLong() else clientCloseFrame.closeCode.toLong()
                    override val data: String
                        get() = if (closedByServer) serverCloseFrame.payloadText else clientCloseFrame.payloadText

                })
            }

            override fun onTextMessage(websocket: com.neovisionaries.ws.client.WebSocket, text: String) {
                super.onTextMessage(websocket, text)
                onmessage.invoke(object : ConnEvent {
                    override val code: Long
                        get() = 0L
                    override val data: String
                        get() = text

                })
            }
        })

        ws.connectAsynchronously()
    }


    //
    //=================================================
    //

    override var onopen: () -> Unit = {}
    override var onclose: (ConnClose?) -> Unit = {}
    override var onerror: (ConnEvent) -> Unit = {}
    override var onmessage: (ConnEvent) -> Unit = {}

    override fun close(code: Long, reason: String) {
        ws.disconnect(code.toInt(), reason)
    }

    override fun close() {
        ws.disconnect()
    }

    override fun send(rawPayload: String) {
        ws.sendText(rawPayload)
    }

    override val readyState: SocketStates
        get() = when (ws.state) {
            WebSocketState.CREATED -> SocketStates.Created
            WebSocketState.CONNECTING -> SocketStates.Connecting
            WebSocketState.OPEN -> SocketStates.Open
            WebSocketState.CLOSING -> SocketStates.Closing
            WebSocketState.CLOSED -> SocketStates.Closed
            else -> SocketStates.Created
        }

    companion object Factory : TransportFactory {
        override val path: String = Companion.path
        override fun invoke(endPoint: String, longpollerTimeout: Long): NVWebSocket =
            NVWebSocket(endPoint, longpollerTimeout)
    }
}

