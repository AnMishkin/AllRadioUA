package download.mishkindeveloper.AllRadioUA.di

import android.app.Application
import download.mishkindeveloper.AllRadioUA.helper.PreferenceHelper
import download.mishkindeveloper.AllRadioUA.di.modules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        ActivityModules::class,
        FragmentBuildersModule::class,
        ViewModelFactoryModule::class,
        ViewModelModule::class,
        ServiceBuilderModule::class]
)

interface AppComponent : AndroidInjector<App?> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application?): Builder?
        fun build(): AppComponent?
    }

    override fun inject(app: App?)

    fun preferenceHelper(): PreferenceHelper
}