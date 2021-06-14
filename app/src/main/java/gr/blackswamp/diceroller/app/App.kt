package gr.blackswamp.diceroller.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import gr.blackswamp.diceroller.BuildConfig
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.core.Logger
import javax.inject.Inject

@HiltAndroidApp
class App @Inject constructor() : Application() {
    override fun onCreate() {
        super.onCreate()
        //<editor-fold desc="set up injections">
        Logger.enable(debug = BuildConfig.DEBUG, appName = getString(R.string.app_name))
        Logger.setContext { applicationContext }
        //</editor-fold>
    }
}