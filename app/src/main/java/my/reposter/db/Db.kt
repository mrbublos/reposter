package my.reposter.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RepostConfig::class, Setting::class, LogEntry::class], version = 3)
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
                    val newInstance = Room.databaseBuilder(context.applicationContext, Db::class.java, "repost.db")
                        .fallbackToDestructiveMigration()
                        .build()
                    instance = newInstance
                    newInstance
                }
            }
        }
    }

    abstract fun repostsDao(): RepostsDao
    abstract fun settingsDao(): SettingsDao
    abstract fun logsDao(): LogDao
}