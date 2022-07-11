package download.mishkindeveloper.AllRadioUA.di.modules

import androidx.lifecycle.ViewModel
import download.mishkindeveloper.AllRadioUA.di.keys.ViewModelKey
import download.mishkindeveloper.AllRadioUA.ui.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel
}