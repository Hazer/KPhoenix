package io.vithor.kphoenix.facades

import io.vithor.kphoenix.SocketStates
import io.vithor.kphoenix.WS_CLOSE_NORMAL
import io.vithor.kphoenix.WebSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit


class OkWebSocket(endpoint: String, override var timeout: Long) : WebSocket(endpoint) {
    enum class State {
        CLOSED, CLOSING, CONNECT_ERROR, RECONNECT_ATTEMPT, RECONNECTING, OPENING, OPEN,

        CREATED
    }

//    private val logging = HttpLoggingInterceptor()
//        .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE)


    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
        .build()

    private var state: State = State.CLOSED


    private var ws: okhttp3.WebSocket

    init {
        state = State.CREATED
        val request = Request.Builder().url(endpoint).build()
        val listener = object : OkWebSocketListener() {
            override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
                state = State.OPEN
                super.onOpen(webSocket, response)
                onopen.invoke()
            }

            override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                state = State.CONNECT_ERROR

                onerror.invoke(object : ConnEvent {
                    override val code: Long
                        get() = response?.code()?.toLong() ?: -1L
                    override val data: String
                        get() = response?.message() ?: t.localizedMessage

                })
            }


            override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                state = State.CLOSING
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                super.onMessage(webSocket, text)
                onmessage.invoke(object : ConnEvent {
                    override val code: Long
                        get() = 0L
                    override val data: String
                        get() = text

                })
            }

//            override fun onMessage(webSocket: okhttp3.WebSocket, bytes: ByteString) {
//                super.onMessage(webSocket, bytes)
//                onmessage.invoke(object : ConnEvent {
//                    override val code: Long
//                        get() = 0L
//                    override val data: String
//                        get() = bytes.string(Charsets.UTF_8)
//
//                })
//            }

            override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                state = State.CLOSED

                onclose.invoke(object : ConnClose {
                    override val code: Long
                        get() = code.toLong()
                    override val data: String
                        get() = reason

                })
            }
        }

        ws = client.newWebSocket(request, listener)
        state = State.OPENING
    }

    //
    //=================================================
    //

    override var onopen: () -> Unit = {}
    override var onclose: (ConnClose?) -> Unit = {}
    override var onerror: (ConnEvent) -> Unit = {}
    override var onmessage: (ConnEvent) -> Unit = {}

    override fun close(code: Long, reason: String) {
        ws.close(code.toInt(), reason)
    }

    override fun close() {
        ws.close(WS_CLOSE_NORMAL.toInt(), null)
    }

    override fun send(rawPayload: String) {
        ws.send(rawPayload)
    }

    override val readyState: SocketStates
        get() = when (state) {
            State.CREATED -> SocketStates.Created
            State.OPENING -> SocketStates.Connecting
            State.OPEN -> SocketStates.Open
            State.CLOSING -> SocketStates.Closing
            State.CLOSED -> SocketStates.Closed
            State.CONNECT_ERROR -> SocketStates.Closed
            else -> SocketStates.Created
        }

    companion object Factory : TransportFactory {
        override val path: String = WebSocket.path
        override fun invoke(endPoint: String, longpollerTimeout: Long): OkWebSocket =
            OkWebSocket(endPoint, longpollerTimeout)
    }
}
