package my.reposter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.chat_element.view.*

class ChatAdapter(context: Context, resource: Int, data: List<Chat>) : ArrayAdapter<Chat>(context, resource, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.chat_element, null)
        val item = getItem(position)

        item?.let {
            view.chatName.text = item.name
            view.chatId.text = item.id.toString()
            view.checkBox.isChecked = item.selected
        }

        return view
    }
}

data class Chat(val name: String, val id: Long, var selected: Boolean)