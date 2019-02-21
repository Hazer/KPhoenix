package io.vithor

typealias SocketPayload = MutableMap<String, Any>

/** Initializes the Socket
 *
 *
 * For IE8 support use an ES5-shim (https://github.com/es-shims/es5-shim)
 *
 * @param {string} endPoint - The string WebSocket endpoint, ie, `"ws://example.com/socket"`,
 *                                               `"wss://example.com"`
 *                                               `"/socket"` (inherited host & protocol)
 * @param {Object} [opts] - Optional configuration
 * @param {string} [opts.transport] - The Websocket Transport, for example WebSocket or Phoenix.LongPoll.
 *
 * Defaults to WebSocket with automatic LongPoll fallback.
 * @param {Function} [opts.encode] - The function to encode outgoing messages.
 *
 * Defaults to JSON:
 *
 * ```javascript
 * (payload, callback) => callback(JSON.stringify(payload))
 * ```
 *
 * @param {Function} [opts.decode] - The function to decode incoming messages.
 *
 * Defaults to JSON:
 *
 * ```javascript
 * (payload, callback) => callback(JSON.parse(payload))
 * ```
 *
 * @param {number} [opts.timeout] - The default timeout in milliseconds to trigger push timeouts.
 *
 * Defaults `DEFAULT_TIMEOUT`
 * @param {number} [opts.heartbeatIntervalMs] - The millisec interval to send a heartbeat message
 * @param {number} [opts.reconnectAfterMs] - The optional function that returns the millsec reconnect interval.
 *
 * Defaults to stepped backoff of:
 *
 * ```javascript
 * function(tries){
 *   return [1000, 5000, 10000][tries - 1] || 10000
 * }
 * ```
 * @param {Function} [opts.logger] - The optional function for specialized logging, ie:
 * ```javascript
 * function(kind, msg, data) {
 *   console.log(`${kind}: ${msg}`, data)
 * }
 * ```
 *
 * @param {number} [opts.longpollerTimeout] - The maximum timeout of a long poll AJAX request.
 *
 * Defaults to 20s (double the server long poll timer).
 *
 * @param {{Object|function)} [opts.params] - The optional params to pass when connecting
 *
 *
 */

open class Socket(url: String, opts: Options) {

    internal var channels: MutableList<Channel> = mutableListOf()

    var timeout: Int = PHOENIX_DEFAULT_TIMEOUT

    /// Set to true once the channel calls .join()
    var joinedOnce: Boolean = false

    var heartbeatIntervalMs: Int = PHOENIX_DEFAULT_HEARTBEAT

    class Options {
        val reconnectAfterMs: RetriesCallback? = null
        val encode: ((msg: Message, callback: (String) -> Unit) -> Unit)? = null
        val decode: DecoderMethod? = null
        val transport: TransportFactory? = null
        val timeout: Int? = null
        val heartbeatIntervalMs: Int? = null
        val logger: ((kind: String, msg: String, data: Any?) -> Unit)? = null
        val longpollerTimeout: Int? = null
        val params: Map<String, String>? = null

    }

    private var reconnectTimer: PhxTimer

    val reconnectAfterMs: (tries: Int) -> Int

    private var defaultEncoder: (msg: Message, callback: (String) -> Unit) -> Unit
    private var defaultDecoder: DecoderMethod

    private var encode: (msg: Message, callback: (String) -> Unit) -> Unit
    private var decode: DecoderMethod

    var logger: ((kind: String, msg: String, data: Any?) -> Unit)? = null

    private var transport: TransportFactory

    private var ref: Int

    private var sendBuffer: MutableList<() -> Unit>

    private var longpollerTimeout: Int

    private var params: Map<String, String>

    private var heartbeatTimer: OSTimer? = null

    private var pendingHeartbeatRef: String?

    private var stateChangeCallbacks = SocketStateChangeCallbacks()

    private val location: Location

    init {
        this.channels = mutableListOf()
        this.sendBuffer = mutableListOf()
        this.ref = 0
        this.timeout = opts.timeout ?: DEFAULT_TIMEOUT
        this.transport = opts.transport ?: WebSocket ?: LongPoll

        this.defaultEncoder = { msg, callback ->
            Serializer.encode(msg, callback)
        }

        this.defaultDecoder = { rawPayload, callback ->
            Serializer.decode(rawPayload, callback)
        }

        if (this.transport != LongPoll) {
            this.encode = opts.encode ?: this.defaultEncoder
            this.decode = opts.decode ?: this.defaultDecoder
        } else {
            this.encode = this.defaultEncoder
            this.decode = this.defaultDecoder
        }

        this.heartbeatIntervalMs = opts.heartbeatIntervalMs ?: 30000

        this.reconnectAfterMs = opts.reconnectAfterMs ?: { tries: Int ->
            listOf(1000, 2000, 5000, 10000).getOrNull(tries - 1) ?: 10000
        }

        this.logger = opts.logger

        this.longpollerTimeout = opts.longpollerTimeout ?: 20000
        this.params = opts.params ?: mutableMapOf()

        this.location = Location("$url/${TRANSPORTS.Websocket.path}")

        this.heartbeatTimer = null
        this.pendingHeartbeatRef = null

        this.reconnectTimer = PhxTimer({
            this.teardown({ this.connect() })
        }, this.reconnectAfterMs)
    }

//    open fun protocol(): String = if (location.protocol.matches("/^https/".toRegex())) "wss" else "ws"

    open fun endPointURL(): String {
        return location.with(this.params, mapOf("vsn" to VSN))
    }

    open fun disconnect(callback: () -> Unit, code: Int?, reason: String?) {
        this.reconnectTimer.reset()
        this.teardown(callback, code, reason)
    }

    private var conn: Transport? = null

    open fun connect(params: Map<String, String>? = null) {
        if (params != null) {
//            console && console.log("passing params to connect is deprecated. Instead pass :params to the Socket constructor")

            this.params = params
        }

        if (this.conn != null) {
            return
        }

        this.conn = this.transport(this.endPointURL())!!.also { conn ->
            conn.timeout = this.longpollerTimeout
            conn.onopen = { this.onConnOpen() }
            conn.onerror = { error -> this.onConnError(error) }
            conn.onmessage = { event -> this.onConnMessage(event) }
            conn.onclose = { event -> this.onConnClose(event) }
        }
    }

    open fun log(kind: String, msg: String, data: Any? = null) {
        this.logger?.invoke(kind, msg, data)
    }

//    open fun onOpen(callback: Function<*>): Unit = TODO()
//    open fun onClose(callback: Function<*>): Unit = TODO()
//    open fun onError(callback: Function<*>): Unit = TODO()
//
//    open fun onMessage(callback: Function<*>) {
//        this.stateChangeCallbacks.message.add(callback)
//    }

    open fun onConnOpen() {
        if (this.hasLogger()) this.log("path", "connected to ${this.endPointURL()}")
        this.flushSendBuffer()
        this.reconnectTimer.reset()
        this.resetHeartbeat()
        this.stateChangeCallbacks.open.forEach { callback -> callback.invoke() }
    }

    internal fun resetHeartbeat() {
        if (this.conn?.skipHeartbeat == true) {
            return
        }

        this.pendingHeartbeatRef = null
        clearInterval(this.heartbeatTimer)
        this.heartbeatTimer = setInterval({ this.sendHeartbeat() }, this.heartbeatIntervalMs)
    }

    internal fun teardown(callback: (() -> Unit)?, code: Int? = null, reason: String? = null) {
        this.conn?.let { conn ->
            conn.onclose = {} // noop

            if (code != null) {
                conn.close(code, reason ?: "")
            } else {
                conn.close()
            }

            // Kill conn
            this.conn = null
        }

        callback?.invoke()
    }

    open fun onConnClose(event: ConnEvent?) {
        if (this.hasLogger()) this.log("path", "close", event)
        this.triggerChanError()
        clearInterval(this.heartbeatTimer)
        if (event != null && event.code != WS_CLOSE_NORMAL) {
            this.reconnectTimer.scheduleTimeout()
        }
        this.stateChangeCallbacks.close.forEach { callback -> callback(event) }
    }


    open fun onConnError(error: ConnEvent?) {
        if (this.hasLogger()) this.log("path", error.toString())
        this.triggerChanError()
        this.stateChangeCallbacks.error.forEach { callback -> callback(error) }
    }

    open fun triggerChanError() = this.channels.forEach { channel ->
        channel.trigger(
            ChannelEvents.Error.event,
            Message(event = ChannelEvents.Error.event)
        )
    }

    open fun connectionState(): String {
        return when (this.conn?.readyState) {
            SocketStates.Connecting -> "connecting"
            SocketStates.Open -> "open"
            SocketStates.Closing -> "closing"
            else -> "closed"
        }
    }

    open fun isConnected(): Boolean = this.connectionState() == "open"

    open fun remove(channel: Channel) {
        this.channels = this.channels.filter { c -> c.joinRef() != channel.joinRef() }.toMutableList()
    }

    open fun channel(topic: String, chanParams: SocketPayload = mutableMapOf()): Channel {
        val chan = Channel(topic, chanParams, this)
        this.channels.add(chan)
        return chan
    }

    open fun push(data: Message) {
        if (this.hasLogger()) {
            val (topic, event, payload, ref, join_ref) = data
            this.log("push", "$topic $event ($join_ref, $ref)", payload)
        }

        if (this.isConnected()) {
            this.encode(data) { result -> this.conn!!.send(result) }
        } else {
            this.sendBuffer.add { this.encode(data) { result -> this.conn!!.send(result) } }
        }
    }

    /**
     * Return the next message ref, accounting for overflows
     * @returns {string}
     */
    open fun makeRef(): String {
        val newRef = this.ref + 1
        if (newRef == this.ref) {
            this.ref = 0
        } else {
            this.ref = newRef
        }

        return this.ref.toString()
    }

    open fun sendHeartbeat() {
        if (this.pendingHeartbeatRef != null) {
            this.pendingHeartbeatRef = null
            if (this.hasLogger()) this.log("path", "heartbeat timeout. Attempting to re-establish connection")
            this.conn!!.close(WS_CLOSE_NORMAL, "hearbeat timeout")
            return
        }

        this.pendingHeartbeatRef = this.makeRef().also { pendingHeartbeatRef ->
            this.push(
                Message(
                    topic = "phoenix",
                    event = "heartbeat",
                    payload = mutableMapOf(),
                    ref = pendingHeartbeatRef
                )
            )
        }
    }

    open fun flushSendBuffer() {
        if (this.isConnected() && this.sendBuffer.isNotEmpty()) {
            this.sendBuffer.forEach { it() }
            this.sendBuffer = mutableListOf()
        }
    }

    open fun onConnMessage(rawMessage: ConnEvent) {
        this.decode(rawMessage.data) { msg ->
            val (topic, event, payload, ref, join_ref) = msg!!

            if (ref != null && ref == this.pendingHeartbeatRef) {
                this.pendingHeartbeatRef = null
            }

            if (this.hasLogger())
                this.log(
                    "receive",
                    "${msg.status ?: ""} ${topic} ${event} ${if (ref != null) "($ref)" else ""}",
                    payload
                )

            for (channel in this.channels) {
                if (!channel.isMember(msg)) {
                    continue
                }
                channel.trigger(event, msg)
            }

            this.stateChangeCallbacks.message.forEach { callback ->
                callback(msg)
            }
        }
    }

    fun hasLogger(): Boolean = this.logger != null
}