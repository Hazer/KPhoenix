package io.vithor

typealias MessageCallback = (message: Message?) -> Unit

open class Channel(val topic: String, val params: SocketPayload?, val socket: Socket) {

    private var joinedOnce: Boolean = false

    private var state: ChannelStates = ChannelStates.Closed

    private var bindings: MutableList<Binding> = mutableListOf()

    private var pushBuffer: MutableList<Push> = mutableListOf()

    private var timeout: Milliseconds = this.socket.timeout

    private var rejoinTimer: PhxTimer

    private var joinPush: Push

    private var bindingRef: Int = 0

    open val canPush: Boolean
        get() = this.socket.isConnected() && this.isJoined()

    init {

        this.joinPush = Push(this, ChannelEvents.Join.event, this.params, this.timeout)
        this.rejoinTimer = PhxTimer({ this.rejoinUntilConnected() }, this.socket.reconnectAfterMs)

        this.joinPush.receive("ok") {
            this.state = ChannelStates.Joined
            this.rejoinTimer.reset()

            this.pushBuffer.forEach { pushEvent -> pushEvent.send() }
            this.pushBuffer = mutableListOf()
        }

        this.onClose {
            this.rejoinTimer.reset()
            if (this.socket.hasLogger()) this.socket.log("channel", "close ${this.topic} ${this.joinRef()}")
            this.state = ChannelStates.Closed
            this.socket.remove(this)
        }

        this.onError { reason ->
            if (this.isLeaving() || this.isClosed()) {
                return@onError
            }
            if (this.socket.hasLogger())
                this.socket.log("channel", "error ${this.topic}", reason)

            this.state = ChannelStates.Errored
            this.rejoinTimer.scheduleTimeout()
        }

        this.joinPush.receive("timeout") {
            if (!this.isJoining()) {
                return@receive
            }
            if (this.socket.hasLogger()) this.socket.log(
                "channel",
                "timeout ${this.topic} (${this.joinRef()})",
                this.joinPush.timeout
            )
            Push(this, ChannelEvents.Leave.event, mutableMapOf(), this.timeout).apply {
                send()
            }
            this.state = ChannelStates.Errored
            this.joinPush.reset()
            this.rejoinTimer.scheduleTimeout()
        }

        this.on(ChannelEvents.Reply.event) { message ->
            replyEventName(message!!.ref!!).let { replyEventName ->
                this.trigger(replyEventName, message.copy(event = replyEventName))
            }
        }
    }

    internal fun rejoinUntilConnected() {
        this.rejoinTimer.scheduleTimeout()
        if (this.socket.isConnected()) {
            this.rejoin()
        }
    }

    open fun join(timeout: Number = this.timeout): Push {
        if (this.joinedOnce) {
            throw Error("tried to join multiple times. 'join' can only be called a single time per channel instance")
        } else {
            this.joinedOnce = true
            this.rejoin(timeout)
            return this.joinPush
        }
    }

    open fun leave(timeout: Number = this.timeout): Push {
        this.state = ChannelStates.Leaving
        val onClose: MessageCallback = { message ->
            if (this.socket.hasLogger())
                this.socket.log("channel", "leave ${this.topic}")

            this.trigger(ChannelEvents.Close.event, message)
        }

        val leavePush = Push(this, ChannelEvents.Leave.event, mutableMapOf(), timeout)

        leavePush.receive("ok", onClose)
            .receive("timeout", onClose)

        leavePush.send()

        if (!this.canPush) {
            leavePush.trigger("ok", mutableMapOf())
        }

        return leavePush
    }

    open fun onClose(callback: MessageCallback) {
        this.on(ChannelEvents.Close.event, callback)
    }

    open fun onError(callback: MessageCallback): Int {
        return this.on(ChannelEvents.Error.event) { message -> callback(message) }
    }

    /**
     * Overridable message hook
     *
     * Receives all events for specialized message handling
     * before dispatching to the channel callbacks.
     *
     * Must return the payload, modified or unmodified
     * @param {string} event
     * @param {Object} payload
     * @param {integer} ref
     * @returns {Object}
     */
    open fun onMessage(event: String, message: Message?): Message? {
        return message
    }

    /**
     * Subscribes on channel events
     *
     * Subscription returns a ref counter, which can be used later to
     * unsubscribe the exact event listener
     *
     * @example
     * const ref1 = channel.on("event", do_stuff)
     * const ref2 = channel.on("event", do_other_stuff)
     * channel.off("event", ref1)
     * // Since unsubscription, do_stuff won't fire,
     * // while do_other_stuff will keep firing on the "event"
     *
     * @param {string} event
     * @param {Function} callback
     * @returns {integer} ref
     */
    open fun on(event: String, callback: MessageCallback): Int {
        val ref = this.bindingRef++
        this.bindings.add(Binding(event, ref, callback))
        return ref
    }

    open fun off(event: String, ref: Int? = null) {
        this.bindings = this.bindings.filter { bind ->
            !(bind.event == event && (ref == null || ref == bind.ref))
        }.toMutableList()
    }

    open fun push(event: String, payload: SocketPayload, timeout: Number = this.timeout): Push {
        if (!this.joinedOnce) {
            throw Error("tried to push '${event}' to '${this.topic}' before joining. Use channel.join() before pushing events")
        }
        val pushEvent = Push(this, event, payload, timeout)
        if (this.canPush) {
            pushEvent.send()
        } else {
            pushEvent.startTimeout()
            this.pushBuffer.add(pushEvent)
        }

        return pushEvent
    }

    internal fun isLifecycleEvent(event: ChannelEvents): Boolean = CHANNEL_LIFECYCLE_EVENTS.indexOf(event) >= 0
    internal fun isLifecycleEvent(event: String): Boolean =
        CHANNEL_LIFECYCLE_EVENTS.map { it.event }.indexOf(event) >= 0

    internal fun isMember(message: Message): Boolean {
        if (this.topic != message.topic) return false

        return if (message.joinRef != null && message.joinRef != this.joinRef() && this.isLifecycleEvent(message.event)) {

            if (this.socket.hasLogger())
                this.socket.log("channel", "dropping outdated message", message)

            false
        } else {
            true
        }
    }

    internal fun joinRef(): String? = this.joinPush.ref

    internal fun sendJoin(timeout: Milliseconds) {
        this.state = ChannelStates.Joining
        this.joinPush.resend(timeout)
    }

    internal fun rejoin(timeout: Milliseconds = this.timeout) {
        if (this.isLeaving()) {
            return
        }
        this.sendJoin(timeout)
    }

    internal fun trigger(event: String, message: Message?) {
        val handledPayload = this.onMessage(event, message)

        if (message != null && handledPayload == null) {
            throw Error("channel onMessage callbacks must return the payload, modified or unmodified")
        }

        for (bind in this.bindings) {
            if (bind.event != event) {
                continue
            }
            bind.callback(message)
        }
    }

    internal fun replyEventName(ref: String): String {
        return "chan_reply_$ref"
    }

    internal fun isClosed(): Boolean {
        return this.state == ChannelStates.Closed
    }

    internal fun isErrored(): Boolean {
        return this.state == ChannelStates.Errored
    }

    internal fun isJoined(): Boolean {
        return this.state == ChannelStates.Joined
    }

    internal fun isJoining(): Boolean {
        return this.state == ChannelStates.Joining
    }

    internal fun isLeaving(): Boolean {
        return this.state == ChannelStates.Leaving
    }
}