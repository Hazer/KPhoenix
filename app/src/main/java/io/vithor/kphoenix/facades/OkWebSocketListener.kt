package io.vithor.kphoenix.facades

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber

open class OkWebSocketListener : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Timber.d("onOpen($webSocket, $response)")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Timber.d("onFailure($webSocket, $t, $response)")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Timber.d("onClosing($webSocket, $code, $reason)")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Timber.d("onMessage($webSocket, $text)")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Timber.d("onMessage($webSocket, $bytes)")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Timber.d("onClosed($webSocket, $code, $reason)")
    }
}
