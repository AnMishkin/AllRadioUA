package download.mishkindeveloper.AllRadioUA.data.repository

import download.mishkindeveloper.AllRadioUA.data.AppDataBase
import download.mishkindeveloper.AllRadioUA.data.entity.Track
import javax.inject.Inject

class TrackRepository @Inject constructor(private val db: AppDataBase)  {
    private var trackDao = db.getTrackDao()!!

    fun insertTrack(track: Track) = trackDao.insert(track)

    fun deleteTrack(track: Track) = trackDao.delete(track)

    fun updateTrack(track: Track) = trackDao.update(track)

    fun getAllTrack(): List<Track> = trackDao.getAll()

    fun deleteAll() = trackDao.deleteAll()
}