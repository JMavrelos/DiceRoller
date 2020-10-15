package gr.blackswamp.diceroller.logic

import android.app.Application
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.data.repos.HomeRepository
import gr.blackswamp.diceroller.ui.commands.HomeCommand
import gr.blackswamp.diceroller.ui.model.Die
import gr.blackswamp.diceroller.ui.model.DieSetHeader
import gr.blackswamp.diceroller.ui.model.HomeFragmentState
import gr.blackswamp.diceroller.ui.model.Roll
import gr.blackswamp.diceroller.util.LiveEvent
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.coroutines.CoroutineContext

class HomeViewModel(app: Application, private val parent: FragmentParent) : AndroidViewModel(app), KoinComponent, CoroutineScope {
    private val supervisor = SupervisorJob()
    override val coroutineContext: CoroutineContext = supervisor + Dispatchers.Main.immediate
    private val repo by inject<HomeRepository>()

    //<editor-fold desc="variables that represent the current state">
    val sets: LiveData<List<DieSetHeader>> = repo.getSets().map { it }
    private val rolls = mutableListOf<Roll>()
    private var editing = false
    private var set: DieSetData? = null
    private var _state = MutableLiveData(HomeFragmentState())
    private var _command = LiveEvent<HomeCommand>()
    //</editor-fold>

    val state: LiveData<HomeFragmentState> = _state
    val command: LiveData<HomeCommand> = _command

    fun roll(die: Die) {
        val set = _state.value?.set as? DieSetData
        if (set == null) {
            updateRolls(RollData(die, repo.generateValue(die)))
            updateState()
        }
    }

    fun rollSet(id: UUID) {
        if (set != null)
            return
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
            launch {
                val nextId = repo.getNextAvailableId()
                _command.postValue(HomeCommand.ShowNameDialog(nextId))
            }
        } else {
            saveSet(current)
        }
    }

    fun nameSelected(name: String) {
        val current = this.set
        if (current != null) {
            parent.showError(R.string.error_create_set)
        } else {
            newSet(name)
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

//    fun clearRolls() {
//        updateRolls()
//        updateState()
//    }

    //<editor-fold desc="private functions">
    private fun saveSet(set: DieSetData) {
        launch {
            val response = repo.saveSet(set)
            if (response.isFailure) {
                //todo:handle error
            } else {
                this@HomeViewModel.set = null
                editing = false
                updateState()
            }
        }
    }

    private fun newSet(name: String) {
        set = repo.buildNewSet(name)
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
                this@HomeViewModel.set = null
                this@HomeViewModel.editing = false
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
        val calculated = newRolls.toList().flatMap {
            when {
                it.die != Die.Mod -> listOf(Roll.Result(it.die, it.value), Roll.Modifier("+"))
                it.value > 0 -> listOf(Roll.Modifier(it.value.toString()), Roll.Modifier("+"))
                else -> listOf()
            }
        }.toMutableList()

        when (calculated.size) {
            0 -> calculated.clear() //no rolls were added
            2 -> calculated.removeAt(1) //remove the modifier
            else -> {
                calculated.removeAt(calculated.size - 1)
                calculated.add(Roll.Modifier("="))
                calculated.add(Roll.Modifier(newRolls.sumBy { it.value }.toString()))
            }
        }

        synchronized(rolls) {
            rolls.clear()
            rolls.addAll(calculated)
        }
    }

    private fun updateState() {
        _state.postValue(
            HomeFragmentState(rolls = this.rolls, set = this.set, editing = this.editing)
        )
    }
    //</editor-fold>

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        supervisor.cancel("View model cleared")
    }

}