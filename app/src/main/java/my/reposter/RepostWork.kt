package my.reposter

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import my.reposter.db.Db
import my.reposter.db.RepostConfig

class RepostWork(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val tag = "reposter"
    }

    private val dao = Db.instance(context = context).repostsDao()

    override suspend fun doWork(): Result {
        Log.i(tag, "Starting repost job")
        setForeground(createForegroundInfo("Reposting"))
        dao.getReposts().map { config ->
            repostChat(config)
        }
        return Result.success()
    }

    private suspend fun repostChat(repostConfig: RepostConfig) {
        try {
            val chats = TeleService.getChats(100) // after restart this id cannot be used, so we need to call getChats
            val messages = TeleService.getMessages(repostConfig.fromChatId, repostConfig.lastMessageId).reversedArray()
            Log.i(tag, "Found ${messages.size} messages to repost ")
            messages.filter { it.id > repostConfig.lastMessageId }
                .forEach {
                    TeleService.sendMessage(repostConfig.toChatId, it)
                    repostConfig.lastMessageId = it.id
                    dao.update(repostConfig)
                }
        } catch (e: Exception) {
            Log.e(TeleService.tag, "Job error", e)
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = "Reposter"
        val title = "Reposting telegram"
        val cancel = "Cancel telegram repost"
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_notification_worker)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(0, notification)
    }

}