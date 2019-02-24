package io.vithor

import io.vithor.facades.OSTimer
import io.vithor.facades.clearTimeout
import io.vithor.facades.setTimeout

/**
 *
 * Creates a timer that accepts a `timerCalc` function to perform
 * calculated timeout retries, such as exponential backoff.
 *
 * @example
 * let reconnectTimer = new Timer(() => this.connect(), function(tries){
 *   return [1000, 5000, 10000][tries - 1] || 10000
 * })
 * reconnectTimer.scheduleTimeout() // fires after 1000
 * reconnectTimer.scheduleTimeout() // fires after 5000
 * reconnectTimer.reset()
 * reconnectTimer.scheduleTimeout() // fires after 1000
 *
 * @param {Function} callback
 * @param {Function} timerCalc
 */
class PhxTimer(val callback: () -> Unit, val timerCalc: RetriesCallback) {

    private var timer: OSTimer? = null

    private var tries: Int = 0

    fun reset() {
        this.tries = 0
        clearTimeout(this.timer)
    }

    fun scheduleTimeout() {
        clearTimeout(this.timer)

        this.timer = setTimeout({
            this.tries = this.tries + 1
            this.callback()
        }, this.timerCalc(this.tries + 1))
    }
}
