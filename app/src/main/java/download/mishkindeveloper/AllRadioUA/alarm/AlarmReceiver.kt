package download.mishkindeveloper.AllRadioUA.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService

class AlarmReceiver : BroadcastReceiver() {
    private var mPlayerService: AlarmRadioPlayerService? = null
    private var service: IBinder? = null
    override fun onReceive(context: Context, intent: Intent) {
        val binder = service as? AlarmRadioPlayerService.PlayerBinder
        mPlayerService = binder?.getService()
        val radioStationUrl = intent.getStringExtra("radioStation")
        // Запускаем радиостанцию и уведомление при срабатывании будильника
        startRadioStationAndNotification(context, radioStationUrl!!)
    }

    private fun startRadioStationAndNotification(context: Context, url: String) {
        AlarmRadioPlayerServiceHelper.startRadioStation(context, url)
        val alarmTitle = context.getString(R.string.alarm_show_notification)
        val alarmText = context.getString(R.string.alarm_text_notification)

        val stopAlarmIntent = Intent(context, StopAlarmActivity::class.java)
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
            val channelId = "mishkin"
            val channelName = "Alarm"

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)

            // Создаем уведомление
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(alarmTitle)
                .setContentText(alarmText)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Устанавливаем PendingIntent
                .build()

            notificationManager.notify(123, notification)

        } else {
            // Создаем уведомление (для более ранних версий Android)
            val notification = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(alarmTitle)
                .setContentText(alarmText)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Устанавливаем PendingIntent
                .build()

            notificationManager.notify(123, notification)

        }
        // Здесь вы можете добавить код для отображения уведомления при срабатывании будильника
    }
}
