package download.mishkindeveloper.AllRadioUA.data.repository

import download.mishkindeveloper.AllRadioUA.data.AppDataBase
import download.mishkindeveloper.AllRadioUA.data.dao.RadioWaveDao
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import javax.inject.Inject

class RadioWaveRepository
@Inject constructor(private val db: AppDataBase) {
    private var radioWaveDao: RadioWaveDao = db.getRadioWaveDao()!!

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
}