package my.reposter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.config_element.view.*
import my.reposter.db.RepostConfig

class ConfigAdapter(context: Context, resource: Int, data: List<RepostConfig>) : ArrayAdapter<RepostConfig>(context, resource, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.config_element, null)
        val item = getItem(position)

        item?.let {
            view.from.text = item.fromChatId.toString()
            view.to.text = item.toChatId.toString()
            view.remove.isChecked = false
        }

        return view
    }
}