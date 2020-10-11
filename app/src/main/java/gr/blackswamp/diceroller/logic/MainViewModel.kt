package gr.blackswamp.diceroller.logic

import android.app.Application
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.KoinComponent
import kotlin.coroutines.CoroutineContext

class MainViewModel(app: Application) : AndroidViewModel(app), KoinComponent, CoroutineScope {
    private val supervisor = SupervisorJob()
    override val coroutineContext: CoroutineContext = supervisor + Dispatchers.Main.immediate

    fun addRoll(die: Int) {

    }

    fun clearRoll(die: Int) {

    }

    fun saveSet() {

    }

    fun deleteSet() {

    }

    fun selectSet(id: Int) {

    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        supervisor.cancel("View model cleared")
    }
}