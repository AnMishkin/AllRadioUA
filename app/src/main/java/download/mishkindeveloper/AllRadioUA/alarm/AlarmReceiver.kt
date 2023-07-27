package download.mishkindeveloper.AllRadioUA.alarm

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val radioStationUrl = intent.getStringExtra("radioStation")
        val serviceIntent = Intent(context, AlarmRadioPlayerService::class.java)
        serviceIntent.putExtra("radioStation", radioStationUrl)
        ContextCompat.startForegroundService(context, serviceIntent)
        // Start StopAlarmActivity when the alarm is received
        val stopAlarmIntent = Intent(context, StopAlarmActivity::class.java)
        stopAlarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(stopAlarmIntent)
    }

}
