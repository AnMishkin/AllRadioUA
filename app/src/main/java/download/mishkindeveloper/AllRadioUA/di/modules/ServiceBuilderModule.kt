package download.mishkindeveloper.AllRadioUA.di.modules

import download.mishkindeveloper.AllRadioUA.services.PlayerService
import download.mishkindeveloper.AllRadioUA.services.TimerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun bindPlayerService(): PlayerService?

    @ContributesAndroidInjector
    abstract fun bindTimerService(): TimerService?
}