package sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.vithor.kphoenix.Message
import io.vithor.kphoenix.Presence
import io.vithor.kphoenix.Socket
import io.vithor.kphoenix.facades.NVWebSocket
import io.vithor.kphoenix.facades.OkWebSocket
import timber.log.Timber
import java.lang.IllegalStateException

actual class Sample {
    actual fun checkMe() = 44
}

class MainActivity : AppCompatActivity() {

    private lateinit var socket: Socket
    private lateinit var adapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hello()
        Sample().checkMe()
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(false)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = MessagesAdapter()
        recyclerView.adapter = adapter

        val btnSend = findViewById<Button>(R.id.btn_send)
        val edtMessage = findViewById<EditText>(R.id.edt_message)

        socket = Socket("ws://192.168.0.188:4000/socket", Socket.Options(
            transport = NVWebSocket,
//            transport = OkWebSocket,
            params = mutableMapOf("user_id" to "23")
        ))

        socket.logger = { kind, msg, data ->
            Timber.tag(kind).d("$msg\n$data")
        }

        socket.onOpen {
            Timber.d("On Open")
            with(socket.channel("room:lobby")) {
                btnSend.setOnClickListener {
                    push("message:add", mutableMapOf("name" to "TestV", "message" to edtMessage.text.toString()))
                    edtMessage.text.clear()
                }

                val presence = Presence(this)

                presence.onJoin { key, currentPresence, newPresence ->
                    Timber.d("Presence Join: $key, $currentPresence, $newPresence")
                    if (key != "23" && newPresence["metas"] != null) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "$key got online!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                presence.onSync {
                    Timber.d("Presence Sync: $it")
                }

                on("room:lobby:new_message") { message ->
                    Timber.d("Shout $message")
                    renderMessages(message)
                }

                join {
                    receive("ok") { message ->
                        Timber.d("Receive ok $message")
                    }

                    receive("error") { message ->
                        Timber.d("Receive error $message")
                    }
                }
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

    private fun renderMessages(message: Message?) {
        adapter.add(message ?: throw IllegalStateException("Corrupt message"))
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect({ Timber.d("Disconnect Callback") })
    }
}