package download.mishkindeveloper.AllRadioUA.helper

import android.annotation.SuppressLint
import android.content.SharedPreferences
import download.mishkindeveloper.AllRadioUA.enums.DisplayListType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceHelper @Inject constructor(private var sharedPreferences: SharedPreferences) {
    fun getIdPlayMedia(): Int {
        return sharedPreferences.getInt("id", 1)
    }

    fun setIdPlayMedia(id: Int?) {
        if (id != null) {
            sharedPreferences.edit().putInt("id", id).apply()
        }
    }

    fun setSwitchEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("switch_status", enabled).apply()
    }

    fun getSwitchEnabled(): Boolean {
        return sharedPreferences.getBoolean("switch_status", false)
    }

    fun getFirstStart(): Boolean {
        return sharedPreferences.getBoolean("firstStart", true)
    }

    @SuppressLint("CommitPrefEdits")
    fun setFirstStart(status: Boolean) {
        sharedPreferences.edit().putBoolean("firstStart", status).apply()
    }

    fun getDefaultSortStatus(): Boolean {
        return sharedPreferences.getBoolean("default_radio", true)
    }

    fun setDefaultSortStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean("default_radio", status).apply()
    }

    fun getSortAscStatus(): Boolean {
        return sharedPreferences.getBoolean("asc_radio", false)
    }

    fun setSortAscStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean("asc_radio", status).apply()
    }

    fun getSortDescStatus(): Boolean {
        return sharedPreferences.getBoolean("desc_radio", false)
    }

    fun setSortDescStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean("desc_radio", status).apply()
    }

    fun getSortPopularStatus(): Boolean {
        return sharedPreferences.getBoolean("popular_radio", false)
    }

    fun setSortPopularStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean("popular_radio", status).apply()
    }

    fun getSortNotPopularStatus(): Boolean {
        return sharedPreferences.getBoolean("not_popular_radio", false)
    }

    fun setSortNotPopularStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean("not_popular_radio", status).apply()
    }

    fun getDisplayListType(): DisplayListType {
        return if (sharedPreferences.getBoolean("display_type", true)) {
            DisplayListType.List
        } else {
            DisplayListType.Grid
        }
    }

    fun setDisplayListType(displayListType: DisplayListType) {
        if (displayListType == DisplayListType.List) {
            sharedPreferences.edit().putBoolean("display_type", true).apply()
        } else {
            sharedPreferences.edit().putBoolean("display_type", false).apply()
        }
    }
}