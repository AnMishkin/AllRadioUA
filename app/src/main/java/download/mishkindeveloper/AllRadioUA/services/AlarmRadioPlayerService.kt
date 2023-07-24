package download.mishkindeveloper.AllRadioUA.services

import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.os.PowerManager.WakeLock
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.alarm.StopAlarmActivity

class AlarmRadioPlayerService : Service() {
    private var valueAnimator: ValueAnimator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isStationPlaying = false
    private var volume = 0.0f
    private var wakeLock: WakeLock? = null
    private var isServiceDestroyed = false

    inner class PlayerBinder : Binder() {
        fun getService(): AlarmRadioPlayerService = this@AlarmRadioPlayerService
    }

    private val playerBinder = PlayerBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return playerBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra("radioStationUrl") == true) {
            val radioStationUrl = intent.getStringExtra("radioStationUrl")
            radioStationUrl?.let { startRadioStationAndNotification(this, it) }
        }

        // Создаем канал уведомлений для службы в фоновом режиме (для Android Oreo и выше)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        // Возвращаем START_STICKY, чтобы служба продолжала работу в случае, если ее остановят
        return START_STICKY
    }

    private fun startRadioStation(url: String) {
        // При запуске радиостанции активируем WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AllRadioUA::AlarmWakeLock"
        )
        wakeLock?.acquire(24 * 60 * 60 * 1000L /* 24 часа */)

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(url)
        mediaPlayer?.prepareAsync()

        mediaPlayer?.setOnPreparedListener { mp ->
            if (!isServiceDestroyed) {
                mp.start()
                isStationPlaying = true // Устанавливаем флаг, что радиостанция начала воспроизведение

                fadeVolumeIn(mediaPlayer!!)
            } else {
                mp.release()
                mediaPlayer = null
            }
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

        valueAnimator = ValueAnimator.ofFloat(startVolume, maxVolume)
        valueAnimator?.duration = duration.toLong()
        valueAnimator?.addUpdateListener { animator ->
            if (isServiceDestroyed) {
                stopFadeVolumeAnimation()
                return@addUpdateListener
            }

            volume = animator.animatedValue as Float

            // Проверяем диапазон громкости
            if (volume < 0.0f) {
                volume = 0.0f
            } else if (volume > 1.0f) {
                volume = 1.0f
            }

            // Проверяем, что mediaPlayer не равен null перед вызовом setVolume
            mediaPlayer.setVolume(volume, volume)
        }
        valueAnimator?.start()
    }

    private fun stopFadeVolumeAnimation() {
        valueAnimator?.cancel()
        valueAnimator = null
    }

    fun stopForegroundNotification() {
        stopForeground(true)
    }

    fun stopVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }

    private fun startRadioStationAndNotification(context: Context, url: String) {
        startRadioStation(url)

        // Создаем PendingIntent для запуска StopAlarmActivity
        val stopAlarmIntent = Intent(context, StopAlarmActivity::class.java)
        stopAlarmIntent.putExtra("radioStation", url) // Передаем URL радиостанции в StopAlarmActivity
        stopAlarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            stopAlarmIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Получаем сервис уведомлений
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Проверяем версию Android для создания канала уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        // Создаем уведомление (для всех версий Android)
        val notification = NotificationCompat.Builder(context, "alarm_service_channel")
            .setContentTitle("Плеер радио будильника")
            .setContentText("Воспроизведение радиостанции...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Устанавливаем PendingIntent для запуска StopAlarmActivity
            .build()

        startForeground(123, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelId = "alarm_service_channel"
        val channelName = "Служба будильника"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Освобождаем WakeLock перед уничтожением службы
        wakeLock?.release()
        wakeLock = null

        isServiceDestroyed = true // Устанавливаем флаг, что служба будет уничтожена
        stopFadeVolumeAnimation() // Остановка анимации громкости

        super.onDestroy()
    }
}
