package download.mishkindeveloper.AllRadioUA.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import download.mishkindeveloper.AllRadioUA.data.AppDataBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Provides
    @Singleton
    fun provideContext(application: Application?): Context? {
        return application
    }


    @Provides
    @Singleton
    fun provideSharedPreference(context: Context?): SharedPreferences {
        return context?.getSharedPreferences("app", Context.MODE_PRIVATE)!!
    }

    @Provides
    @Singleton
    fun providesAppDatabase(context: Context?): AppDataBase {
        return AppDataBase.getDatabase(context)!!
    }


}