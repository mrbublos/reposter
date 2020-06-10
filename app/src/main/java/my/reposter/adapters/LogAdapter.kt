package my.reposter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.config_element.view.*
import kotlinx.android.synthetic.main.log_element.view.*
import my.reposter.R
import my.reposter.db.LogEntry
import my.reposter.db.RepostConfig

class LogAdapter(context: Context, private val resource: Int, data: List<LogEntry>) : ArrayAdapter<LogEntry>(context, resource, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, null)
        val item = getItem(position)

        item?.let {
            view.element.text = item.message
        }

        return view
    }
}