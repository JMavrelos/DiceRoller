package gr.blackswamp.diceroller

import gr.blackswamp.diceroller.data.db.DieSetEntity
import gr.blackswamp.diceroller.data.db.DieSetHeaderEntity
import java.util.*
import kotlin.random.Random

object TestData {
    private val rnd = Random.Default
    val SETS = (1..100).map(this::buildSet)

    val SET_HEADERS = SETS.map { DieSetHeaderEntity(it.id, it.name) }

    fun buildSet(id: Int) = DieSetEntity(
        UUID.randomUUID(),
        "Set $id",
        rnd.nextInt(1, 8),
        rnd.nextInt(1, 8),
        rnd.nextInt(1, 8),
        rnd.nextInt(1, 8),
        rnd.nextInt(1, 8),
        rnd.nextInt(1, 8)
    )
}