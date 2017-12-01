package org.yuzhiqiang.smsmulticast

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import com.yuzhiqiang.smsmulticast.R

class MessageArrayAdapter(private val context: Context, private val list: List<Message>) : ArrayAdapter<Message>(context, R.layout.message_list_item, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.message_list_item, parent, false)
        val message = list[position]

        val imageView = itemView.findViewById(R.id.icon) as ImageView
        val textView1 = itemView.findViewById(android.R.id.text1) as TextView
        val textView2 = itemView.findViewById(android.R.id.text2) as TextView
        val textViewStatus = itemView.findViewById(R.id.textViewStatus) as TextView

        when (message.status) {
            Message.NEW -> {
                imageView.setImageResource(android.R.drawable.ic_menu_send)
                textViewStatus.text = "New"
            }
            Message.PENDING -> {
                imageView.setImageResource(android.R.drawable.ic_media_pause)
                textViewStatus.text = "Pending"
            }
            Message.SENDING -> {
                imageView.setImageResource(android.R.drawable.ic_media_play)
                textViewStatus.text = "Sending"
            }
            Message.SENT -> {
                imageView.setImageResource(android.R.drawable.ic_dialog_info)
                textViewStatus.text = "Sent"
            }
            Message.FAILED -> {
                imageView.setImageResource(android.R.drawable.ic_delete)
                textViewStatus.text = "Failed"
            }
        }

        textView1.text = message.destination
        textView2.text = message.content

        return itemView
    }
}
