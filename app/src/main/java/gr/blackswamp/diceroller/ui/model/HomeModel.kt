package gr.blackswamp.diceroller.ui.model

import java.util.*

sealed class Roll {
    class Result(val die: Die, val value: Int) : Roll()
    class Modifier(val text: String) : Roll()
}

data class HomeFragmentState(
    val rolls: List<Roll> = listOf(),
    val set: DieSet? = null,
    val editing: Boolean = false
)

interface DieSet {
    val id: UUID
    val name: String
    val dice: Map<Die, Int>
}

interface DieSetHeader {
    val id: UUID
    val name: String
}

enum class Die {
    D4,
    D6,
    D8,
    D10,
    D12,
    D20,
    Mod
}