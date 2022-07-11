package download.mishkindeveloper.AllRadioUA.di.modules

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {
    @Binds
    abstract fun bindViewModelFactory(modelProvider: ViewModelFactory?): ViewModelProvider.Factory?
}