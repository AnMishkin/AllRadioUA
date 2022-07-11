package download.mishkindeveloper.AllRadioUA.data.dao

import androidx.room.*
import download.mishkindeveloper.AllRadioUA.data.entity.Track

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg track: Track)

    @Delete
    fun delete(track: Track)

    @Query("DELETE FROM track")
    fun deleteAll()

    @Update
    fun update(vararg track: Track)

    @Query("SELECT * FROM track ORDER BY id DESC")
    fun getAll(): List<Track>
}