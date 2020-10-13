package gr.blackswamp.diceroller

import gr.blackswamp.diceroller.data.db.DieSetEntity
import gr.blackswamp.diceroller.data.db.DieSetHeaderEntity
import java.util.*
import kotlin.random.Random

object TestData {
    private val rnd = Random.Default

    val SETS = (1..100).map(this::buildSet)
    val COUNT get() = SETS.size

    val SET_HEADERS = SETS.map { DieSetHeaderEntity(it.id, it.name) }

    fun buildSet(id: Int) = DieSetEntity(
        UUID.randomUUID(),
        "Set $id",
        rnd.nextInt(4) + 1,
        rnd.nextInt(6) + 1,
        rnd.nextInt(8) + 1,
        rnd.nextInt(10) + 1,
        rnd.nextInt(12) + 1,
        rnd.nextInt(100) + 1,
        rnd.nextInt(5) + 1,
    )
}