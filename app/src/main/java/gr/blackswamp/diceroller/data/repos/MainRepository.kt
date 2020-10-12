package gr.blackswamp.diceroller.data.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import gr.blackswamp.diceroller.data.db.AppDatabase
import gr.blackswamp.diceroller.data.db.DieSetEntity
import gr.blackswamp.diceroller.data.db.DieSetHeaderEntity
import gr.blackswamp.diceroller.logic.DieSetData
import gr.blackswamp.diceroller.logic.DieSetHeaderData
import gr.blackswamp.diceroller.logic.RollData
import gr.blackswamp.diceroller.ui.Die
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.random.Random

class MainRepository : KoinComponent {
    private val newSetId by lazy { UUID(0L, 0L) }
    private val db by inject<AppDatabase>()
    private val rnd get() = Random.Default

    fun getSets(): LiveData<List<DieSetHeaderData>> {
        return db.dieSetDao.getSetHeaders().map { it.map(DieSetHeaderEntity::toData) }
    }

    fun generateValue(die: Die): Int {
        return when (die) {
            Die.D4 -> rnd.nextInt(4) + 1
            Die.D6 -> rnd.nextInt(6) + 1
            Die.D8 -> rnd.nextInt(8) + 1
            Die.D10 -> rnd.nextInt(10) + 1
            Die.D12 -> rnd.nextInt(12) + 1
            Die.D20 -> rnd.nextInt(20) + 1
            Die.Mod -> rnd.nextInt(100) + 1
        }
    }

    fun buildNewSet(): DieSetData {
        return DieSetData(
            newSetId, "New Set", mapOf(
                Die.D4 to 0,
                Die.D6 to 0,
                Die.D8 to 0,
                Die.D10 to 0,
                Die.D12 to 0,
                Die.D20 to 0,
                Die.Mod to 0
            )
        )
    }

    fun exists(dieSet: DieSetData): Boolean {
        return (dieSet.id != newSetId)
    }

    suspend fun getSet(id: UUID): Reply<DieSetData> =
        tryWithReply { db.dieSetDao.getSet(id).toData() }


    suspend fun delete(set: DieSetData): Reply<Unit> =
        tryWithReply { db.dieSetDao.delete(set.id) }

    suspend fun saveSet(set: DieSetData): Reply<DieSetData> =
        tryWithReply {
            if (set.id == newSetId) {
                val newEntity = set.toEntity().copy(id = UUID.randomUUID())
                db.dieSetDao.insert(newEntity)
                newEntity.toData()
            } else {
                val newEntity = set.toEntity()
                db.dieSetDao.update(newEntity)
                newEntity.toData()
            }
        }

    private suspend fun <T> tryWithReply(action: suspend () -> T): Reply<T> {
        return try {
            val reply = action.invoke()
            Reply.success(reply)
        } catch (t: Throwable) {
            Reply.failure(t)
        }
    }

    suspend fun generateRolls(set: DieSetData): List<RollData> {
        return withContext(Dispatchers.Default) {
            val rolls = mutableListOf<RollData>()
            set.dice.forEach { (die, times) ->
                yield()
                repeat((1..times).count()) {
                    yield()
                    rolls.add(RollData(die, generateValue(die)))
                }
            }
            rolls
        }
    }


}

private fun DieSetEntity.toData(): DieSetData {
    val map = mutableMapOf(
        Die.D4 to this.d4s,
        Die.D6 to this.d6s,
        Die.D8 to this.d8s,
        Die.D10 to this.d10s,
        Die.D12 to this.d12s,
        Die.D20 to this.d20s,
        Die.Mod to this.mod
    )
    return DieSetData(this.id, this.name, map)
}

private fun DieSetData.toEntity(): DieSetEntity {
    return DieSetEntity(
        this.id, this.name, this.dice[Die.D4] ?: 0,
        this.dice[Die.D6] ?: 0,
        this.dice[Die.D8] ?: 0,
        this.dice[Die.D10] ?: 0,
        this.dice[Die.D12] ?: 0,
        this.dice[Die.D20] ?: 0,
        this.dice[Die.Mod] ?: 0,
    )

}

private fun DieSetHeaderEntity.toData(): DieSetHeaderData =
    DieSetHeaderData(this.id, this.name)


