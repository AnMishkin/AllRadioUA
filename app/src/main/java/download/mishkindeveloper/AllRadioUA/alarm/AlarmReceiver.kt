package download.mishkindeveloper.AllRadioUA.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.alarm.StopAlarmActivity
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val radioStationUrl = intent.getStringExtra("radioStation")

        // Создаем намерение для запуска активности при остановке будильника
        val stopAlarmIntent = Intent(context, StopAlarmActivity::class.java)
        stopAlarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Создаем PendingIntent для запуска активности
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
                .setContentTitle("Будильник")
                .setContentText("Просыпайся!")
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
                .setContentTitle("Будильник")
                .setContentText("Просыпайся!")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Устанавливаем PendingIntent
                .build()

            notificationManager.notify(123, notification)
        }

        // Запускаем активность для остановки будильника при нажатии на уведомление
        val pendingStopIntent = PendingIntent.getActivity(
            context,
            0,
            stopAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Запускаем сервис воспроизведения радио
        val serviceIntent = Intent(context, AlarmRadioPlayerService::class.java).apply {
            putExtra("radioStationUrl", radioStationUrl)
        }
        context.startService(serviceIntent)
    }
}
