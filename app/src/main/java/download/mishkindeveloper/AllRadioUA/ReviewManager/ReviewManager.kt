package download.mishkindeveloper.AllRadioUA.ReviewManager

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import java.util.*

class ReviewManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ReviewPrefs", Context.MODE_PRIVATE)
    private lateinit var textReview : String
    private lateinit var laiterReview : String
    private lateinit var leaveReview : String
    private lateinit var okReview : String

    fun checkAndPromptForReview(textReview : String,laiterReview : String,leaveReview : String,okReview : String) {
        this.textReview = textReview
        this.laiterReview = laiterReview
        this.leaveReview = leaveReview
        this.okReview = okReview
        val appLaunchCount = sharedPreferences.getInt("appLaunchCount", 0)
        if (appLaunchCount >= 2) {
            val reviewLeft = sharedPreferences.getBoolean("reviewLeft", false)
            if (!reviewLeft) {
                showReviewPromptDialog(textReview,laiterReview,leaveReview,okReview)
            }
        }

        // Увеличиваем счетчик запусков приложения
        sharedPreferences.edit().putInt("appLaunchCount", appLaunchCount + 1).apply()
    }

    private fun showReviewPromptDialog(textReview : String,laiterReview : String,leaveReview : String,okReview : String) {
        this.textReview = textReview
        this.laiterReview = laiterReview
        this.leaveReview = leaveReview
        this.okReview = okReview

        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(leaveReview)
        alertDialogBuilder.setMessage(textReview)
        alertDialogBuilder.setPositiveButton(okReview) { _, _ ->
            // Обработка нажатия кнопки "Оставить отзыв"
            sharedPreferences.edit().putBoolean("reviewLeft", true).apply()

            // Перенаправление пользователя на страницу для написания отзыва в магазине приложений
            val appPackageName = context.packageName
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (e: ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }

        alertDialogBuilder.setNegativeButton(laiterReview) { _, _ ->
            // Обработка нажатия кнопки "Позже"
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, 3) // Добавляем 2 дня
            val remindTime = calendar.timeInMillis
            sharedPreferences.edit().putLong("remindTime", remindTime).apply()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
