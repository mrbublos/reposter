package my.reposter

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import my.reposter.db.Db
import my.reposter.db.LogEntry
import my.reposter.db.RepostConfig
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RepostWork(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val tag = "reposter"
    }

    private val dao = Db.instance(context = context).repostsDao()
    private val logDao = Db.instance(context = context).logsDao()

    override suspend fun doWork(): Result {
        val now  = LocalDateTime.now().toString()

        // if teleservice was destroyed, we need to call auth again
        if (TeleService.state == State.SETUP) {
            TeleService.auth(context.filesDir.absolutePath)
                .takeWhile { it == State.SETUP }
                .collect()
        }

        when(TeleService.state) {
            State.AUTHORIZED -> repostAll(now)
            else -> {
                logDao.insert(LogEntry(message = "Not authorized state at $now"))
            }
        }

        return Result.success()
    }

    private suspend fun repostAll(now: String) {
        Log.i(tag, "Starting repost job")
        setForeground(createForegroundInfo("Reposting"))
        dao.getReposts().map { config ->
            repostChat(config)
        }
        logDao.insert(LogEntry(message = "Repost done at $now"))
        logDao.deleteOld(Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli())
    }

    private suspend fun repostChat(repostConfig: RepostConfig) {
        try {
            val chats = TeleService.getChats(100) // after restart this id cannot be used, so we need to call getChats
            val messages = TeleService.getMessages(repostConfig.fromChatId, repostConfig.lastMessageId).reversedArray()
            Log.i(tag, "Found ${messages.size} messages to repost ")
            messages.filter { it.id > repostConfig.lastMessageId }
                .forEach {
                    try {
                        TeleService.sendMessage(repostConfig.toChatId, it)
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to send a message, skipping", e)
                    }
                    repostConfig.lastMessageId = it.id
                    dao.update(repostConfig)
                }
        } catch (e: Exception) {
            logDao.insert(LogEntry(message = "Job error ${e.message}"))
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