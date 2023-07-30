package download.mishkindeveloper.AllRadioUA.alarm

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import download.mishkindeveloper.AllRadioUA.R

import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService
import download.mishkindeveloper.AllRadioUA.alarm.AlarmRadioPlayerServiceHelper
import download.mishkindeveloper.AllRadioUA.databinding.ActivityStopAlarmBinding
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity

class StopAlarmActivity : AppCompatActivity() {
    private var mPlayerService: AlarmRadioPlayerService? = null
    private lateinit var mAdView: AdView
    private lateinit var preferenceAlarmHelper: PreferenceAlarmHelper
    private var isServiceBound = false
    private lateinit var binding: ActivityStopAlarmBinding
    private lateinit var alarmRadioPlayerServiceHelper: AlarmRadioPlayerServiceHelper

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

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        val serviceIntent = Intent(this, AlarmRadioPlayerService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)


        binding = ActivityStopAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceAlarmHelper = PreferenceAlarmHelper(this)
        alarmRadioPlayerServiceHelper = AlarmRadioPlayerServiceHelper(this)

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        binding.imageButtonStopAlarm.setOnClickListener {
            stopAlarmDialog()

            val serviceIntent = Intent(this, AlarmRadioPlayerService::class.java)
            stopService(serviceIntent)


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unbind from the service
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        mPlayerService?.stopVibration()
        mPlayerService?.stopRadioStation()
        mPlayerService?.stopVolumeIncrease()
        mPlayerService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }

    private fun stopAlarmDialog() {
        val yes = resources.getText(R.string.update_yes)
        val no = resources.getText(R.string.update_no)
        val title = resources.getText(R.string.title_alarm_stop)
        val sure = resources.getText(R.string.text_alarm_stop)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(sure)
            .setPositiveButton(yes) { _, _ ->
                mPlayerService?.stopVolumeIncrease()
                mPlayerService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                AlarmRadioPlayerService.stopVibration(this)
                mPlayerService?.stopRadioStation()
                preferenceAlarmHelper.saveBoolean("Alarm", false)
Log.d("PREF","${preferenceAlarmHelper.getBoolean("Alarm",false)}")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton(no) { dialog, _ ->
                // Освободить WakeLock, если пользователь решит не останавливать будильник

                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }
}

