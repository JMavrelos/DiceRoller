package gr.blackswamp.diceroller.app

import android.app.Application
import timber.log.Timber

class TestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(TestTree())
    }
}