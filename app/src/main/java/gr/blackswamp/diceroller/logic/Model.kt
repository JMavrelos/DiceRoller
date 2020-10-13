package gr.blackswamp.diceroller.logic

import gr.blackswamp.diceroller.ui.Die
import gr.blackswamp.diceroller.ui.DieSet
import gr.blackswamp.diceroller.ui.DieSetHeader
import java.util.*

data class DieSetHeaderData(
    override val id: UUID,
    override val name: String
) : DieSetHeader

data class DieSetData(
    override val id: UUID,
    override val name: String,
    override val dice: Map<Die, Int> = mapOf()
) : DieSet

data class RollData(val die: Die, val value: Int)