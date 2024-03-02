package download.mishkindeveloper.AllRadioUA.services

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import download.mishkindeveloper.AllRadioUA.R

class TimerService : Service() {

    private val intentFilter = "count_down"
    private val timerIntent = Intent(intentFilter)
    private var countDownTimer: CountDownTimer? = null
    private var minutes: String? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d("TimerService", "onDestroy")
        countDownTimer?.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        minutes = intent?.getStringExtra(getString(R.string.serializable_extra_min))
        Log.d("TimerService", "Minutes from intent: $minutes")

        if (minutes.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        countDownTimer = object : CountDownTimer(minutes!!.toLong() * 60000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("TimerService", "onTick: $millisUntilFinished")
                timerIntent.putExtra(getString(R.string.serializable_extra_long), millisUntilFinished)
                sendBroadcast(timerIntent)
            }


            override fun onFinish() {
                stopService(Intent(this@TimerService, PlayerService::class.java))
                countDownTimer?.cancel()
                stopSelf(startId)
            }

        }

        countDownTimer?.start()
        return START_STICKY
    }
}
