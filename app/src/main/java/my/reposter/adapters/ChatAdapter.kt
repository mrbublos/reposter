package my.reposter.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.chat_element.view.*
import my.reposter.R

class ChatAdapter(context: Context, private val resource: Int, data: List<Chat>) : ArrayAdapter<Chat>(context, resource, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, null)
        val item = getItem(position)

        item?.let {
            view.chatName.text = item.name
            view.chatId.text = item.id.toString()
            view.setBackgroundColor(if (item.selected) Color.GRAY else Color.WHITE )
        }

        return view
    }
}

data class Chat(val name: String, val id: Long, var selected: Boolean)