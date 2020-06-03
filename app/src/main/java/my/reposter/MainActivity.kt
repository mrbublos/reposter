package my.reposter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.work.*
import androidx.work.impl.utils.LiveDataUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.time.Duration

@InternalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private var code = ""
    private var status: String = "Authenticating"
    private val tag = "reposter"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        data.hint = "Enter code"
        button.setOnClickListener {
            code = data.text.toString()
            startService()
        }

        stop.setOnClickListener {
            WorkManager.getInstance(baseContext).cancelAllWorkByTag(tag)
        }

        GlobalScope.launch(Dispatchers.IO) {
            TeleService.auth(filesDir.absolutePath)
        }
    }

    private fun startService() {
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