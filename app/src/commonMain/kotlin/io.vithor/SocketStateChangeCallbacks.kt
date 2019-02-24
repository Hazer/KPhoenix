package io.vithor

import io.vithor.facades.ConnClose
import io.vithor.facades.ConnEvent

class SocketStateChangeCallbacks {
    val open = mutableListOf<() -> Unit>()
    val close = mutableListOf<(ConnClose?) -> Unit>()
    val error = mutableListOf<(ConnEvent?) -> Unit>()
    val message = mutableListOf<MessageCallback>()
}
