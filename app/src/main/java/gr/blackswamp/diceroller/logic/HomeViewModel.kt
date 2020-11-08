package gr.blackswamp.diceroller.logic

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import gr.blackswamp.diceroller.data.repos.HomeRepository
import gr.blackswamp.diceroller.data.repos.Reply
import gr.blackswamp.diceroller.ui.model.*
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class HomeViewModel(app: Application) : AndroidViewModel(app), KoinComponent {
    private val repo by inject<HomeRepository>()

    @VisibleForTesting
    internal val privateState = MutableLiveData<HomeState>()
    private val privateEffect = MutableLiveData<HomeEffect>()

    //<editor-fold desc="live data the fragment can observe">
    val sets: LiveData<List<DieSetHeader>> = repo.getSets().map { it }
    val state: LiveData<HomeState> = privateState
    val effect: LiveData<HomeEffect> = privateEffect
    //</editor-fold>

    init {
        privateState.value = HomeState.Viewing()
    }


    /**
     * this processes all events from the home fragment
     */
    fun process(event: HomeEvent) {
        when (event) {
            is HomeEvent.Roll -> roll(event.die)
            is HomeEvent.RollSet -> rollSet(event.id)
            is HomeEvent.EditSet -> editSet(event.id)
            is HomeEvent.Action1 -> action1()
            is HomeEvent.NameSelected -> nameSelected(event.name)
            is HomeEvent.Action2 -> action2()
            is HomeEvent.Action3 -> action3()
            is HomeEvent.Increase -> change(event.die, true)
            is HomeEvent.Decrease -> change(event.die, false)
            is HomeEvent.Clear -> clear(event.die)
            is HomeEvent.Help -> privateEffect.postValue(HomeEffect.ShowHelp)

        }
    }


    //<editor-fold desc="event listeners">
    private fun roll(die: Die) {
        if (state.value is HomeState.Viewing) {
            viewModelScope.launch {
                val calculated = listOf(RollData(die, repo.generateValue(die))).toRolls()
                privateState.postValue(HomeState.Viewing(calculated))
            }
        }
    }

    private fun rollSet(id: UUID) {
        viewModelScope.launch {
            when (val set = repo.getSet(id)) {
                is Reply.Success -> {
                    val rolls = repo.generateRolls(set.data)
                    val calculated = rolls.toRolls(set.data.modifier)
                    privateState.postValue(HomeState.Viewing(calculated))
                }
                is Reply.Failure -> privateEffect.postValue(HomeEffect.ShowError(set.messageId))
            }
        }
    }

    private fun editSet(id: UUID) {
        viewModelScope.launch {
            repo.getSet(id).onSuccess {
                privateState.postValue(HomeState.Editing(it))
            }.onFailure {
                privateEffect.postValue(HomeEffect.ShowError(it.messageId))
            }
        }
    }

    private fun action1() {
        val set = when (val current = state.value) {
            is HomeState.Creating -> current.set as DieSetData
            is HomeState.Editing -> current.set as DieSetData
            is HomeState.Viewing -> {
                viewModelScope.launch {
                    val nextId = repo.getNextAvailableId()
                    privateEffect.postValue(HomeEffect.ShowNameDialog(nextId = nextId))
                }
                return
            }
            else -> return
        }

        viewModelScope.launch {
            repo.saveSet(set).onFailure {
                privateEffect.postValue(HomeEffect.ShowError(it.messageId))
            }.onSuccess {
                privateState.postValue(HomeState.Viewing())
            }
        }
    }

    private fun nameSelected(name: String) {
        viewModelScope.launch {
            repo.buildNewSet(name).onFailure {
                privateEffect.postValue(HomeEffect.ShowError(it.messageId))
            }.onSuccess {
                privateState.postValue(HomeState.Creating(it))
            }
        }
    }

    private fun action2() {
        val current = state.value
        if (current is HomeState.Creating) {
            privateState.postValue(HomeState.Viewing())
        } else if (current is HomeState.Editing) {
            viewModelScope.launch {
                repo.delete(current.set as DieSetData).onFailure {
                    privateEffect.postValue(HomeEffect.ShowError(it.messageId))
                }.onSuccess {
                    privateState.postValue(HomeState.Viewing())
                }
            }
        }
    }

    private fun action3() {
        val current = state.value
        if (current is HomeState.Editing) {
            privateState.postValue(HomeState.Viewing())
        }
    }

    private fun change(die: Die, increase: Boolean) {
        val current = state.value
        val set = when (current) {
            is HomeState.Creating -> current.set as DieSetData
            is HomeState.Editing -> current.set as DieSetData
            else -> return
        }

        val new = if (die == Die.D100) {
            set.copy(modifier = (set.modifier + if (increase) 1 else -1).coerceAtLeast(0))
        } else {
            set.copy(
                dice = set.dice.toMutableMap().apply {
                    this[die] = ((this[die] ?: 0) + (if (increase) 1 else -1)).coerceAtLeast(0)
                }
            )
        }
        if (current is HomeState.Creating) {
            privateState.postValue(HomeState.Creating(new))
        } else if (current is HomeState.Editing) {
            privateState.postValue(HomeState.Editing(new))
        }

    }

    private fun clear(die: Die) {
        val current = state.value
        val set = when (current) {
            is HomeState.Creating -> current.set as DieSetData
            is HomeState.Editing -> current.set as DieSetData
            else -> return
        }
        val new = set.copy(
            dice = set.dice.toMutableMap().apply {
                this[die] = 0
            }
        )
        if (current is HomeState.Creating) {
            privateState.postValue(HomeState.Creating(new))
        } else if (current is HomeState.Editing) {
            privateState.postValue(HomeState.Editing(new))
        }
    }

    //</editor-fold>

    //<editor-fold desc="private functions">
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
    //</editor-fold>

}