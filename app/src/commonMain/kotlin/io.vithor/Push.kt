package io.vithor

open class Push(val channel: Channel, val event: String, payload: SocketPayload?, var timeout: Milliseconds) {
    var receivedResp: Message? = null
    var timeoutTimer: OSTimer? = null
    var recHooks: MutableMap<String, MutableList<MessageCallback>> = mutableMapOf()
    var sent = false
    var payload: SocketPayload = payload ?: mutableMapOf()

    /// The reference ID of the Push
    var ref: String? = null

    /// The event that is associated with the reference ID of the Push
    var refEvent: String? = null

    open fun resend(timeout: Milliseconds) {
        this.timeout = timeout
        this.reset()
        this.send()
    }

    open fun send() {
        if (this.hasReceived("timeout")) {
            return
        }
        this.startTimeout()
        this.sent = true

        val message = Message(
            topic = this.channel.topic,
            event = this.event,
            payload = this.payload, // this.payload()
            ref = this.ref,
            joinRef = this.channel.joinRef()
        )

        this.channel.socket.push(message)
    }

    open fun receive(status: String, callback: MessageCallback): Push {
        if (this.hasReceived(status) && this.receivedResp != null) {
            callback(this.receivedResp)
        }

        /// Create a new array of hooks if no previous hook is associated with status
        if (recHooks[status] == null) {
            recHooks[status] = mutableListOf(callback)
        } else {
            /// A previous hook for this status already exists. Just append the new hook
            recHooks[status]?.add(callback)
        }

        return this
    }

    internal fun reset() {
        this.cancelRefEvent()
        this.ref = null
        this.refEvent = null
        this.receivedResp = null
        this.sent = false
    }

    private fun matchReceive(status: String, message: Message) {
        this.recHooks[status]?.forEach { callback -> callback(message) }
    }

    private fun cancelRefEvent() {
        this.refEvent?.let {
            this.channel?.off(event = it)
        }
    }

    private fun cancelTimeout() {
        clearTimeout(this.timeoutTimer)
        this.timeoutTimer = null
    }

    private fun clearTimeout(timeoutTimer: OSTimer?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal fun startTimeout() {
        if (this.timeoutTimer != null) {
            this.cancelTimeout()
        }
        this.ref = this.channel.socket.makeRef()
        this.refEvent = this.channel.replyEventName(this.ref!!)

        this.channel.on(this.refEvent!!) { message ->
            this.cancelRefEvent()
            this.cancelTimeout()

            this.receivedResp = message

            val status = message?.status ?: return@on

            this.matchReceive(status, message)
        }

        this.timeoutTimer = setTimeout({
            this.trigger("timeout", mutableMapOf())
        }, this.timeout)
    }

    private fun setTimeout(function: () -> Unit, timeout: Milliseconds): OSTimer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun hasReceived(status: String): Boolean {
        return this.receivedResp?.status == status
    }

    internal fun trigger(status: String, response: SocketPayload) {
        val refEvent = this.refEvent ?: return

        response["status"] = status

        val message = Message(event = refEvent, payload = response)

        this.channel.trigger(refEvent, message)
    }
}