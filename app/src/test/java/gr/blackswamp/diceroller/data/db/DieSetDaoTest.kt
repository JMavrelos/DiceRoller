package gr.blackswamp.diceroller.data.db

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.blackswamp.diceroller.TestData
import gr.blackswamp.diceroller.app.TestApp
import gr.blackswamp.diceroller.uitls.count
import gr.blackswamp.diceroller.uitls.countWhere
import gr.blackswamp.diceroller.uitls.getOrAwait
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*


@RunWith(AndroidJUnit4::class)
@Config(application = TestApp::class, manifest = Config.NONE)
class DieSetDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: DieSetDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        dao = db.dieSetDao
    }


    @After
    fun tearDown() {
        db.close()
    }


    @Test
    fun `insert saves data`() {
        runBlocking {
            val set = TestData.SETS.random()

            dao.insert(set)

            assertEquals(1, db.count("die_sets"))
            val count = db.countWhere(
                "die_sets",
                "id = '${set.id}' AND name = '${set.name}' AND d4s = ${set.d4s} AND d6s = ${set.d6s} AND d8s = ${set.d8s} AND d10s = ${set.d10s} AND d12s = ${set.d12s} AND d20s = ${set.d20s} AND mod = ${set.mod} "
            )
            assertEquals(1, count)
        }
    }

    @Test
    fun `insert over existing one throws an exception`() {
        runBlocking {
            val set = TestData.SETS.random()
            dao.insert(set)
            assertEquals(1, db.count("die_sets"))
            val exception = assertThrows(SQLiteConstraintException::class.java) {
                runBlocking {
                    dao.insert(set.copy(d4s = 100, mod = 100))
                }
            }
            assertEquals("Cannot execute for last inserted row ID", exception.message)
        }
    }

    @Test
    fun `update works correctly`() {
        runBlocking {
            insertAllSets()
            val set = TestData.SETS.random()
            val new = set.copy(name = "this is a new set", d4s = 100, d6s = 101, d8s = 102, d10s = 103, d12s = 104, d20s = 105, mod = 106)

            val updated = dao.update(new)

            assertEquals(TestData.COUNT, db.count("die_sets"))
            val count = db.countWhere(
                "die_sets",
                "id = '${new.id}' AND name = '${new.name}' AND d4s = ${new.d4s} AND d6s = ${new.d6s} AND d8s = ${new.d8s} AND d10s = ${new.d10s} AND d12s = ${new.d12s} AND d20s = ${new.d20s} AND mod = ${new.mod} "
            )
            assertEquals(1, count)
            assertEquals(1, updated)
        }
    }

    @Test
    fun `update fails when there is nothing to update`() {
        runBlocking {
            val set = TestData.SETS.random()

            val updated = dao.update(set)

            assertEquals(0, updated)
            assertEquals(0, db.count("die_sets"))
        }
    }

    @Test
    fun `delete works correctly`() {
        runBlocking {
            insertAllSets()

            val set = TestData.SETS.random()

            val deleted = dao.delete(set.id)

            assertEquals(1, deleted)
            assertEquals(TestData.SETS.size - 1, db.count("die_sets"))
            assertEquals(0, db.countWhere("die_sets", "id = '${set.id}'"))
        }
    }

    @Test
    fun `delete fails when there is nothing to delete`() {
        runBlocking {
            insertAllSets()

            val set = TestData.SETS.random().copy(id = UUID.randomUUID())

            val deleted = dao.delete(set.id)

            assertEquals(0, deleted)
            assertEquals(TestData.COUNT, db.count("die_sets"))
        }
    }

    @Test
    fun `get set works`() {
        runBlocking {
            insertAllSets()
            val expected = TestData.SETS.random()

            val set = dao.getSet(expected.id)

            assertEquals(expected, set)
        }
    }

    @Test
    fun `get set that doesn't exist fails`() {
        runBlocking {
            insertAllSets()
            val set = dao.getSet(UUID.randomUUID())
            assertNull(set)
        }
    }

    @Test
    fun `get set headers works`() {
        runBlocking {
            insertAllSets()

            val headers = dao.getSetHeaders()

            assertEquals(TestData.SET_HEADERS, headers.getOrAwait())
        }
    }

    @Test
    fun `get headers is updated by insert`() {
        runBlocking {
            insertAllSets()
            val headers = dao.getSetHeaders()
            assertEquals(TestData.SET_HEADERS, headers.getOrAwait())
            val newSet = TestData.buildSet(111111111)

            dao.insert(newSet)
            val newHeaders = headers.getOrAwait()
            assertTrue(newHeaders.contains(DieSetHeaderEntity(newSet.id, newSet.name)))
            assertEquals(TestData.COUNT + 1, newHeaders.size)
        }
    }

    @Test
    fun `get headers is updated by update`() {
        runBlocking {
            insertAllSets()
            val headers = dao.getSetHeaders()
            assertEquals(TestData.SET_HEADERS, headers.getOrAwait())
            val newSet = TestData.SETS.random().copy(name = "this is a new set", d4s = 100, d6s = 101, d8s = 102, d10s = 103, d12s = 104, d20s = 105, mod = 106)

            dao.update(newSet)
            val newHeaders = headers.getOrAwait()

            assertTrue(newHeaders.contains(DieSetHeaderEntity(newSet.id, newSet.name)))
            assertEquals(TestData.COUNT, newHeaders.size)
        }
    }


    private fun insertAllSets() {
        db.runInTransaction {
            TestData.SETS.forEach {
                runBlocking {
                    dao.insert(it)
                }
            }
        }
        assertEquals(TestData.COUNT, db.count("die_sets"))
    }

}