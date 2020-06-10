package my.reposter.db

import androidx.room.*

@Dao
interface LogDao {

    @Query("SELECT * FROM logs ORDER BY created DESC LIMIT :limit")
    fun getLogs(limit: Int = 20): MutableList<LogEntry>

    @Query("DELETE FROM logs")
    fun clear()

    @Insert
    fun insert(data: LogEntry)

    @Query("DELETE FROM logs WHERE created < :from")
    fun deleteOld(from: Long)

    @Update
    fun update(data: LogEntry)
}


@Entity(tableName = "logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val custom: String = "",
    val created: Long = System.currentTimeMillis()
)