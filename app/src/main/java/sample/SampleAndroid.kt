package sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.vithor.kphoenix.Presence
import io.vithor.kphoenix.Socket
import io.vithor.kphoenix.facades.NVWebSocket
import timber.log.Timber

actual class Sample {
    actual fun checkMe() = 44
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hello()
        Sample().checkMe()
        setContentView(R.layout.activity_main)

        val socket = Socket("ws://192.168.0.101:4000/socket", Socket.Options(
            transport = NVWebSocket, params = mutableMapOf("user_id" to "23")
        ))

        socket.logger = { kind, msg, data ->
            Timber.tag(kind).d("$msg\n$data")
        }

        socket.onOpen {
            Timber.d("On Open")
            with(socket.channel("room:lobby")) {
                val presence = Presence(this)

                presence.onJoin { key, currentPresence, newPresence ->
                    Timber.d("Presence Join: $key, $currentPresence, $newPresence")
                }

                presence.onSync {
                    Timber.d("Presence Sync: $it")
                }

                on("room:lobby:new_message") { message ->
                    Timber.d("Shout $message")
                }

                join {
                    receive("ok") { message ->
                        Timber.d("Receive ok $message")
                    }

                    receive("error") { message ->
                        Timber.d("Receive error $message")
                    }
                }

                push("message:add", mutableMapOf("name" to "TestV", "message" to "Just a message"))
            }
        }

        with(socket) {

            onClose {
                Timber.d("On Close $it")
            }

            onError {
                Timber.d("On Error $it")
            }

            onMessage {
                Timber.d("On Message $it")
            }

            connect()
        }
    }
}