package download.mishkindeveloper.AllRadioUA.services

import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.alarm.StopAlarmActivity
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity
import java.util.*


class AlarmRadioPlayerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isStationPlaying = false
    private var volume = 0.0f
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

            val notification = createNotification()
            startForeground(123, notification)

            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 1000, 1000) // Паттерн вибрации (включение на 1 секунду, выключение на 1 секунду, повтор)
            vibrator.vibrate(pattern, 0) // Запуск вибрации с заданным паттерном

        }
        return START_STICKY_COMPATIBILITY
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
            isStationPlaying = true // Установка флага, что станция начала воспроизводиться

            fadeVolumeIn(mediaPlayer!!)
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
        val duration = 190000
        val startVolume = 0.01f

        val valueAnimator = ValueAnimator.ofFloat(startVolume, maxVolume)
        valueAnimator.duration = duration.toLong()
        valueAnimator.addUpdateListener { animator ->
            volume = animator.animatedValue as Float

            // Проверка диапазона громкости
            if (volume < 0.0f) {
                volume = 0.0f
            } else if (volume > 1.0f) {
                volume = 1.0f
            }

            mediaPlayer.setVolume(volume, volume)
        }
        valueAnimator.start()
    }


    private fun createNotification(): Notification {
        val channelId = "mishkin"
        val channelName = "Alarm"
        var alarmTitle = applicationContext.getString(R.string.alarm_show_notification)
        var alarmText = applicationContext.getString(R.string.alarm_text_notification)
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
            .setContentTitle(alarmTitle)
            .setContentText(alarmText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

     fun stopForegroundNotification() {
        stopForeground(true)

        // stopSelf()
    }

    fun stopVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }


}
