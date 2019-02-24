package io.vithor.facades

/**
 * Timer implementation to schedule closures, setTimeout in JS, Handler.postDelayed in Android, etc
 */
expect class OSTimer {

}

expect fun setInterval(function: () -> Unit, intervalMs: Long): OSTimer

expect fun clearInterval(timer: OSTimer?)

expect fun clearTimeout(timer: OSTimer?)

expect fun setTimeout(function: () -> Unit, delay: Long): OSTimer