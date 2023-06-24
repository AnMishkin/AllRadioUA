package download.mishkindeveloper.AllRadioUA.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.helper.PreferenceHelper
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService
import download.mishkindeveloper.AllRadioUA.services.PlayerService

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var preferencesHelper: PreferenceHelper

    private var mPlayerService: PlayerService? = null
    private var mExoPlayer: ExoPlayer? = null
    private var isServiceBound = false
    private var activityContext: Context? = null

    private var myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            mPlayerService = (binder as PlayerService.PlayerBinder).getService()
            mExoPlayer = mPlayerService?.getPlayer()
            mPlayerService?.getRadioWave()?.id?.let { preferencesHelper.setIdPlayMedia(it) }

            isServiceBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mPlayerService = null
            mExoPlayer = null
            isServiceBound = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {

        val radioStationUrl = intent.getStringExtra("radioStation")

        val serviceIntent = Intent(context, AlarmRadioPlayerService::class.java).apply {
            putExtra("radioStationUrl", radioStationUrl)
        }
        val stopAlarmIntent = Intent(context, StopAlarmActivity::class.java)
        stopAlarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK


        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            stopAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = createNotification(context, pendingIntent)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent)
            val channelId = "mishkin"
            val channelName = "Alarm"
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager?.createNotificationChannel(channel)
            notificationManager?.notify(123, notification)
        } else {

            context.startService(serviceIntent)
            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notificationManager.notify(123, notification)
        }

        context.startActivity(stopAlarmIntent)
    }


    private fun createNotification(context: Context, pendingIntent: PendingIntent): Notification {
        val channelId = "mishkin"
        val channelName = "Alarm"
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )


            notificationManager?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Будильник")
            .setContentText("Просыпайся!")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Устанавливаем PendingIntent
            .build()
    }
}
