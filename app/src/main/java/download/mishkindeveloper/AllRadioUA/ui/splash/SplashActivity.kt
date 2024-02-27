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
import com.google.android.ump.*
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity


//@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var consentInformation: ConsentInformation
    private lateinit var consentForm: ConsentForm

    private var repeatImageButton: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        val params = ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                // The consent information state was updated.
                // You are now ready to check if a form is available.
                if (consentInformation.isConsentFormAvailable) {
                    loadForm()
                }
            },
            {
                // Handle the error.
            })

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initButton()
        startActivityOrShowToastsError()
    }

    private fun loadForm() {
        UserMessagingPlatform.loadConsentForm(
            this,
            {
                this.consentForm = it
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(
                        this,
                        ConsentForm.OnConsentFormDismissedListener {
                            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                                // App can start requesting ads.
                            }
                            loadForm()
                        }
                    )
                }
            },
            { errorCode ->
                // Handle the error.

            }
        )
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
            handler.postDelayed({ startMainActivity() }, 2000)
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