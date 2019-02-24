package io.vithor.kphoenix

import io.vithor.kphoenix.facades.ConnClose
import io.vithor.kphoenix.facades.ConnEvent

class SocketStateChangeCallbacks {
    val open = mutableListOf<() -> Unit>()
    val close = mutableListOf<(ConnClose?) -> Unit>()
    val error = mutableListOf<(ConnEvent?) -> Unit>()
    val message = mutableListOf<MessageCallback>()
}
