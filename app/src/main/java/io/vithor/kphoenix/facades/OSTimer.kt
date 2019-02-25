package io.vithor.kphoenix.facades

import android.os.Handler
import android.os.Looper


/**
 * Timer implementation to schedule closures, setTimeout in JS, Handler.postDelayed in Android, etc
 */
actual class OSTimer {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    fun setTimeout(function: () -> Unit, delay: Long) = guardCreation {
        runnable = Runnable { function() }

        handler.postDelayed(runnable, delay)
    }

    fun setInterval(function: () -> Unit, intervalMs: Long) = guardCreation {
        runnable = object : Runnable {

            override fun run() {
                try {
                    function()
                } catch (e: Exception) {
                    // TODO: handle exception
                } finally {
                    //also call the same runnable to call it at regular interval
                    handler.postDelayed(this, intervalMs)
                }
            }
        }

        //runnable must be execute once
        handler.post(runnable)
    }

    private fun guardCreation(block: () -> Boolean) {
        synchronized(this) {
            if (runnable != null) throw IllegalStateException("Cannot reuse timer, create a new ${OSTimer::class.qualifiedName}")
            block()
        }
    }

    fun clearInterval() = clearRunnable()

    fun clearTimeout() = clearRunnable()

    private fun clearRunnable() {
        synchronized(this) {
            if (runnable != null)
                handler.removeCallbacks(runnable)
        }
    }
}

actual fun setInterval(function: () -> Unit, intervalMs: Long): OSTimer {
    return OSTimer().also {
        it.setInterval(function, intervalMs)
    }
}

actual fun clearInterval(timer: OSTimer?) {
    timer?.clearInterval()
}

actual fun clearTimeout(timer: OSTimer?) {
    timer?.clearTimeout()
}

actual fun setTimeout(function: () -> Unit, delay: Long): OSTimer {
    return OSTimer().also {
        it.setTimeout(function, delay)
    }
}

private val handler = Handler(Looper.getMainLooper())

actual fun onMainThread(function: () -> Unit) {
    handler.post(function)
}