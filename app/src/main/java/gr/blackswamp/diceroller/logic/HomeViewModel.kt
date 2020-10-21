package gr.blackswamp.diceroller.logic

import android.app.Application
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Suppress("PropertyName")
    internal val _state = MutableLiveData(HomeFragmentState())
    private var _command = LiveEvent<HomeCommand>()
    //</editor-fold>

    //<editor-fold desc="Live Data">
    val state: LiveData<HomeFragmentState> = _state
    val command: LiveData<HomeCommand> = _command
    //</editor-fold>

    fun roll(die: Die) {
        val set = _state.value?.set as? DieSetData
        if (set == null) {
            launch {
                val calculated = listOf(RollData(die, repo.generateValue(die))).toRolls()
                updateRolls(calculated)
                updateState()
            }
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
                val calculated = rolls.toRolls(rollSet.modifier)
                updateRolls(calculated)
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
                updateRolls(listOf())
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
            launch {
                if (repo.exists(current)) {
                    deleteSet(current)
                } else {
                    cancelEdit()
                }
            }
        }
    }

    fun action3() {
        val current = this.set
        launch {
            if (current != null && repo.exists(current)) {
                cancelEdit()
            }
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
        _command.postValue(HomeCommand.ShowHelp)
        updateState()
    }

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
        updateRolls(listOf())
        editing = false
        updateState()
    }

    private suspend fun deleteSet(set: DieSetData) {
        val response = repo.delete(set)
        if (response.isFailure) {
            //todo:handle error
        } else {
            this@HomeViewModel.set = null
            this@HomeViewModel.editing = false
            updateState()
        }
    }

    private fun cancelEdit() {
        this.set = null
        this.editing = false
        updateState()
    }

    private fun List<RollData>.toRolls(modifier: Int = 0): List<Roll> = transform(this, modifier)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun transform(rolls: List<RollData>, modifier: Int = 0): List<Roll> {
        var first = true
        val calculated = rolls.flatMap {
            val reply = mutableListOf<Roll>()
            if (first)
                first = false
            else
                reply.add(Roll.Text("+"))

            reply.add(Roll.Result(it.die, it.value))
            if (it.die == Die.D100) {
                reply.add(Roll.Text("%"))
            }
            reply
        }.toMutableList()

        if (modifier > 0 && calculated.size > 0)
            calculated.addAll(listOf(Roll.Text("+"), Roll.Text(modifier.toString())))

        if (calculated.count { it is Roll.Text && it.text == "+" } > 0)
            calculated.addAll(listOf(Roll.Text("="), Roll.Text((rolls.sumBy { it.value } + modifier).toString())))

        return calculated
    }

    private fun updateRolls(rolls: List<Roll>) {
        synchronized(this.rolls) {
            this.rolls.clear()
            this.rolls.addAll(rolls)
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