package gr.blackswamp.diceroller.logic

import gr.blackswamp.diceroller.ui.model.Die
import gr.blackswamp.diceroller.ui.model.DieProperty
import gr.blackswamp.diceroller.ui.model.DieSet
import gr.blackswamp.diceroller.ui.model.DieSetHeader
import java.util.*

data class DieSetHeaderData(
    override val id: UUID,
    override val name: String
) : DieSetHeader

data class DieSetData(
    override val id: UUID,
    override val name: String,
    override val dice: Map<Die, DiePropertyData> = mapOf(),
    override val modifier: Int = 0
) : DieSet

data class DiePropertyData(override val times: Int, override val exploding: Boolean) : DieProperty

data class RollData(val die: Die, val value: Int)

