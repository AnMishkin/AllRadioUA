package download.mishkindeveloper.AllRadioUA.alarm

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class AlarmRadioPlayerServiceHelper(private val context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(1000, 1000), 0))
        } else {
            vibrator.vibrate(longArrayOf(1000, 1000), 0)
        }
    }

    fun stopVibration() {
        vibrator.cancel()
    }
}
