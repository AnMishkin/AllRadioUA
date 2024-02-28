package download.mishkindeveloper.AllRadioUA.updateAp

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.InstallStatus

class UpdateApp : BroadcastReceiver() {
    private val UPDATE_ACTION = "download.mishkindeveloper.AllRadioUA.UPDATE_ACTION"
    private lateinit var mAppUpdateManager: AppUpdateManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == UPDATE_ACTION) {
            // Отримано трансляцію про оновлення, викликайте вашу функціональність
            checkUpdateStatus(context)
        }
    }

    fun checkUpdateStatus(context: Context?) {
        mAppUpdateManager = AppUpdateManagerFactory.create(context!!)
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // Покажіть повідомлення про завершення оновлення
                showCompletedUpdate(context)
            }
        }
    }

    fun showCompletedUpdate(context: Context?) {
        val newAppIsReady = "New app is ready"
        val updateInstall = "Update Install"

        val snackbar = newAppIsReady?.let {
            Snackbar.make(
                (context as Activity).findViewById(android.R.id.content), it,
                Snackbar.LENGTH_INDEFINITE
            )
        }
        snackbar?.setAction(updateInstall) {
            context?.let { it1 -> AppUpdateManagerFactory.create(it1) }?.completeUpdate()
        }
        snackbar?.show()
    }
}
