package sample

import android.support.v7.util.DiffUtil
import io.vithor.kphoenix.Message

class MessageDiff : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldMsg: Message, newMsg: Message): Boolean {
        return oldMsg["content"] == newMsg["content"]
    }

    override fun areContentsTheSame(oldMsg: Message, newMsg: Message): Boolean {
        return oldMsg["content"] == newMsg["content"]
    }
}
