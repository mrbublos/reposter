package my.reposter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.config_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import my.reposter.db.Db

@InternalCoroutinesApi
class SettingsActivity : AppCompatActivity() {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_list)
    }

    override fun onResume() {
        super.onResume()
        val dao = Db.instance(context = applicationContext).repostsDao()
        GlobalScope.launch {
            val configs = dao.getReposts()
            launch(Dispatchers.Main) {
                configList.adapter = ConfigAdapter(applicationContext, R.layout.config_element, configs)
            }
        }

        add.setOnClickListener {
            startAddActivity()
        }

        clear.setOnClickListener {
            GlobalScope.launch { dao.clear() }
        }
    }

    private fun startAddActivity() {
        val intent = Intent(this, AddActivity::class.java)
        startActivity(intent)
    }
}