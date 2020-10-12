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
    val d6s: Int,
    val d8s: Int,
    val d10s: Int,
    val d12s: Int,
    val d20s: Int,
    val mod: Int
)

data class DieSetHeaderEntity(val id: UUID, val name: String)