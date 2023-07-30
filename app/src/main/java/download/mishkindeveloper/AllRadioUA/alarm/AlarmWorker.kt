package download.mishkindeveloper.AllRadioUA.alarm

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService

class AlarmWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val radioStationUrl = inputData.getString("radioStation")
        if (radioStationUrl != null) {
            val serviceIntent = Intent(applicationContext, AlarmRadioPlayerService::class.java)
            serviceIntent.putExtra("radioStation", radioStationUrl)
            applicationContext.startService(serviceIntent)
        }
        return Result.success()
    }
}
