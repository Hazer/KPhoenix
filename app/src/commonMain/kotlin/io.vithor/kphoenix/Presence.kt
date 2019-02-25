@file:Suppress("NOTHING_TO_INLINE")

package io.vithor.kphoenix

//typealias Diff = Map<String, Presence.PresenceState>
//typealias Meta = Map<String, Any>
typealias OnJoinCallback = ((key: String, currentPresence: PresenceMap?, newPresence: PresenceMap) -> Unit)?
typealias OnLeaveCallback = ((key: String, currentPresence: PresenceMap, leftPresence: PresenceMap) -> Unit)?
typealias OnSync = ((PresenceState) -> Unit)?
typealias ListBy = (key: String, presence: PresenceMap) -> Any

typealias PresenceMap = MutableMap<String, List<Meta>>
typealias PresenceState = MutableMap<String, PresenceMap>
// Diff has keys "joins" and "leaves", pointing to a PresenceState each
// containing the users that joined and left, respectively...
typealias Diff = Map<String, PresenceState>

typealias Meta = MutableMap<String, Any>

const val phx_ref = "phx_ref"
const val diff_joins = "joins"
const val diff_leaves = "leaves"

/**
 * Initializes the Presence
 * @param {Channel} channel - The Channel
 * @param {Object} opts - The options,
 *        for example `{events: {state: "state", diff: "diff"}}`
 */
open class Presence(val channel: Channel, opts: Options = Options()) {
    data class Options(
        val events: List<Event> = mutableListOf(
            Event(EventType.State, "presence_state"),
            Event(EventType.Diff, "presence_diff")
        )
    )

    enum class EventType(val type: String) {
        State("state"), Diff("diff")
    }

    data class Event(val type: EventType, val value: String)

    data class Caller(var onJoin: OnJoinCallback, var onLeave: OnLeaveCallback, var onSync: OnSync)

    private var caller = Caller(onJoin = { _, _, _ -> },
        onLeave = { _, _, _ -> },
        onSync = {})

    private var joinRef: String? = null

    private var state = presenceStateOf()

    private var pendingDiffs: MutableList<Diff> = mutableListOf()

    val events = opts.events

    init {
        this.channel.on(events.state) { msg ->
            val newState: PresenceState = msg?.payload?.mapValues {
                @Suppress("UNCHECKED_CAST")
                it.value as PresenceMap
            }!!.toMutableMap()

            val (onJoin, onLeave, onSync) = this.caller

            this.joinRef = this.channel.joinRef()
            this.state = syncState(this.state, newState, onJoin, onLeave)

            this.pendingDiffs.forEach { diff ->
                this.state = syncDiff(this.state, diff, onJoin, onLeave)
            }

            this.pendingDiffs = mutableListOf()
            onSync?.invoke(state)
        }

        this.channel.on(events.diff) { msg ->
            val diff: Diff = msg?.payload?.toMutableMap()?.mapValues {
                @Suppress("UNCHECKED_CAST")
                it.value as PresenceState
            } ?: mapOf()

            val (onJoin, onLeave, onSync) = this.caller

            if (inPendingSyncState()) {
                this.pendingDiffs.add(diff)
            } else {
                this.state = syncDiff(this.state, diff, onJoin, onLeave)
                onSync?.invoke(state)
            }
        }
    }

    fun onJoin(callback: OnJoinCallback) {
        this.caller.onJoin = callback
    }

    fun onLeave(callback: OnLeaveCallback) {
        this.caller.onLeave = callback
    }

    fun onSync(callback: OnSync) {
        this.caller.onSync = callback
    }
//
//    fun list(by: ListBy) {
//        return Presence.list(this.state, by)
//    }

    private fun inPendingSyncState(): Boolean = this.joinRef == null || (this.joinRef != this.channel.joinRef())

    companion object {
        fun syncState(
            currentState: PresenceState,
            newState: PresenceState,
            onJoin: OnJoinCallback = null,
            onLeave: OnLeaveCallback = null
        ): PresenceState {
            val state = currentState.clone()
            val leaves = currentState.filterKeys { key ->
                !newState.containsKey(key)
            }.toMutableMap()

            val joins = newState.filterKeys { key ->
                !currentState.containsKey(key)
            }.toMutableMap()

            newState.forEach { (key: String, newPresence: PresenceMap) ->
                // Looking for differences in metadata of already present users.
                state[key]?.let { currentPresence ->
                    val curRefs = currentPresence.metas.map { it[phx_ref] as String }

                    val newMetas = newPresence.metas.filter { meta ->
                        curRefs.contains(meta[phx_ref] as String)
                    }

                    if (newMetas.isNotEmpty()) {
                        joins[key] = presenceMapOf("metas" to newMetas.toMutableList())
                    }

                    val newRefs = newPresence.metas.map { it[phx_ref] as String }
                    val leftMetas = currentPresence.metas.filter { meta ->
                        newRefs.contains(meta[phx_ref] as String)
                    }
                    if (leftMetas.isNotEmpty()) {
                        leaves[key] = presenceMapOf("metas" to leftMetas.toMutableList())
                    }
                }

                return@forEach
            }

            return syncDiff(state, mapOf(diff_joins to joins, diff_leaves to leaves), onJoin, onLeave)
        }

        fun syncDiff(
            currentState: PresenceState,
            newState: Diff,
            onJoin: OnJoinCallback,
            onLeave: OnLeaveCallback
        ): PresenceState {
            val state = currentState.clone()

            val joins = newState[diff_joins] ?: presenceStateOf()
            val leaves = newState[diff_leaves] ?: presenceStateOf()

            for ((key, newPresence) in joins) {
                val currentPresence: PresenceMap? = state[key]
                state[key] = newPresence
                if (currentPresence != null) {
                    val joinedRefs = state[key]!!.metas.map { it[phx_ref] as String }

                    val curMetas = currentPresence.metas.filter { meta ->
                        joinedRefs.contains(meta[phx_ref] as String)
                    }
                    state[key]!!.metas.addAll(curMetas)
                }

                onJoin?.invoke(key, currentPresence, newPresence)
            }

            for ((key, leftPresence) in leaves) {
                state[key]?.let { currentPresence ->
                    val refsToRemove = leftPresence.metas.map { it[phx_ref] as String }
                    val keepMetas = currentPresence.metas.filter { meta ->
                        !refsToRemove.contains(meta[phx_ref] as String)
                    }

                    onLeave?.invoke(key, currentPresence, leftPresence)

                    if (keepMetas.isNotEmpty()) {
                        state[key]!!["metas"] = keepMetas.toMutableList()
                    } else {
                        state.remove(key)
                    }
                }
            }


            return state
        }
//
//        /**
//         * Returns the array of presences, with selected metadata.
//         *
//         * @param {Object} presences
//         * @param {Function} chooser
//         *
//         * @returns {Presence}
//         */
//        fun list(presences: PresenceMap, chooser: ListBy? = null): PresenceMap {
//            if (chooser == null) {
//                chooser = { key, pres -> pres }
//            }
//
//            return this.map(presences) { key, presence ->
//                return@map chooser(key, presence)
//            }
//        }

    }

    inline val List<Event>.state: String
        get() {
            return find { it.type == EventType.State }!!.value
        }

    inline val List<Event>.diff: String
        get() {
            return find { it.type == EventType.Diff }!!.value
        }
}

inline fun <K, V> Map<K, V>.clone() = toMutableMap()

inline val PresenceMap.metas: MutableList<Meta>
    get() {
        return get("metas")!!.toMutableList()
    }

inline fun presenceStateOf(): PresenceState = mutableMapOf()

inline fun presenceMapOf(vararg pairs: Pair<String, MutableList<Meta>>): PresenceMap =
    pairs.toMap().toMutableMap()
