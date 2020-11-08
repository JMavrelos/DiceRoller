package gr.blackswamp.diceroller.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "die_sets")
data class DieSetEntity(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val d4s: Int,
    val d4Explode: Boolean,
    val d6s: Int,
    val d6Explode: Boolean,
    val d8s: Int,
    val d8Explode: Boolean,
    val d10s: Int,
    val d10Explode: Boolean,
    val d12s: Int,
    val d12Explode: Boolean,
    val d20s: Int,
    val d20Explode: Boolean,
    val mod: Int
)

data class DieSetHeaderEntity(val id: UUID, val name: String)