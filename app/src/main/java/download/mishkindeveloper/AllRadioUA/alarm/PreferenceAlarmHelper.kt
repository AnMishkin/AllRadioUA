package download.mishkindeveloper.AllRadioUA.alarm

import android.content.Context
import android.content.SharedPreferences

class PreferenceAlarmHelper(context: Context) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("Alarm", Context.MODE_PRIVATE)

    init {
        if (!sharedPrefs.contains("Alarm")) {
            saveBoolean("Alarm", false)
        }
    }
    fun saveBoolean(key: String, value: Boolean) {
        sharedPrefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPrefs.getBoolean(key, defaultValue)
    }

    fun isFirstRun(): Boolean {
        return sharedPrefs.getBoolean("isFirstRun", true)
    }

    fun setFirstRun(value: Boolean) {
        sharedPrefs.edit().putBoolean("isFirstRun", value).apply()
    }

    fun saveHour(key: String,value: Int ) {
        sharedPrefs.edit().putInt(key,value).apply()
    }

    fun saveMinute(key: String,value: Int ) {
        sharedPrefs.edit().putInt(key,value).apply()
    }
}

