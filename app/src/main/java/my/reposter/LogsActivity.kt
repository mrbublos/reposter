package my.reposter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.log_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import my.reposter.adapters.LogAdapter
import my.reposter.db.Db

class LogsActivity : AppCompatActivity() {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_list)
    }

    override fun onResume() {
        super.onResume()
        val dao = Db.instance(context = applicationContext).logsDao()
        GlobalScope.launch {
            val logs = dao.getLogs()
            launch(Dispatchers.Main) {
                logList.adapter = LogAdapter(applicationContext, R.layout.log_element, logs)
            }
        }
    }
}