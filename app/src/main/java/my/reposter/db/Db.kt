package my.reposter.db

import android.content.Context
import androidx.room.*

@Database(entities = [RepostConfig::class], version = 1)
abstract class Db : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: Db? = null

        fun instance(context: Context): Db {
            val i = instance
            if (i != null) { return i }

            return synchronized(this) {
                val i2 = instance
                if (i2 != null) {
                    i2
                } else {
                    val newInstance = Room.databaseBuilder(context.applicationContext, Db::class.java, "repost.db").build()
                    instance = newInstance
                    newInstance
                }
            }
        }
    }

    abstract fun repostsDao(): RepostsDao
}

@Entity(tableName = "reposts")
data class RepostConfig(
    @PrimaryKey
    val id: Long = System.nanoTime(),
    val fromChatId: Long,
    val toChatId: Long,
    var lastMessageId: Long = 0L
)