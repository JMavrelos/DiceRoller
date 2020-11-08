package gr.blackswamp.diceroller.data.repos

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.sqlite.db.SimpleSQLiteQuery
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.data.db.AppDatabase
import gr.blackswamp.diceroller.data.db.DieSetEntity
import gr.blackswamp.diceroller.data.db.DieSetHeaderEntity
import gr.blackswamp.diceroller.data.rnd.RandomGenerator
import gr.blackswamp.diceroller.logic.DiePropertyData
import gr.blackswamp.diceroller.logic.DieSetData
import gr.blackswamp.diceroller.logic.DieSetHeaderData
import gr.blackswamp.diceroller.logic.RollData
import gr.blackswamp.diceroller.ui.model.Die
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class HomeRepository : KoinComponent {
    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal val NEXT_ROW_ID_QUERY = "SELECT ifNull(max(rowid),0) + 1 FROM die_sets"
    }

    private val newSetId by lazy { UUID(0L, 0L) }
    private val db by inject<AppDatabase>()
    private val rnd by inject<RandomGenerator>()

    fun getSets(): LiveData<List<DieSetHeaderData>> {
        return db.dieSetDao.getSetHeaders().map { it.map(DieSetHeaderEntity::toData) }
    }

    suspend fun generateValue(die: Die): Int {
        return rnd.nextInt(die.max) + 1
    }

    suspend fun buildNewSet(name: String): Reply<DieSetData> {
        if (db.dieSetDao.countByName(name) > 0)
            return Reply.Failure(R.string.error_set_name_exists, IllegalArgumentException("Set $name already exists"))

        return Reply.Success(
            DieSetData(
                newSetId, name, mapOf(
                    Die.D4 to DiePropertyData(0, false),
                    Die.D6 to DiePropertyData(0, false),
                    Die.D8 to DiePropertyData(0, false),
                    Die.D10 to DiePropertyData(0, false),
                    Die.D12 to DiePropertyData(0, false),
                    Die.D20 to DiePropertyData(0, false),
                ), 0
            )
        )
    }

    suspend fun exists(dieSet: DieSetData): Boolean {
        return try {
            dieSet.id != newSetId && db.dieSetDao.getSet(dieSet.id) != null
        } catch (ignored: Throwable) {
            false
        }
    }

    suspend fun getSet(id: UUID): Reply<DieSetData> =
        tryWithReply(R.string.error_set_not_found) { db.dieSetDao.getSet(id)?.toData() ?: throw Throwable("Set with id $id not found") }


    suspend fun delete(set: DieSetData): Reply<Unit> =
        tryWithReply(R.string.error_deleting_set) { db.dieSetDao.delete(set.id) }

    suspend fun saveSet(set: DieSetData): Reply<DieSetData> {
        if (set.dice.count { it.value.times > 0 } == 0)
            return Reply.Failure(R.string.error_set_with_no_rolls, Throwable("There are no rolls in submitted data"))
        return tryWithReply(R.string.error_saving_set) {
            if (set.id == newSetId) {
                val newEntity = set.toEntity().copy(id = UUID.randomUUID())
                db.dieSetDao.insert(newEntity)
                newEntity
            } else {
                val newEntity = set.toEntity()
                db.dieSetDao.update(newEntity)
                newEntity
            }.toData()
        }
    }

    private suspend fun <T : Any> tryWithReply(errorMessage: Int, action: suspend () -> T): Reply<T> {
        return try {
            val reply = action.invoke()
            Reply.Success(reply)
        } catch (t: Throwable) {
            Reply.Failure(errorMessage, t)
        }
    }

    suspend fun generateRolls(set: DieSetData): List<RollData> {
        return withContext(Dispatchers.Default) {
            val rolls = mutableListOf<RollData>()
            set.dice.forEach { (die, prop) ->
                yield()
                if (die != Die.D100) { //we don't roll d100s in sets
                    repeat((1..prop.times).count()) {
                        do {
                            yield()
                            val newValue = generateValue(die)
                            rolls.add(RollData(die, newValue))
                        } while (prop.exploding && newValue == die.max)
                    }
                }
            }
            rolls
        }
    }

    suspend fun getNextAvailableId(): Int {
        return withContext(Dispatchers.IO) {
            try {
                db.query(SimpleSQLiteQuery(NEXT_ROW_ID_QUERY)).use {
                    if (!it.moveToFirst()) {
                        1
                    } else {
                        it.getInt(0)
                    }
                }
            } catch (t: Throwable) {
                1
            }
        }
    }


}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun DieSetEntity.toData(): DieSetData {
    val map = mutableMapOf(
        Die.D4 to DiePropertyData(this.d4s, this.d4Explode),
        Die.D6 to DiePropertyData(this.d6s, this.d6Explode),
        Die.D8 to DiePropertyData(this.d8s, this.d8Explode),
        Die.D10 to DiePropertyData(this.d10s, this.d10Explode),
        Die.D12 to DiePropertyData(this.d12s, this.d12Explode),
        Die.D20 to DiePropertyData(this.d20s, this.d20Explode),
    )

    return DieSetData(this.id, this.name, map, this.mod)
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun DieSetData.toEntity(): DieSetEntity {
    return DieSetEntity(
        this.id, this.name, this.dice[Die.D4]?.times ?: 0, this.dice[Die.D4]?.exploding == true,
        this.dice[Die.D6]?.times ?: 0, this.dice[Die.D6]?.exploding == true,
        this.dice[Die.D8]?.times ?: 0, this.dice[Die.D8]?.exploding == true,
        this.dice[Die.D10]?.times ?: 0, this.dice[Die.D10]?.exploding == true,
        this.dice[Die.D12]?.times ?: 0, this.dice[Die.D12]?.exploding == true,
        this.dice[Die.D20]?.times ?: 0, this.dice[Die.D20]?.exploding == true,
        this.modifier
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun DieSetHeaderEntity.toData(): DieSetHeaderData =
    DieSetHeaderData(this.id, this.name)


