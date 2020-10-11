package gr.blackswamp.diceroller.logic

import android.app.Application
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
        } else {
            this.set = set.copy(
                dice = set.dice.toMutableMap().apply {
                    this[die] = (this[die] ?: 0) + 1
                }
            )
            updateState()
        }
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

    fun clear() {
        val set = _state.value?.set as? DieSetData ?: return
        if (repo.exists(set)) {
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
        } else {
            this.set = null
            this.editing = false
            updateState()
        }
    }

    fun action() {
        val current = set
        if (current == null) {
            set = repo.buildNewSet()
            editing = false
            updateState()
        } else {
            launch {
                val response = repo.saveSet(current)
                if (response.isFailure) {
                    //todo:handle error
                } else {
                    set = response.getOrNull()
                    editing = true
                    updateState()
                }
            }

        }
    }

    fun selectSet(id: UUID) {
        val current = this.set
        launch {
            val response = repo.getSet(id)
            if (response.isFailure) {
                //todo:handle error
            } else {
                val set = if (current?.id != id) {
                    val set = response.getOrNull() ?: return@launch
                    this@MainViewModel.set = set
                    set
                } else {
                    current
                }
                val rolls = repo.generateRolls(set)
                updateRolls(*rolls.toTypedArray())
                editing = true
                updateState()
            }

        }
    }

    fun clearRolls() {
        rolls.clear()
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

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        supervisor.cancel("View model cleared")
    }
}