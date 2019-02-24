package io.vithor.kphoenix

import io.vithor.kphoenix.Presence.PresenceMap

typealias Diff = Map<String, Presence.PresenceState>
//typealias Meta = Map<String, Any>
typealias OnJoinCallback = ((key: String, currentPresence: PresenceMap?, newPresence: PresenceMap) -> Unit)?
typealias OnLeaveCallback = ((key: String, currentPresence: PresenceMap, leftPresence: PresenceMap) -> Unit)?
typealias OnSync = ((Presence.PresenceState) -> Unit)?
typealias ListBy = (key: String, presence: PresenceMap) -> Any

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
            Event(PresenceEventType.State, "presence_state"),
            Event(PresenceEventType.Diff, "presence_diff")
        )
    )

    class PresenceState(val map: Map<String, PresenceMap>) : MutableMap<String, PresenceMap> by map.toMutableMap() {
//        constructor(map: Map<String, Any>? = null) : this(map?.mapValues { it as PresenceMap } ?: mutableMapOf())

        fun clone() = PresenceState(map.toMutableMap())
    }

    class PresenceMap(val map: Map<String, MutableList<Meta>>) :
        MutableMap<String, MutableList<Meta>> by map.toMutableMap() {
        val metas: MutableList<Meta>
            get() {
                return get("metas")!!
            }

        fun clone() = PresenceMap(map)
    }

    class Meta(val map: Map<String, Any>) : MutableMap<String, Any> by map.toMutableMap() {
        constructor(unknown: Any?) : this(unknown?.let { it as Map<String, Any> } ?: mutableMapOf())
    }

    enum class PresenceEventType(val type: String) {
        State("state"), Diff("diff")
    }

    data class Event(val type: PresenceEventType, val value: String)

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
            msg?.payload?.entries
            val newState = presenceStateOf(msg?.payload?.toMutableMap())
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
            val diff: Diff = msg?.payload?.toMutableMap()?.mapValues { it as PresenceState } ?: mapOf()
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
            var leaves = PresenceState(currentState.filterKeys { key ->
                !newState.containsKey(key)
            })

            var joins = PresenceState(newState.filterKeys { key ->
                !currentState.containsKey(key)
            })

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
//            val joins = mutableMapOf<String, PresenceMap>()
//            val leaves = mutableMapOf<String, PresenceMap>()

//            this.map(state) { key, presence ->
//                if (!newState.containsKey(key)) {
//                    leaves[key] = presence
//                }
//            }

//            this.map(newState) { key, newPresence ->
//                val currentPresence = state[key]
//
//                if (currentPresence != null) {
//                    val newRefs = newPresence.metas.map { m ->
//                        m.phx_ref
//                    }
//
//                    val curRefs = currentPresence.metas.map { m ->
//                        m.phx_ref
//                    }
//                    val joinedMetas = newPresence.metas.filter { m ->
//                        curRefs.indexOf(m.phx_ref) < 0
//                    }
//                    val leftMetas = currentPresence.metas.filter { m ->
//                        newRefs.indexOf(m.phx_ref) < 0
//                    }
//                    if (joinedMetas.length > 0) {
//                        joins[key] = newPresence
//                        joins[key].metas = joinedMetas
//                    }
//                    if (leftMetas.length > 0) {
//                        leaves[key] = currentPresence.clone()
//                        leaves[key].metas = leftMetas
//                    }
//                } else {
//                    joins[key] = newPresence
//                }
//            }

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


            /*
            if (onJoin == null) {
                onJoin = {}
            }
            if (onLeave == null) {
                onLeave = {}
            }

            this.map(joins) { (key, newPresence) ->
                val currentPresence = state[key]
                state[key] = newPresence
                if (currentPresence) {
                    val joinedRefs = state[key].metas.map(m = > m . phx_ref
                    )
                    val curMetas = currentPresence.metas.filter(m = > joinedRefs . indexOf (m.phx_ref) < 0
                    )
                    state[key].metas.unshift(... curMetas
                    )
                }
                onJoin(key, currentPresence, newPresence)
            }
            )
            this.map(leaves) { (key, leftPresence) ->
                val currentPresence = state[key]
                if (
                    !currentPresence
                ) {
                    return
                }
                val refsToRemove = leftPresence.metas.map(m = > m . phx_ref
                )
                currentPresence.metas = currentPresence.metas.filter(p = > {
                    return refsToRemove.indexOf(p.phx_ref) < 0
                }
                )
                onLeave(key, currentPresence, leftPresence)
                if (currentPresence.metas.isEmpty()) {
                    delete state [key]
                }
            })

            */


            return state
        }

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
//
//        fun map(obj: PresenceMap, func: (key: String, pres: PresenceMap) -> Any): PresenceMap {
//            return obj.map { (key, value) -> func(key, value) }
//        }

//        fun clone(obj) {
//            return JSON.parse(JSON.stringify(obj))
//        }
    }

    val List<Event>.state: String
        get() {
            return find { it.type == PresenceEventType.State }!!.value
        }

    val List<Event>.diff: String
        get() {
            return find { it.type == PresenceEventType.Diff }!!.value
        }
}

fun presenceStateOf(map: Map<String, Any>? = null) =
    Presence.PresenceState(map?.mapValues { (it.value as Map<String, Any>).mapValues { Presence.Meta(it.value) }.toMutableMap() as PresenceMap } ?: mutableMapOf())

fun presenceMapOf(map: Map<String, Any>? = null) =
    PresenceMap(map?.mapValues { it as MutableList<Presence.Meta> } ?: mutableMapOf())

fun presenceMapOf(vararg pairs: Pair<String, MutableList<Presence.Meta>>) =
    PresenceMap(pairs.toMap())
