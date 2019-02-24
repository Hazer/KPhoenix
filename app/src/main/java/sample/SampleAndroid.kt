package sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.vithor.Message
import io.vithor.Socket
import io.vithor.WebSocket
import io.vithor.facades.NVWebSocket
import io.vithor.facades.TransportFactory
import timber.log.Timber

actual class Sample {
    actual fun checkMe() = 44
}

actual object Platform {
    actual val name: String = "Android"
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hello()
        Sample().checkMe()
        setContentView(R.layout.activity_main)

        val socket = Socket("ws://192.168.0.101:4000/socket", Socket.Options(transport = NVWebSocket.Factory))

        socket.onOpen {
            Timber.d("On Open")
            with(socket.channel("room:lobby")) {
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