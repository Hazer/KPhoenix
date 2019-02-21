package io.vithor

class SocketStateChangeCallbacks {
    val open = mutableListOf<() -> Unit>()
    val close = mutableListOf<(ConnEvent?) -> Unit>()
    val error = mutableListOf<(ConnEvent?) -> Unit>()
    val message = mutableListOf<MessageCallback>()
}
