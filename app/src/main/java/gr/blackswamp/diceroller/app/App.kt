package gr.blackswamp.diceroller.app

import android.app.Application
import gr.blackswamp.diceroller.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        //<editor-fold desc="set up injections">
        startKoin {
            if (BuildConfig.DEBUG)
                androidLogger(Level.ERROR)
            androidContext(this@App)
            modules(applicationModule)
        }
        //</editor-fold>
    }
}