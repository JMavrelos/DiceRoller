package gr.blackswamp.diceroller.app

import android.util.Log
import timber.log.Timber

class TestTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        println("${priority.print()} : $tag : $message ${t?.let { "($it)" } ?: ""}")
    }

    private fun Int.print(): String {
        return when (this) {
            Log.VERBOSE -> "Verbose"
            Log.DEBUG -> "Debug"
            Log.INFO -> "Info"
            Log.WARN -> "Warn"
            Log.ERROR -> "Error"
            Log.ASSERT -> "Assert"
            else -> "???"


        }
    }
}