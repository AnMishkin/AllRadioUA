package download.mishkindeveloper.AllRadioUA.updateAp

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class UpdateApp : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_REPLACED) {
            showUpdateReadyDialog(context)
        }
    }

    private fun showUpdateReadyDialog(context: Context?) {
        Log.d("UpdateApp", "Оновлення встановлено. Показуємо діалог.")

        AlertDialog.Builder(context)
            .setMessage("Оновлення готове до встановлення")
            .setPositiveButton("Встановити") { dialog, _ ->
                installUpdate(context)
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun installUpdate(context: Context?) {
        Log.d("UpdateApp", "Встановлення оновлення...")

        // Запустіть інтент для встановлення оновлення
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=${context?.packageName}")
        context?.startActivity(intent)
    }

    companion object {
        fun sendUpdateBroadcast(context: Context) {
            Log.d("UpdateApp", "Відправлення broadcast про підготовку оновлення.")

            val updateIntent = Intent(Intent.ACTION_PACKAGE_REPLACED)
            updateIntent.data = Uri.parse("package:" + context.packageName)
            LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
        }
    }
}
