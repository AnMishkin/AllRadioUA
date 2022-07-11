package download.mishkindeveloper.AllRadioUA.di

import android.app.Service
import androidx.fragment.app.Fragment

import download.mishkindeveloper.AllRadioUA.BuildConfig
import download.mishkindeveloper.AllRadioUA.helper.PreferenceHelper
import dagger.android.*
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class App : DaggerApplication(), HasActivityInjector,
    HasSupportFragmentInjector, HasServiceInjector {


    @set:Inject
    internal lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @set:Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @set:Inject
    lateinit var preferencesHelper: PreferenceHelper

    override fun applicationInjector(): AppComponent {
        val appComponent: AppComponent =
            download.mishkindeveloper.AllRadioUA.di.DaggerAppComponent.builder().application(this)?.build()!!
        appComponent.inject(this)
        return appComponent
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }



    override fun serviceInjector(): DispatchingAndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    companion object {
        /**
         * @see BuildConfig.VERSION_NAME
         *
         */
        @Deprecated("")
        val VERSION = download.mishkindeveloper.AllRadioUA.BuildConfig.VERSION_NAME

        /**
         * Return current application
         *
         * @return current application instance
         */
        var instance: App? = null
            private set
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }
}