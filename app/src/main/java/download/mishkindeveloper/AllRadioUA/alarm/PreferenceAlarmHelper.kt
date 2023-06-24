package download.mishkindeveloper.AllRadioUA.alarm

import android.content.Context
import android.content.SharedPreferences

class PreferenceAlarmHelper(context: Context) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("Alarm", Context.MODE_PRIVATE)

    fun saveBoolean(key: String, value: Boolean) {
        sharedPrefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPrefs.getBoolean(key, defaultValue)
    }
}

