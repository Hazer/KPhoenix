package io.vithor.kphoenix

import io.vithor.kphoenix.facades.Transport
import io.vithor.kphoenix.facades.TransportFactory

abstract class WebSocket(val endPoint: String) : Transport {
    override val transportPath: String
        get() = path

    override val skipHeartbeat: Boolean = false

    companion object : TransportFactory {
        override val path: String
            get() =  TRANSPORTS.Websocket.path

        override operator fun invoke(endPoint: String, longpollerTimeout: Long): WebSocket? {
            return TRANSPORT_BAG.find { transportClass ->
                transportClass.supertypes.any { it.classifier == WebSocket::class }
            }?.constructors?.firstOrNull()?.call(endPoint) as? WebSocket
        }
    }
}