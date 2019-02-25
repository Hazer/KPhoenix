package sample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.vithor.kphoenix.Message

class MessagesAdapter : RecyclerView.Adapter<MessageViewHolder>() {
    val items = mutableListOf<Message>()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder =
        MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_message, parent, false))


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): Message = items[position]

    fun refresh(newItems: List<Message>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun add(message: Message) {
        items.add(message)
        notifyItemInserted(items.size)
    }
}

class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val txtMessage: TextView = itemView.findViewById(R.id.txt_message)

    fun bind(message: Message) {
        txtMessage.text = message["content"].toString()
    }

}