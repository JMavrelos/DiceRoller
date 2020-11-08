package gr.blackswamp.diceroller.ui.model

import androidx.annotation.StringRes
import java.util.*

sealed class HomeState {
    class Viewing(val rolls: List<Roll> = listOf()) : HomeState()
    class Creating(val set: DieSet) : HomeState()
    class Editing(val set: DieSet) : HomeState()
}

sealed class HomeEffect {
    object ShowHelp : HomeEffect()
    class ShowNameDialog(val nextId: Int) : HomeEffect()
    class ShowError(@StringRes val id: Int) : HomeEffect()
}

sealed class HomeEvent {
    class DieSelect(val die: Die) : HomeEvent()
    class RollSet(val id: UUID) : HomeEvent()
    class Clear(val die: Die) : HomeEvent()
    class EditSet(val id: UUID) : HomeEvent()
    class Increase(val die: Die) : HomeEvent()
    class Decrease(val die: Die) : HomeEvent()
    object Action1 : HomeEvent()
    object Action2 : HomeEvent()
    object Action3 : HomeEvent()
    object Help : HomeEvent()
    class NameSelected(val name: String) : HomeEvent()
}

sealed class Roll {
    data class Result(val die: Die, val value: Int) : Roll()
    data class Text(val text: String) : Roll()
}

interface DieSet {
    val id: UUID
    val name: String
    val dice: Map<Die, DieProperty>
    val modifier: Int
}

interface DieProperty {
    val times: Int
    val exploding: Boolean
}

interface DieSetHeader {
    val id: UUID
    val name: String
}

enum class Die(val max: Int) {
    D4(4),
    D6(6),
    D8(8),
    D10(10),
    D12(12),
    D20(20),
    D100(100)
}