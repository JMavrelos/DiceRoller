package gr.blackswamp.diceroller.logic

import android.app.Application
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import gr.blackswamp.diceroller.data.repos.MainRepository
import gr.blackswamp.diceroller.ui.Die
import gr.blackswamp.diceroller.ui.DieSetHeader
import gr.blackswamp.diceroller.ui.MainActivityState
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainViewModel(app: Application) : AndroidViewModel(app), KoinComponent, CoroutineScope {
    private val supervisor = SupervisorJob()
    override val coroutineContext: CoroutineContext = supervisor + Dispatchers.Main.immediate
    private val repo by inject<MainRepository>()

    //<editor-fold desc="variables that represent the current state">
    val sets: LiveData<List<DieSetHeader>> = repo.getSets().map { it }
    private val rolls = mutableListOf<RollData>()
    private var editing = false
    private var set: DieSetData? = null
    //</editor-fold>

    private var _state = MutableLiveData(MainActivityState())
    val state: LiveData<MainActivityState> = _state


    fun roll(die: Die) {
        val set = _state.value?.set as? DieSetData
        if (set == null) {
            updateRolls(RollData(die, repo.generateValue(die)))
            updateState()
        }
    }

    fun rollSet(id: UUID) {
        launch {
            val response = repo.getSet(id)
            if (response.isFailure) {
                //todo:handle error
            } else {
                val rollSet = response.getOrNull() ?: return@launch
                val rolls = repo.generateRolls(rollSet)
                updateRolls(*rolls.toTypedArray())
                updateState()
            }
        }
    }

    fun editSet(id: UUID) {
        launch {
            val response = repo.getSet(id)
            if (response.isFailure) {
                //todo:handle error
            } else {
                set = response.getOrNull()
                updateRolls()
                editing = true
                updateState()
            }
        }
    }

    fun action1() {
        val current = this.set
        if (current == null) {
            newSet()
        } else {
            saveSet(current)
        }
    }

    fun action2() {
        val current = this.set
        if (current != null) {
            if (repo.exists(current)) {
                deleteSet(current)
            } else {
                cancelEdit()
            }
        }
    }

    fun action3() {
        val current = this.set
        if (current != null && repo.exists(current)) {
            cancelEdit()
        }
    }

    fun change(die: Die, increase: Boolean) {
        val current = set ?: return
        this.set = current.copy(
            dice = current.dice.toMutableMap().apply {
                this[die] = (this[die] ?: 0) + (if (increase) 1 else -1)
            }
        )
        updateState()

    }

    fun clear(die: Die) {
        val set = _state.value?.set as? DieSetData ?: return
        this.set = set.copy(
            dice = set.dice.toMutableMap().apply {
                this[die] = 0
            }
        )
        updateState()
    }

    fun pleaseHelpMe() {
        Toast.makeText(getApplication(), "Please Help", Toast.LENGTH_SHORT).show()
    }

    fun clearRolls() {
        rolls.clear()
        updateState()
    }

    //<editor-fold desc="private functions">
    private fun saveSet(set: DieSetData) {
        launch {
            val response = repo.saveSet(set)
            if (response.isFailure) {
                //todo:handle error
            } else {
                this@MainViewModel.set = null
                editing = false
                updateState()
            }
        }
    }

    private fun newSet() {
        set = repo.buildNewSet()
        updateRolls()
        editing = false
        updateState()
    }

    private fun deleteSet(set: DieSetData) {
        launch {
            val response = repo.delete(set)
            if (response.isFailure) {
                //todo:handle error
            } else {
                this@MainViewModel.set = null
                this@MainViewModel.editing = false
                updateState()
            }
        }
    }

    private fun cancelEdit() {
        this.set = null
        this.editing = false
        updateState()
    }

    private fun updateRolls(vararg newRolls: RollData) {
        synchronized(rolls) {
            rolls.clear()
            rolls.addAll(newRolls.toList())
        }
    }

    private fun updateState() {
        _state.postValue(
            MainActivityState(rolls = this.rolls, set = this.set, editing = this.editing)
        )
    }
    //</editor-fold>

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        supervisor.cancel("View model cleared")
    }

}