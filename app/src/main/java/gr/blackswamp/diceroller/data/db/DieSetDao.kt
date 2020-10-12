package gr.blackswamp.diceroller.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface DieSetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(set: DieSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(set: DieSetEntity)

    @Query("delete from die_sets where id = :id")
    suspend fun delete(id: UUID)

    @Query("SELECT id , name from die_sets")
    fun getSetHeaders(): LiveData<List<DieSetHeaderEntity>>

    @Query("SELECT * from die_sets where id = :id")
    suspend fun getSet(id: UUID): DieSetEntity
}
