package download.mishkindeveloper.AllRadioUA.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import java.util.*


class AlarmRadioPlayerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isStationPlaying = false

    inner class PlayerBinder : Binder() {
        fun getService(): AlarmRadioPlayerService = this@AlarmRadioPlayerService
    }
    private val playerBinder = PlayerBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return playerBinder
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val radioStationUrl = intent?.getStringExtra("radioStationUrl")
        if (!radioStationUrl.isNullOrEmpty()) {
            startRadioStation(radioStationUrl)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    private fun startRadioStation(url: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(url)
        mediaPlayer?.prepareAsync()

        mediaPlayer?.setOnPreparedListener { mp ->
            mp.start()
            fadeVolumeIn(mediaPlayer!!)
            isStationPlaying = true // Установка флага, что станция начала воспроизводиться
        }
    }

     fun stopRadioStation() {
         if (isStationPlaying && mediaPlayer?.isPlaying == true) {
             mediaPlayer?.stop()
         }
         mediaPlayer?.reset()
         isStationPlaying = false
    }

    private fun fadeVolumeIn(mediaPlayer: MediaPlayer) {
        val maxVolume = 1.0f
        val numSteps = 40
        val duration = 195000
        val startVolume = 0.02f
        val volumeStep = (maxVolume - startVolume) / numSteps

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            var currentStep = 0
            override fun run() {
                if (currentStep >= numSteps) {
                    timer.cancel()
                    mediaPlayer.setVolume(maxVolume, maxVolume)
                } else {
                    val volume = startVolume + currentStep * volumeStep
                    mediaPlayer.setVolume(volume, volume)
                    currentStep++
                }
            }
        }, 0, duration / numSteps.toLong())
    }
}
