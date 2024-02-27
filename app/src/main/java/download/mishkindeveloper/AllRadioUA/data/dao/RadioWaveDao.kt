package download.mishkindeveloper.AllRadioUA.data.dao

import androidx.room.*
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave

@Dao
interface RadioWaveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg radioWave: RadioWave)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(listRadioWave: List<RadioWave>)

    @Delete
    fun delete(radioWave: RadioWave)

    @Update
    fun update(vararg radioWave: RadioWave)

    @Query("SELECT * FROM radiowave")
    fun getAll(): List<RadioWave>

    @Query("SELECT * FROM radiowave WHERE custom=1")
    fun getCustomAll(): List<RadioWave>

    @Query("SELECT * FROM radiowave WHERE favorite =1")
    fun getFavoriteRadioWave(): List<RadioWave>

    @Query("SELECT * FROM radiowave WHERE id==:id")
    fun getRadioWaveForId(id: Int?):RadioWave

    @Query("SELECT * FROM radiowave ORDER BY LOWER(name) ASC")
    fun getAllSortAsc():List<RadioWave>

    @Query("SELECT * FROM radiowave ORDER BY LOWER(name) DESC")
    fun getAllSortDesc():List<RadioWave>

    @Query("SELECT * FROM radiowave WHERE  custom=1 ORDER BY LOWER(name) ASC")
    fun getCustomSortAsc():List<RadioWave>

    @Query("SELECT * FROM radiowave WHERE  custom=1 ORDER BY LOWER(name) DESC")
    fun getCustomSortDesc():List<RadioWave>

    @Query("SELECT * FROM radiowave ORDER BY countOpen ASC")
    fun getPopularSortAsc():List<RadioWave>

    @Query("SELECT * FROM radiowave ORDER BY countOpen DESC")
    fun getPopularSortDesc():List<RadioWave>
}