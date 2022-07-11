package download.mishkindeveloper.AllRadioUA.services

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import download.mishkindeveloper.AllRadioUA.R


class TimerService : Service() {
   private var intentFilter ="count_down"
   private var timerIntent = Intent(intentFilter)
   private var countDownTimer: CountDownTimer? = null
   private var minutes: String? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        countDownTimer!!.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        minutes = intent?.getStringExtra(getString(R.string.serializable_extra_min))
        countDownTimer = object : CountDownTimer(minutes?.toLong()!! * 60000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerIntent.putExtra(getString(R.string.serializable_extra_long), millisUntilFinished)
                sendBroadcast(timerIntent)
            }

            override fun onFinish() {
                stopService(Intent(this@TimerService, PlayerService::class.java))
                stopService(Intent(this@TimerService, TimerService::class.java))
            }
        }
        (countDownTimer as CountDownTimer).start()
        return START_STICKY
    }
}