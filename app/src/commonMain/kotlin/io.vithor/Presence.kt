package io.vithor

open class Presence(channel: Channel, opts: Any? = TODO()) {
    companion object {
        fun syncState(
            currentState: Any,
            newState: Any,
            onJoin: ((key: String?, currentPresence: Any?, newPresence: Any?) -> Unit)? = TODO(),
            onLeave: ((key: String?, currentPresence: Any?, newPresence: Any?) -> Unit)? = TODO()
        ): Any = TODO()

        fun syncDiff(
            currentState: Any,
            newState: Any,
            onJoin: ((key: String?, currentPresence: Any?, newPresence: Any?) -> Unit)? = TODO(),
            onLeave: ((key: String?, currentPresence: Any?, newPresence: Any?) -> Unit)? = TODO()
        ): Any = TODO()

        fun list(presences: Any, chooser: Function<*>? = TODO()): Any = TODO()
    }
}