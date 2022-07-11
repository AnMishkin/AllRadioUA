package download.mishkindeveloper.AllRadioUA.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private var repeatImageButton: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initButton()
        startActivityOrShowToastsError()
    }

    private fun checkNetworkConnection(): Boolean {
        val connectivityManager =
            getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        val currentNetwork: Network? = connectivityManager.activeNetwork
        return currentNetwork != null
    }

    private fun startActivityOrShowToastsError() {
        if (checkNetworkConnection()) {
            val handler = Handler()
            handler.postDelayed({ startMainActivity() }, 1000)
        } else {
            repeatImageButton?.visibility = View.VISIBLE
            Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initButton() {
        repeatImageButton = findViewById(R.id.imageButtonRepeat)
        repeatImageButton?.setOnClickListener {
            recreateActivity()
        }
    }

    private fun recreateActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }
}