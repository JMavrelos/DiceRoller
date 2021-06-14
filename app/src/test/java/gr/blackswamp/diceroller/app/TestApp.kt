package gr.blackswamp.diceroller.app

import android.app.Application
import gr.blackswamp.diceroller.core.Logger

class TestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.enable(debug = true, testing = true, appName = "DieRollerTest")
    }
}