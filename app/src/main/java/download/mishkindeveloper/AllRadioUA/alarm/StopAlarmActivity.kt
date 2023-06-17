package download.mishkindeveloper.AllRadioUA.alarm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import download.mishkindeveloper.AllRadioUA.databinding.ActivityStopAlarmBinding
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService

class StopAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStopAlarmBinding
    private var mPlayerService: AlarmRadioPlayerService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? AlarmRadioPlayerService.PlayerBinder
            mPlayerService = binder?.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mPlayerService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageButtonStopAlarm.setOnClickListener {
            stopRadioPlayback()
            Log.d("MyLog", "Остановился будильник")
            Toast.makeText(this.applicationContext,"Будильник СТОП", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun stopRadioPlayback() {
        if (isServiceBound) {
            mPlayerService?.stopRadioStation()
        }
    }


    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, AlarmRadioPlayerService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}
