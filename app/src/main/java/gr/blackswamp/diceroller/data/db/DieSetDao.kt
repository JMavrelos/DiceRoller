package gr.blackswamp.diceroller.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface DieSetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(set: DieSetEntity)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(set: DieSetEntity): Int

    @Query("delete from die_sets where id = :id")
    suspend fun delete(id: UUID): Int

    @Query("select id , name from die_sets")
    fun getSetHeaders(): LiveData<List<DieSetHeaderEntity>>

    @Query("select * from die_sets where id = :id")
    suspend fun getSet(id: UUID): DieSetEntity?

    @Query("select count(*) from die_sets where lower(name) = lower(:name)")
    suspend fun countByName(name: String): Int
}
