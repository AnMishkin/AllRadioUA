package download.mishkindeveloper.AllRadioUA.di.modules

import download.mishkindeveloper.AllRadioUA.ui.favoriteFragment.FavoriteFragment
import download.mishkindeveloper.AllRadioUA.ui.historyFragment.HistoryFragment
import download.mishkindeveloper.AllRadioUA.ui.listFragment.ListFragment
import download.mishkindeveloper.AllRadioUA.ui.settingFragment.SettingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun listFragment(): ListFragment?

    @ContributesAndroidInjector
    abstract fun playerFragment(): SettingFragment?

    @ContributesAndroidInjector
    abstract fun favoriteFragment(): FavoriteFragment?

    @ContributesAndroidInjector
    abstract fun historyFragment():HistoryFragment?
}