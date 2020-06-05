package my.reposter.db

import androidx.room.*

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings")
    fun getSettings(): MutableList<Setting>

    @Query("SELECT * FROM settings WHERE name = :name")
    fun get(name: String): Setting

    @Query("DELETE FROM reposts")
    fun clear()

    @Insert
    fun insert(data: Setting)

    @Update
    fun update(data: Setting)
}


@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey
    val id: Long = System.nanoTime(),
    val name: String,
    val value: Long,
    val custom: String
)