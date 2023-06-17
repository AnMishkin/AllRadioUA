//package download.mishkindeveloper.AllRadioUA.alarm
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import androidx.core.app.NotificationManagerCompat
//
//import com.google.android.exoplayer2.SimpleExoPlayer
//
//class StopReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        val sharedPreferences = context.getSharedPreferences("PlayerState", Context.MODE_PRIVATE)
//        val isPlaying = sharedPreferences.getBoolean("isPlaying", false)
//        // Другие данные состояния плееров, если необходимо
//
//        if (isPlaying) {
//            // Остановите воспроизведение радиостанции здесь
//            val exoPlayer = SimpleExoPlayer.Builder(context).build()
//            exoPlayer.stop()
//        }
//
//        // Отмените уведомление
//        val notificationManager = NotificationManagerCompat.from(context)
//        notificationManager.cancel(123)
//    }
//}
//
//
//
