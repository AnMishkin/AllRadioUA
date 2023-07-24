package download.mishkindeveloper.AllRadioUA.alarm

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService

object AlarmRadioPlayerServiceHelper {
    private var alarmRadioPlayerService: AlarmRadioPlayerService? = null

    fun startRadioStation(context: Context, url: String) {
        // Здесь запускаем радиостанцию, как вы делали ранее, и сохраняем ссылку на экземпляр службы
        alarmRadioPlayerService = AlarmRadioPlayerService()
        val serviceIntent = Intent(context, AlarmRadioPlayerService::class.java).apply {
            putExtra("radioStationUrl", url)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun stopRadioStation(context: Context) {
        // Здесь останавливаем радиостанцию и убиваем службу
        alarmRadioPlayerService?.let {
            it.stopRadioStation()
            it.stopForegroundNotification()
            it.stopVibration()
        }
        alarmRadioPlayerService = null
    }
}
