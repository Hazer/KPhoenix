package io.vithor.kphoenix

//val SOCKET_STATES = { connecting: 0, open: 1, closing: 2, closed: 3 }

const val VSN = "2.0.0"

enum class SocketStates(val code: Int) {
    Created(0),
    Connecting(1),
    Open(2),
    Closing(3),
    Closed(4)
}

const val DEFAULT_TIMEOUT = 10000L
const val WS_CLOSE_NORMAL = 1000L

const val PHOENIX_DEFAULT_TIMEOUT: Long = DEFAULT_TIMEOUT
const val PHOENIX_DEFAULT_HEARTBEAT: Long = 30000

//enum class ChannelStates(val text: String) {
//    Closed("closed"),
//    Errored("errored"),
//    Joined("joined"),
//    Joining("joining"),
//    Leaving("leaving"),
//}

//enum class ChannelEvents(val event: String) {
//    Close("phx_close"),
//    Error("phx_error"),
//    Join("phx_join"),
//    Reply("phx_reply"),
//    Leave("phx_leave")
//}

sealed class ChannelStates(val text: String) {
    object Closed : ChannelStates("closed")
    object Errored : ChannelStates("errored")
    object Joined : ChannelStates("joined")
    object Joining : ChannelStates("joining")
    object Leaving : ChannelStates("leaving")
}

sealed class ChannelEvents(val event: String) {
    object Close : ChannelEvents("phx_close")
    object Error : ChannelEvents("phx_error")
    object Join : ChannelEvents("phx_join")
    object Reply : ChannelEvents("phx_reply")
    object Leave : ChannelEvents("phx_leave")
}

val CHANNEL_LIFECYCLE_EVENTS = listOf(
    ChannelEvents.Close,
    ChannelEvents.Error,
    ChannelEvents.Join,
    ChannelEvents.Reply,
    ChannelEvents.Leave
)

enum class TRANSPORTS(val path: String) {
    Longpoll("longpoll"),
    Websocket("websocket")
}

typealias Milliseconds = Number

typealias RetriesCallback = ((tries: Int) -> Long)

typealias MessageCallback = (message: Message?) -> Unit


