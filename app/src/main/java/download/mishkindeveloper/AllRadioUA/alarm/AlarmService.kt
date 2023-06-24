//package download.mishkindeveloper.AllRadioUA.alarm
//
//import android.animation.ValueAnimator
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.media.MediaPlayer
//import android.os.Build
//import android.os.IBinder
//import android.os.VibrationEffect
//import android.os.Vibrator
//
//class AlarmService : Service() {
//    private var mediaPlayer: MediaPlayer? = null
//    private var isStationPlaying = false
//    private lateinit var vibrator: Vibrator
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        mediaPlayer = MediaPlayer()
//        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        // Инициализация MediaPlayer и Vibrator
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val radioStationUrl = intent?.getStringExtra("radioStationUrl")
//        if (!radioStationUrl.isNullOrEmpty()) {
//            startRadioStation(radioStationUrl)
//            vibrate()
//        }
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        mediaPlayer?.stop()
//        mediaPlayer?.release()
//        mediaPlayer = null
//        super.onDestroy()
//    }
//
//    private fun startRadioStation(url: String) {
//        mediaPlayer = MediaPlayer()
//        mediaPlayer?.setDataSource(url)
//        mediaPlayer?.prepareAsync()
//
//        mediaPlayer?.setOnPreparedListener { mp ->
//            mp.start()
//            fadeVolumeIn(mediaPlayer!!)
//            isStationPlaying = true
//        }
//    }
//
//    private fun stopRadioStation() {
//        if (isStationPlaying && mediaPlayer?.isPlaying == true) {
//            mediaPlayer?.stop()
//        }
//        mediaPlayer?.reset()
//        isStationPlaying = false
//    }
//
//    private fun fadeVolumeIn(mediaPlayer: MediaPlayer) {
//        val maxVolume = 1.0f
//        val duration = 195000
//        val startVolume = 0.02f
//
//        val valueAnimator = ValueAnimator.ofFloat(startVolume, maxVolume)
//        valueAnimator.duration = duration.toLong()
//        valueAnimator.addUpdateListener { animator ->
//            val volume = animator.animatedValue as Float
//            mediaPlayer.setVolume(volume, volume)
//        }
//        valueAnimator.start()
//    }
//
//    private fun vibrate() {
//        val vibrationPattern = longArrayOf(0, 1000, 1000)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            vibrator.vibrate(
//                VibrationEffect.createWaveform(
//                    vibrationPattern,
//                    0
//                )
//            )
//        } else {
//            vibrator.vibrate(vibrationPattern, 0)
//        }
//    }
//}
