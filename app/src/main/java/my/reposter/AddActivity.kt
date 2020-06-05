package my.reposter

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.config_add.*
import kotlinx.android.synthetic.main.config_list.add
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import my.reposter.TeleService.tag
import my.reposter.db.Db
import my.reposter.db.RepostConfig

class AddActivity : AppCompatActivity() {

    var from: Int = -1
    var to: Int = -1

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_add)

        val dao = Db.instance(context = applicationContext).repostsDao()

        GlobalScope.launch(Dispatchers.IO) {
            val fromChannels = TeleService.getChats(100)
            val toChannels = fromChannels.map { Chat(it.name, it.id, it.selected) }
            launch(Dispatchers.Main) {
                fromList.adapter = ChatAdapter(applicationContext, R.layout.chat_element, fromChannels)
                toList.adapter = ChatAdapter(applicationContext, R.layout.chat_element, toChannels)
            }
        }

        fromList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (from != -1) { (fromList.getItemAtPosition(from) as Chat).selected = false }
            from = position
            (fromList.getItemAtPosition(from) as Chat).selected = !(fromList.getItemAtPosition(from) as Chat).selected
            (fromList.adapter as BaseAdapter).notifyDataSetChanged()
        }

        toList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (to != -1 ) { (toList.getItemAtPosition(to) as Chat).selected = false }
            to = position
            (toList.getItemAtPosition(to) as Chat).selected = !(toList.getItemAtPosition(to) as Chat).selected
            (toList.adapter as BaseAdapter).notifyDataSetChanged()
        }

        add.setOnClickListener {
            if (from == -1 || to == -1) { return@setOnClickListener }
            val from = fromList.getItemAtPosition(from) as Chat
            val to = toList.getItemAtPosition(to) as Chat
            val newConfig = RepostConfig(fromChatId = from.id, toChatId = to.id)
            GlobalScope.launch { dao.insert(newConfig) }
            Log.d(tag, "Creating config $newConfig")
            this.finish()
        }
    }
}