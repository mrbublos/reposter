package my.reposter.db

import androidx.room.*

@Dao
interface RepostsDao {

    @Query("SELECT * FROM reposts")
    fun getReposts(): MutableList<RepostConfig>

    @Query("DELETE FROM reposts")
    fun clear()

    @Insert
    fun insert(data: RepostConfig)

    @Update
    fun update(data: RepostConfig)
}