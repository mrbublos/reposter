package my.reposter

import android.content.Intent
import android.os.Bundle
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

        data.hint = "Enter code"
        state.observe(this, Observer {
            when (it) {
                State.SETUP -> data.hint = "Processing.."
                State.CODE -> {
                    data.text.clear()
                    data.hint = "Enter code"
                }
                State.PASSWORD -> {
                    data.text.clear()
                    data.hint = "Enter password"
                }
                State.AUTHORIZED -> {
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
            WorkManager.getInstance(baseContext).cancelAllWorkByTag(tag)
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
        WorkManager.getInstance(baseContext).cancelAllWorkByTag(tag)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repost = PeriodicWorkRequestBuilder<RepostWork>(Duration.ofMinutes(1))
            .setConstraints(constraints)
            .addTag(tag)
            .build()
        if (!WorkManager.getInstance(baseContext).getWorkInfosByTagLiveData(tag).value.isNullOrEmpty()) { return }
        WorkManager.getInstance(baseContext).enqueue(repost)
    }
}