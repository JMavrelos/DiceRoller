package gr.blackswamp.diceroller.data.repos

import android.database.Cursor
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.nhaarman.mockitokotlin2.*
import gr.blackswamp.diceroller.TestData
import gr.blackswamp.diceroller.data.db.AppDatabase
import gr.blackswamp.diceroller.data.db.DieSetDao
import gr.blackswamp.diceroller.data.db.DieSetEntity
import gr.blackswamp.diceroller.data.db.DieSetHeaderEntity
import gr.blackswamp.diceroller.data.repos.HomeRepository.Companion.NEXT_ROW_ID_QUERY
import gr.blackswamp.diceroller.data.rnd.RandomGenerator
import gr.blackswamp.diceroller.logic.DieSetData
import gr.blackswamp.diceroller.logic.DieSetHeaderData
import gr.blackswamp.diceroller.ui.model.Die
import gr.blackswamp.diceroller.uitls.KoinUnitTest
import gr.blackswamp.diceroller.uitls.getOrAwait
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock
import java.util.*
import kotlin.random.Random


@ExperimentalCoroutinesApi
class HomeRepositoryTest : KoinUnitTest() {
    companion object {
        private val EmptyUUID = UUID(0L, 0L)
    }

    private val db = mock(AppDatabase::class.java)
    private val dao = mock(DieSetDao::class.java)
    private val rnd = mock(RandomGenerator::class.java)
    private lateinit var repo: HomeRepository

    override val modules: Module = module {
        single<AppDatabase> { db }
        factory<RandomGenerator> { rnd }
    }

    override fun setup() {
        super.setup()
        repo = HomeRepository()
        whenever(db.dieSetDao).thenReturn(dao)
    }

    @Test
    fun `get sets returns correct live data`() {
        val liveData = MutableLiveData(TestData.SET_HEADERS)
        val expected = TestData.SET_HEADERS.map { it.toData() }
        whenever(dao.getSetHeaders()).thenReturn(liveData)

        val response = repo.getSets().getOrAwait()

        assertEquals(expected, response)
    }

    @Test
    fun `when generate value is called it calls the appropriate method from generator`() {
        runBlocking {
            whenever(rnd.nextInt(100)).thenReturn(32)

            val response = repo.generateValue(Die.Mod)

            assertEquals(33, response)
            verify(rnd).nextInt(100)
        }
    }

    @Test
    fun `when new set is called an empty set is created`() {
        val name = "hello world"
        val expected = DieSetData(EmptyUUID, name, mapOf(Die.D4 to 0, Die.D6 to 0, Die.D8 to 0, Die.D10 to 0, Die.D12 to 0, Die.D20 to 0, Die.Mod to 0))

        val response = repo.buildNewSet(name)

        assertEquals(expected, response)
    }

    @Test
    fun `when checking exists with an empty uuid it automatically returns false`() {
        runBlocking {
            val response = repo.exists(DieSetData(EmptyUUID, "hello world"))

            assertFalse(response)
            verify(dao, never()).getSet(any())
        }
    }

    @Test
    fun `when checking exists with non empty uuid it also checks the database and returns true when found`() {
        runBlocking {
            val existing = TestData.SETS.random()
            whenever(dao.getSet(existing.id)).thenReturn(existing)

            val response = repo.exists(DieSetData(existing.id, "hello world"))

            assertTrue(response)
            verify(dao).getSet(existing.id)
            verifyNoMoreInteractions(dao)
        }
    }

    @Test
    fun `when checking exists with non empty uuid it also checks the database and  returns false when not found`() {
        runBlocking {
            val id = UUID.randomUUID()
            whenever(dao.getSet(id)).thenReturn(null)

            val response = repo.exists(DieSetData(id, "hello world"))

            assertFalse(response)
            verify(dao).getSet(id)
            verifyNoMoreInteractions(dao)
        }
    }

    @Test
    fun `toData parses correctly`() {
        val id = UUID.randomUUID()
        val name = "hello world"
        val entity = DieSetEntity(id, name, 1111, 221312, 12312, 312, 31, 2314, 124)
        val expected = DieSetData(id, name, mapOf(Die.D4 to 1111, Die.D6 to 221312, Die.D8 to 12312, Die.D10 to 312, Die.D12 to 31, Die.D20 to 2314, Die.Mod to 124))

        val response = entity.toData()

        assertEquals(expected, response)
    }

    @Test
    fun `header toData parses correctly`() {
        val id = UUID.randomUUID()
        val name = "hello world"
        val entity = DieSetHeaderEntity(id, name)
        val expected = DieSetHeaderData(id, name)

        val response = entity.toData()

        assertEquals(expected, response)
    }

    @Test
    fun `toEntity parses correctly`() {
        val id = UUID.randomUUID()
        val name = "hello world"
        val expected = DieSetEntity(id, name, 1111, 221312, 12312, 312, 31, 2314, 124)
        val data = DieSetData(id, name, mapOf(Die.D4 to 1111, Die.D6 to 221312, Die.D8 to 12312, Die.D10 to 312, Die.D12 to 31, Die.D20 to 2314, Die.Mod to 124))

        val response = data.toEntity()

        assertEquals(expected, response)

    }


    @Test
    fun `get set retrieves the set from dao`() {
        runBlocking {
            val entity = TestData.SETS.random()
            val expected = entity.toData()
            whenever(dao.getSet(entity.id)).thenReturn(entity)

            val response = repo.getSet(entity.id)

            assertEquals(expected, response.getOrNull())
            verify(dao).getSet(entity.id)
            verifyNoMoreInteractions(dao)
        }
    }

    @Test
    fun `get cannot find the set in the dao`() {
        runBlocking {
            val id = UUID.randomUUID()
            whenever(dao.getSet(id)).thenReturn(null)

            val response = repo.getSet(id)

            assertTrue(response.isFailure)
            assertEquals("Set with id $id not found", response.exceptionOrNull()!!.message)
            verify(dao).getSet(id)
            verifyNoMoreInteractions(dao)
        }
    }

    @Test
    fun `get cannot retrieve the set from dao`() {
        runBlocking {
            val id = UUID.randomUUID()
            val error = "this is the problem with your query"
            whenever(dao.getSet(id)).thenThrow(RuntimeException(error))

            val response = repo.getSet(id)

            assertTrue(response.isFailure)
            assertEquals(error, response.exceptionOrNull()!!.message)
            verify(dao).getSet(id)
            verifyNoMoreInteractions(dao)
        }
    }

    @Test
    fun `delete works correctly`() {
        runBlocking {
            val entity = TestData.SETS.random()

            val response = repo.delete(entity.toData())

            assertTrue(response.isSuccess)
            verify(dao).delete(entity.id)
            verifyNoMoreInteractions(dao)
        }
    }

    @Test
    fun `delete fails with error`() {
        runBlocking {
            val entity = TestData.SETS.random()
            val error = "this is the problem with your query"
            whenever(dao.delete(entity.id)).thenThrow(RuntimeException(error))

            val response = repo.delete(entity.toData())

            assertFalse(response.isSuccess)
            assertEquals(error, response.exceptionOrNull()?.message)
            verify(dao).delete(entity.id)
            verifyNoMoreInteractions(dao)
        }
    }

    @Test
    fun `save new set successfully`() {
        runBlocking {
            val set = TestData.SETS.random().copy(id = EmptyUUID).toData()

            val reply = repo.saveSet(set)

            assertTrue(reply.isSuccess)
            val captor = argumentCaptor<DieSetEntity>()
            verify(dao).insert(captor.capture())
            verifyNoMoreInteractions(dao)
            assertNotEquals(EmptyUUID, captor.firstValue.id)
            assertEquals(set.name, captor.firstValue.name)
            assertEquals(set.dice[Die.D4], captor.firstValue.d4s)
            assertEquals(set.dice[Die.D6], captor.firstValue.d6s)
            assertEquals(set.dice[Die.D8], captor.firstValue.d8s)
            assertEquals(set.dice[Die.D10], captor.firstValue.d10s)
            assertEquals(set.dice[Die.D12], captor.firstValue.d12s)
            assertEquals(set.dice[Die.D20], captor.firstValue.d20s)
            assertEquals(set.dice[Die.Mod], captor.firstValue.mod)

            assertEquals(reply.getOrNull(), captor.firstValue.toData())

            assertNotEquals(EmptyUUID, captor.firstValue.id)
            assertEquals(captor.firstValue.name, reply.getOrNull()!!.name)
            assertEquals(set.dice, reply.getOrNull()!!.dice)
        }
    }

    @Test
    fun `save existing set successfully`() {
        runBlocking {
            val set = TestData.SETS.random().toData()

            val reply = repo.saveSet(set)

            assertTrue(reply.isSuccess)
            val captor = argumentCaptor<DieSetEntity>()
            verify(dao).update(captor.capture())
            verifyNoMoreInteractions(dao)
            assertEquals(set.toEntity(), captor.firstValue)
            assertEquals(set, reply.getOrNull())
        }
    }

    @Test
    fun `save new set fails`() {
        runBlocking {
            val set = TestData.SETS.random().copy(id = EmptyUUID).toData()
            whenever(dao.insert(any())).thenThrow(RuntimeException("problem inserting"))

            val reply = repo.saveSet(set)

            assertTrue(reply.isFailure)
            verify(dao).insert(any())
            verifyNoMoreInteractions(dao)
            assertEquals("problem inserting", reply.exceptionOrNull()?.message)
        }
    }

    @Test
    fun `save existing set failed`() {
        runBlocking {
            val set = TestData.SETS.random().toData()

            whenever(dao.update(set.toEntity())).thenThrow(RuntimeException("problem updating"))

            val reply = repo.saveSet(set)

            assertTrue(reply.isFailure)
            verify(dao).update(set.toEntity())
            verifyNoMoreInteractions(dao)
            assertEquals("problem updating", reply.exceptionOrNull()?.message)
        }
    }

    @Test
    fun `generate random rolls`() {
        runBlocking {
            val random = Random.Default
            whenever(rnd.nextInt(anyInt())).doAnswer { a -> random.nextInt(a.arguments[0] as Int) }
            val set = DieSetData(
                EmptyUUID, "test", mapOf(
                    Die.D4 to random.nextInt(0, 5),
                    Die.D6 to random.nextInt(0, 5),
                    Die.D8 to random.nextInt(0, 5),
                    Die.D10 to random.nextInt(0, 5),
                    Die.D12 to random.nextInt(0, 5),
                    Die.D20 to random.nextInt(0, 5),
                    Die.Mod to random.nextInt(0, 5)
                )
            )

            val rolls = repo.generateRolls(set)

            assertEquals(set.dice[Die.D4], rolls.count { it.die == Die.D4 })
            assertEquals(set.dice[Die.D4], rolls.count { it.die == Die.D4 })
            assertEquals(set.dice[Die.D6], rolls.count { it.die == Die.D6 })
            assertEquals(set.dice[Die.D8], rolls.count { it.die == Die.D8 })
            assertEquals(set.dice[Die.D10], rolls.count { it.die == Die.D10 })
            assertEquals(set.dice[Die.D12], rolls.count { it.die == Die.D12 })
            assertEquals(set.dice[Die.D20], rolls.count { it.die == Die.D20 })
            assertEquals(1, rolls.count { it.die == Die.Mod })
            assertEquals(set.dice[Die.Mod], rolls.first { it.die == Die.Mod }.value)
        }
    }

    @Test
    fun `calling next available id queries the db for the next one`() {
        runBlocking {
            val cursor = mock(Cursor::class.java)
            whenever(db.query(any())).thenReturn(cursor)
            whenever(cursor.moveToFirst()).thenReturn(true)
            whenever(cursor.getInt(0)).thenReturn(32)

            val reply = repo.getNextAvailableId()

            assertEquals(32, reply)
            verify(cursor).moveToFirst()
            verify(cursor).getInt(0)
            verify(cursor).close()
            verifyNoMoreInteractions(cursor)
            val captor = argumentCaptor<SimpleSQLiteQuery>()
            verify(db).query(captor.capture())
            assertEquals(NEXT_ROW_ID_QUERY, captor.firstValue.sql)
            verifyNoMoreInteractions(db)
        }
    }

    @Test
    fun `calling next available id with move to first fails returns 1`() {
        runBlocking {
            val cursor = mock(Cursor::class.java)
            whenever(db.query(any())).thenReturn(cursor)
            whenever(cursor.moveToFirst()).thenReturn(false)

            val reply = repo.getNextAvailableId()

            assertEquals(1, reply)
            verify(cursor).moveToFirst()
            verify(cursor).close()
            verifyNoMoreInteractions(cursor)
            val captor = argumentCaptor<SimpleSQLiteQuery>()
            verify(db).query(captor.capture())
            assertEquals(NEXT_ROW_ID_QUERY, captor.firstValue.sql)
            verifyNoMoreInteractions(db)
        }
    }

    @Test
    fun `calling next available id which throws error returns 1`() {
        runBlocking {
            whenever(db.query(any())).thenThrow(RuntimeException("oops something went wrong"))

            val reply = repo.getNextAvailableId()

            assertEquals(1, reply)
            val captor = argumentCaptor<SimpleSQLiteQuery>()
            verify(db).query(captor.capture())
            assertEquals(NEXT_ROW_ID_QUERY, captor.firstValue.sql)
            verifyNoMoreInteractions(db)
        }
    }
}