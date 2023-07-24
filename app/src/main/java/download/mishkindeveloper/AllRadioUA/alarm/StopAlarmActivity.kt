package download.mishkindeveloper.AllRadioUA.alarm

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.alarm.PreferenceAlarmHelper
import download.mishkindeveloper.AllRadioUA.databinding.ActivityStopAlarmBinding
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService
import download.mishkindeveloper.AllRadioUA.alarm.AlarmRadioPlayerServiceHelper
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity

class StopAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStopAlarmBinding
    private var mPlayerService: AlarmRadioPlayerService? = null
    private var isServiceBound = false
    private lateinit var preferenceAlarmHelper: PreferenceAlarmHelper
    private var alertImageButton: ImageButton? = null
    private lateinit var mAdView: AdView
    private var wakeLock: PowerManager.WakeLock? = null // Add a field for WakeLock

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

        // Убираем блокировку экрана и отображаем активити поверх других окон
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val serviceIntent = Intent(this, AlarmRadioPlayerService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Получаем WakeLock для удержания устройства включенным
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AllRadioUA::AlarmWakeLock")
        // Удерживаем устройство включенным на протяжении 24 часов (1 день)
        wakeLock?.acquire(24 * 60 * 60 * 1000L)

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

    private fun stopRadioPlayback() {
        if (isServiceBound && mPlayerService != null) {
            mPlayerService?.stopRadioStation()
            mPlayerService?.stopForegroundNotification()
            mPlayerService?.stopVibration()
        }
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
                mPlayerService?.stopRadioStation() // Остановить радиостанцию
                preferenceAlarmHelper.saveBoolean("Alarm", false)
                // Освободить WakeLock здесь (после остановки будильника)
                releaseWakeLock()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton(no) { dialog, _ ->
                // Освободить WakeLock, если пользователь решит не останавливать будильник
                releaseWakeLock()
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
            wakeLock = null
        }
    }

    override fun onResume() {
        super.onResume()
        val serviceIntent = Intent(this, AlarmRadioPlayerService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRadioPlayback()
        // Освобождаем WakeLock при уничтожении активити
        releaseWakeLock()
    }
}
