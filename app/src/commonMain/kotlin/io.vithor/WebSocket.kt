package io.vithor

open class WebSocket(endPoint: String) : Transport {


    open fun normalizeEndpoint(endPoint: String): String = TODO()
    open fun endpointURL(): String = TODO()
    open fun closeAndRetry(): Unit = TODO()
    open fun ontimeout(): Unit = TODO()
    open fun poll(): Unit = TODO()
    open fun send(body: Any): Unit = TODO()
    open fun close(code: Any? = TODO(), reason: Any? = TODO()): Unit = TODO()

    companion object : TransportFactory {
        override operator fun invoke(endPoint: String): WebSocket? {
            return WebSocket(endPoint)
        }
    }
}