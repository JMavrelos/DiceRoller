package gr.blackswamp.diceroller.data.repos

import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import gr.blackswamp.diceroller.TestData
import gr.blackswamp.diceroller.data.db.AppDatabase
import gr.blackswamp.diceroller.data.db.DieSetDao
import gr.blackswamp.diceroller.data.db.DieSetEntity
import gr.blackswamp.diceroller.data.db.DieSetHeaderEntity
import gr.blackswamp.diceroller.data.rnd.RandomGenerator
import gr.blackswamp.diceroller.logic.DieSetData
import gr.blackswamp.diceroller.logic.DieSetHeaderData
import gr.blackswamp.diceroller.ui.model.Die
import gr.blackswamp.diceroller.uitls.KoinUnitTest
import gr.blackswamp.diceroller.uitls.getOrAwait
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mockito.mock
import java.util.*

class HomeRepositoryTest : KoinUnitTest() {
    private val EmptyUUID = UUID(0L, 0L)
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
    fun `when get set fails`() {
        //todo:continue here
    }


    @Test
    fun `when generate value is called it calls the appropriate metod from generator`() {
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

    //tests remaining
    //save set stuff
}