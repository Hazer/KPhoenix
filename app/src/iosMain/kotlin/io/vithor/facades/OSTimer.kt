package io.vithor.facades

/**
 * Timer implementation to schedule closures, setTimeout in JS, Handler.postDelayed in Android, etc
 */
actual class OSTimer {

}

actual fun setInterval(function: () -> Unit, intervalMs: Long): OSTimer {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual fun clearInterval(timer: OSTimer?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual fun clearTimeout(timer: OSTimer?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual fun setTimeout(function: () -> Unit, delay: Long): OSTimer {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}