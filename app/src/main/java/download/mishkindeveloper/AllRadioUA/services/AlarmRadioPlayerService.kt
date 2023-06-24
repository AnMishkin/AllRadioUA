package download.mishkindeveloper.AllRadioUA.services

import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.alarm.StopAlarmActivity
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity
import java.util.*


class AlarmRadioPlayerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isStationPlaying = false
    private lateinit var vibrator: Vibrator

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
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val radioStationUrl = intent?.getStringExtra("radioStationUrl")
        if (!radioStationUrl.isNullOrEmpty()) {
            startRadioStation(radioStationUrl)

            val notification = createNotification()
            startForeground(123, notification)

            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 1000, 1000) // Паттерн вибрации (включение на 1 секунду, выключение на 1 секунду, повтор)
            vibrator.vibrate(pattern, 0) // Запуск вибрации с заданным паттерном

        }
        return START_STICKY
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
        val duration = 195000
        val startVolume = 0.02f

        val valueAnimator = ValueAnimator.ofFloat(startVolume, maxVolume)
        valueAnimator.duration = duration.toLong()
        valueAnimator.addUpdateListener { animator ->
            val volume = animator.animatedValue as Float
            mediaPlayer.setVolume(volume, volume)
        }
        valueAnimator.start()
    }
    private fun createNotification(): Notification {
        val channelId = "mishkin"
        val channelName = "Alarm"
        val notificationIntent = Intent(this, StopAlarmActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Foreground Service")
            .setContentText("Radio is playing...")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

     fun stopForegroundNotification() {
        stopForeground(true)

        // stopSelf()
    }
    private fun vibrate() {
        val vibrationPattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    vibrationPattern,
                    0
                )
            )
        } else {
            vibrator.vibrate(vibrationPattern, 0)
        }
    }

    fun stopVibration() {
        vibrator.cancel()
    }
}
