package my.reposter

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import my.reposter.RepostWork.Companion.tag
import java.time.Duration

@InternalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private var state = MutableLiveData<State>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val instance = WorkManager.getInstance(baseContext)
        instance.pruneWork()
        instance.getWorkInfosByTagLiveData(tag).observe(this, Observer {
            val jobState = it.lastOrNull()?.state ?: WorkInfo.State.FAILED
            GlobalScope.launch(Dispatchers.Main) { info.text = jobState.toString() }
        })

        data.hint = "Enter code"
        state.observe(this, Observer {
            when (it) {
                State.SETUP -> data.hint = "Processing.."
                State.CODE -> {
                    data.visibility = View.VISIBLE
                    data.text.clear()
                    data.hint = "Enter code"
                }
                State.PASSWORD -> {
                    data.visibility = View.VISIBLE
                    data.text.clear()
                    data.hint = "Enter password"
                }
                State.AUTHORIZED -> {
                    data.visibility = View.INVISIBLE
                    startJobs()
                    openSettingScreen()
                }
                else -> Unit
            }
        })

        button.setOnClickListener {
            val value = data.text.toString()
            when (state.value) {
                State.CODE -> GlobalScope.launch { TeleService.checkCode(value) }
                State.PASSWORD -> GlobalScope.launch { TeleService.checkPassword(value) }
                else -> Unit
            }
        }

        stop.setOnClickListener {
            WorkManager.getInstance(baseContext).cancelUniqueWork(tag)
        }

        runNow.setOnClickListener {
            startJobs()
        }

        logs.setOnClickListener {
            val intent = Intent(this, LogsActivity::class.java)
            startActivity(intent)
        }

        GlobalScope.launch(Dispatchers.IO) {
            TeleService.auth(filesDir.absolutePath)
                .collect {
                    GlobalScope.launch(Dispatchers.Main) { state.value = it }
                }
        }
    }

    private fun openSettingScreen() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun startJobs() {
        val instance = WorkManager.getInstance(baseContext)
        instance.cancelAllWorkByTag(tag)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repost = PeriodicWorkRequestBuilder<RepostWork>(Duration.ofMinutes(16))
            .setConstraints(constraints)
            .addTag(tag)
            .build()
        instance.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.REPLACE, repost)
    }
}