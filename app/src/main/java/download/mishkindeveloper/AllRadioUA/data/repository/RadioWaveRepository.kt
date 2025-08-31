package download.mishkindeveloper.AllRadioUA.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import download.mishkindeveloper.AllRadioUA.data.AppDataBase
import download.mishkindeveloper.AllRadioUA.data.dao.RadioWaveDao
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import javax.inject.Inject

class RadioWaveRepository
@Inject constructor(private val db: AppDataBase) {
    private var radioWaveDao: RadioWaveDao = db.getRadioWaveDao()!!
    private val database = FirebaseDatabase.getInstance()
    private val radioWavesRef = database.getReference("https://allradioua-default-rtdb.europe-west1.firebasedatabase.app/")

    fun insertRadioWave(radioWave: RadioWave) = radioWaveDao.insert(radioWave)

    fun insertListRadioWave(listRadioWave: List<RadioWave>) = radioWaveDao.insertAll(listRadioWave)

    fun deleteRadioWave(radioWave: RadioWave) = radioWaveDao.delete(radioWave)

    fun updateRadioWave(radioWave: RadioWave?) = radioWaveDao.update(radioWave!!)

    fun getAllRadioWave(): List<RadioWave> = radioWaveDao.getAll()

    fun getFavoriteRadioWave(): List<RadioWave> = radioWaveDao.getFavoriteRadioWave()

    fun getMediaForId(id: Int?):RadioWave = radioWaveDao.getRadioWaveForId(id)

    fun getAllSortAsc():List<RadioWave> = radioWaveDao.getAllSortAsc()

    fun getAllSortDesc():List<RadioWave> = radioWaveDao.getAllSortDesc()

    fun getCustomSortAsc():List<RadioWave> = radioWaveDao.getCustomSortAsc()

    fun getCustomSortDesc():List<RadioWave> = radioWaveDao.getCustomSortDesc()

    fun getCustomAll():List<RadioWave> = radioWaveDao.getCustomAll()

    fun getPopularSortAsc():List<RadioWave> = radioWaveDao.getPopularSortAsc()

    fun getPopularSortDesc():List<RadioWave> = radioWaveDao.getPopularSortDesc()

    fun getRadioWavesFromDb(callback: (List<RadioWave>) -> Unit) {
        radioWavesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val radioWaves = mutableListOf<RadioWave>()
                for (childSnapshot in snapshot.children) {
                    val radioWave = childSnapshot.getValue(RadioWave::class.java)
                    radioWave?.let { radioWaves.add(it) }
                }
                callback(radioWaves)
            }

            override fun onCancelled(error: DatabaseError) {
                // Обробіть помилку, наприклад, виведіть у лог
                println("Error getting radio waves: ${error.message}")
                callback(emptyList()) // Повертаємо порожній список у випадку помилки
            }
        })
    }
}