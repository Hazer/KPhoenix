package io.vithor.facades

import com.neovisionaries.ws.client.*
import timber.log.Timber

open class NVWebSocketAdapter : WebSocketListener {
    @Throws(Exception::class)
    override fun onStateChanged(websocket: WebSocket, newState: WebSocketState) {
        Timber.d("onStateChanged")
    }


    @Throws(Exception::class)
    override fun onConnected(websocket: WebSocket, headers: Map<String, List<String>>) {
        Timber.d("onConnected")
    }


    @Throws(Exception::class)
    override fun onConnectError(websocket: WebSocket, exception: WebSocketException) {
        Timber.e(exception, "onConnectError")
    }


    @Throws(Exception::class)
    override fun onDisconnected(
        websocket: WebSocket,
        serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame,
        closedByServer: Boolean
    ) {
        Timber.d("onDisconnected closedByServer? $closedByServer\n server: $serverCloseFrame\nclient:$clientCloseFrame")
    }


    @Throws(Exception::class)
    override fun onFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onFrame")
    }


    @Throws(Exception::class)
    override fun onContinuationFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onContinuationFrame")
    }


    @Throws(Exception::class)
    override fun onTextFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onTextFrame")
    }


    @Throws(Exception::class)
    override fun onBinaryFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onBinaryFrame")
    }


    @Throws(Exception::class)
    override fun onCloseFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onCloseFrame")
    }


    @Throws(Exception::class)
    override fun onPingFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onPingFrame")
    }


    @Throws(Exception::class)
    override fun onPongFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onPongFrame")
    }


    @Throws(Exception::class)
    override fun onTextMessage(websocket: WebSocket, text: String) {
        Timber.d("onTextMessage")
    }


    @Throws(Exception::class)
    override fun onTextMessage(websocket: WebSocket, data: kotlin.ByteArray) {
        Timber.d("onTextMessage")
    }


    @Throws(Exception::class)
    override fun onBinaryMessage(websocket: WebSocket, binary: kotlin.ByteArray) {
        Timber.d("onBinaryMessage")
    }


    @Throws(Exception::class)
    override fun onSendingFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onSendingFrame")
    }


    @Throws(Exception::class)
    override fun onFrameSent(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onFrameSent")
    }


    @Throws(Exception::class)
    override fun onFrameUnsent(websocket: WebSocket, frame: WebSocketFrame) {
        Timber.d("onFrameUnsent")
    }


    @Throws(Exception::class)
    override fun onError(websocket: WebSocket, cause: WebSocketException) {
        Timber.e(cause, "onError")
    }


    @Throws(Exception::class)
    override fun onFrameError(
        websocket: WebSocket,
        cause: WebSocketException,
        frame: WebSocketFrame
    ) {
        Timber.e(cause, "onFrameError frame $frame")
    }


    @Throws(Exception::class)
    override fun onMessageError(
        websocket: WebSocket,
        cause: WebSocketException,
        frames: List<WebSocketFrame>
    ) {
        Timber.e(cause, "onMessageError frames $frames")
    }


    @Throws(Exception::class)
    override fun onMessageDecompressionError(
        websocket: WebSocket,
        cause: WebSocketException,
        compressed: kotlin.ByteArray
    ) {
        Timber.e(cause, "onMessageDecompressionError")
    }


    @Throws(Exception::class)
    override fun onTextMessageError(
        websocket: WebSocket,
        cause: WebSocketException,
        data: kotlin.ByteArray
    ) {
        Timber.e(cause, "onTextMessageError")
    }


    @Throws(Exception::class)
    override fun onSendError(
        websocket: WebSocket,
        cause: WebSocketException,
        frame: WebSocketFrame
    ) {
        Timber.e(cause, "onSendError")
    }


    @Throws(Exception::class)
    override fun onUnexpectedError(websocket: WebSocket, cause: WebSocketException) {
        Timber.e(cause, "onUnexpectedError")
    }


    @Throws(Exception::class)
    override fun handleCallbackError(websocket: WebSocket, cause: Throwable) {
        Timber.e(cause, "handleCallbackError")
    }


    @Throws(Exception::class)
    override fun onSendingHandshake(
        websocket: WebSocket,
        requestLine: String,
        headers: List<Array<String>>
    ) {
        Timber.d("onSendingHandshake: $requestLine")
    }


    @Throws(Exception::class)
    override fun onThreadCreated(
        websocket: WebSocket,
        threadType: ThreadType,
        thread: Thread
    ) {
        Timber.d("onThreadCreated: $thread")
    }


    @Throws(Exception::class)
    override fun onThreadStarted(
        websocket: WebSocket,
        threadType: ThreadType,
        thread: Thread
    ) {
        Timber.d("onThreadStarted: $thread")
    }


    @Throws(Exception::class)
    override fun onThreadStopping(
        websocket: WebSocket,
        threadType: ThreadType,
        thread: Thread
    ) {
        Timber.d("onThreadStopping: $thread")
    }
}