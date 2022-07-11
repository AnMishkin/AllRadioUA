package download.mishkindeveloper.AllRadioUA.di.modules

import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModules {
    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity?
}