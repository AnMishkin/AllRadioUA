package download.mishkindeveloper.AllRadioUA.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.fragment.app.DialogFragment
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import java.util.*


abstract class RadioStationDialog(private val alarmHour: Int, private val alarmMinute: Int) : DialogFragment(), RadioStationAdapter.RadioStationSelectedListener {
    // ...

    override fun onRadioStationSelected(radioWave: RadioWave) {
        // Вызывается при выборе радиостанции
        val alarmIntent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("radioStation", radioWave) // Передача URL выбранной радиостанции в AlarmReceiver
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, alarmHour)
        calendar.set(Calendar.MINUTE, alarmMinute)
        calendar.set(Calendar.SECOND, 0)

        // Получите AlarmManager
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Установите повторяющийся будильник с заданным временем
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        dismiss()
    }
}

