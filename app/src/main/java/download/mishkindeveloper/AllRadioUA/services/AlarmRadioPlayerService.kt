package download.mishkindeveloper.AllRadioUA.services

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.alarm.StopAlarmActivity

class AlarmRadioPlayerService : Service() {
    private val binder = PlayerBinder()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator
    private var volumeHandler: Handler? = null
    private val maxVolume = 1.0f
    private val volumeIncrement = 0.1f
    private var targetVolume = 0.0f
    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class PlayerBinder : Binder() {
        fun getService(): AlarmRadioPlayerService {
            return this@AlarmRadioPlayerService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val radioStationUrl = intent?.getStringExtra("radioStation")
        if (radioStationUrl != null) {
            startForeground(NOTIFICATION_ID, createNotification())
            startRadioStation(radioStationUrl)
            startVibration()
        }
        return START_STICKY
    }

    fun startRadioStation(radioStationUrl: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(radioStationUrl)
            prepareAsync()
            setOnPreparedListener {
                // Start playing the sound with gradually increasing volume
                it.start()
                startVolumeIncrease()
            }
        }
    }

    fun stopRadioStation() {
        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        mediaPlayer = null
    }

    fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(1000, 1000), 0))
        } else {
            vibrator.vibrate(longArrayOf(1000, 1000), 0)
        }
    }

    fun stopVibration() {
        vibrator.cancel()
    }

    private fun createNotification(): Notification {
        val alarmTitle = applicationContext.getString(R.string.alarm_show_notification)
        val alarmText = applicationContext.getString(R.string.alarm_text_notification)
        val channelId = "alarm_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Alarm Channel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, StopAlarmActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Add FLAG_IMMUTABLE here
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(alarmTitle)
            .setContentText(alarmText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
    }


    companion object {
        private const val NOTIFICATION_ID = 1

        fun stopVibration(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
        }
    }


    private fun startVolumeIncrease() {
        val targetVolume = maxVolume
        mediaPlayer?.let { player ->
            player.setVolume(0f, 0f)
            player.start()

            val volumeSteps = 13 // Количество шагов до достижения целевой громкости
            val volumeStepDuration = 7000L // Продолжительность каждого шага (в миллисекундах)
            val volumeStepValue = targetVolume / volumeSteps

            volumeHandler = Handler(Looper.getMainLooper())
            var currentStep = 1

            volumeHandler?.postDelayed(object : Runnable {
                override fun run() {
                    if (currentStep <= volumeSteps) {
                        val newVolume = volumeStepValue * currentStep
                        player.setVolume(newVolume, newVolume)
                        currentStep++
                        volumeHandler?.postDelayed(this, volumeStepDuration)
                    } else {
                        player.setVolume(targetVolume, targetVolume)
                    }
                }
            }, volumeStepDuration)
        }
    }


    fun stopVolumeIncrease() {
        volumeHandler?.removeCallbacksAndMessages(null)
    }















}
