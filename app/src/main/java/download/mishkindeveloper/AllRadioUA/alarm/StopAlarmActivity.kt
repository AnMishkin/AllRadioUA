package download.mishkindeveloper.AllRadioUA.alarm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ServiceCompat.stopForeground
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.databinding.ActivityStopAlarmBinding
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity

class StopAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStopAlarmBinding
    private var mPlayerService: AlarmRadioPlayerService? = null
    private var isServiceBound = false
    private lateinit var preferenceAlarmHelper: PreferenceAlarmHelper
    private var alertImageButton: ImageButton? = null
    private lateinit var mAdView: AdView
    private lateinit var vibrator : Vibrator

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

        val serviceIntent = Intent(this, AlarmRadioPlayerService::class.java)
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? AlarmRadioPlayerService.PlayerBinder
                val serviceInstance = binder?.getService()
                serviceInstance?.stopForegroundNotification()
                unbindService(this)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // Обработка отключения службы (если необходимо)
            }
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)



        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        binding = ActivityStopAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceAlarmHelper = PreferenceAlarmHelper(this)
        alertImageButton = findViewById(R.id.alertButton)
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        binding.imageButtonStopAlarm.setOnClickListener {
            stopAlarmDialog()
        }

        //vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

     fun stopRadioPlayback() {
        if (isServiceBound && mPlayerService != null) {
            mPlayerService?.stopRadioStation()
            mPlayerService?.stopForegroundNotification()
            mPlayerService?.stopVibration()
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


    fun stopAlarmDialog() {
        val yes  = resources.getText(R.string.update_yes)
        val no  = resources.getText(R.string.update_no)
        val title  = resources.getText(R.string.title_alarm_stop)
        val sure  = resources.getText(R.string.text_alarm_stop)
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("$yes"){ _, _ ->
            stopRadioPlayback()

            //vibrator.cancel() // Остановка вибрации

            preferenceAlarmHelper.saveBoolean("Alarm",false)
            alertImageButton?.setImageResource(R.drawable.baseline_add_alert_24)
            startMainActivity()
            finish()

        }
        builder.setNegativeButton("$no"){_, _ -> }
        builder.setTitle("$title")
        builder.setMessage("$sure")
        builder.create().show()
    }

    fun startMainActivity() {
        val context = this
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

}
