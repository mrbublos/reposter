package my.reposter

import android.os.Bundle
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.config_add.*
import kotlinx.android.synthetic.main.config_list.add
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import my.reposter.db.Db
import my.reposter.db.RepostConfig

class AddActivity : AppCompatActivity() {

    var from: Int = -1
    var to: Int = -1

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_list)

        val dao = Db.instance(context = applicationContext).repostsDao()

        GlobalScope.launch(Dispatchers.IO) {
            val channels = TeleService.getChats(100)
            launch(Dispatchers.Main) {
                fromList.adapter = ChatAdapter(applicationContext, R.layout.chat_element, channels)
                toList.adapter = ChatAdapter(applicationContext, R.layout.chat_element, channels)
            }
        }

        fromList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            (fromList.getItemAtPosition(from) as Chat).selected = false
            from = position
            (fromList.getItemAtPosition(from) as Chat).selected = !(fromList.getItemAtPosition(from) as Chat).selected
            (fromList.adapter as BaseAdapter).notifyDataSetChanged()
        }

        toList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            (toList.getItemAtPosition(from) as Chat).selected = false
            from = position
            (toList.getItemAtPosition(from) as Chat).selected = !(toList.getItemAtPosition(from) as Chat).selected
            (toList.adapter as BaseAdapter).notifyDataSetChanged()
        }

        add.setOnClickListener {
            if (from == -1 || to == -1) { return@setOnClickListener }
            val from = fromList.getItemAtPosition(from) as Chat
            val to = toList.getItemAtPosition(to) as Chat
            dao.insert(RepostConfig(fromChatId = from.id, toChatId = to.id))
            this.finish()
        }
    }
}