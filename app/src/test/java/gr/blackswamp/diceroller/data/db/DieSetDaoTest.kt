package gr.blackswamp.diceroller.data.db

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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.random.Random


@RunWith(AndroidJUnit4::class)
@Config(application = TestApp::class)
class DieSetDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: DieSetDao

    @Before
    fun setUp() {
        db = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
            "test.db"
        ).allowMainThreadQueries()
            .build()
        dao = db.dieSetDao
    }

    @After
    fun tearDown() {
        db.close()
    }


    @Test
    fun `get entity by id`() {
        runBlocking {
            saveAllSets()

            val checked = TestData.SETS.random()

            val retrieved = dao.getSet(checked.id)

            assertEquals(checked, retrieved)
        }
    }

    @Test
    fun `get all set headers`() {
        runBlocking {
            val affected = dao.insert(*TestData.SETS.toTypedArray())
            assertEquals(TestData.SETS.size, affected.size)

            val retrieved = dao.getSetHeaders().getOrAwait()
            assertEquals(TestData.SET_HEADERS.size, TestData.SETS.intersect(retrieved).size)
        }
    }

    @Test
    fun `insert set`() {
        runBlocking {
            val expected = TestData.SETS.random()

            dao.insert(expected)

            assertEquals(1, db.count("die_sets"))

            val retrieved = dao.getSet(expected.id)

            assertEquals(expected, retrieved)

        }
    }


    fun `insert existing set fails`() {
        runBlocking {
            val expected = TestData.SETS.random()

            dao.insert(expected)

            assertEquals(1, db.count("die_sets"))

            val retrieved = dao.insert(expected)
            assertEquals(0, retrieved.size)
        }
    }

    @Test
    fun `add mass sets`() {
        runBlocking {
            val expected = TestData.SETS.shuffled().take(Random.nextInt(TestData.SETS.size))

            val inserted = dao.insert(*expected.toTypedArray()).size

            assertEquals(expected.size, db.count("die_sets"))
            assertEquals(expected.size, inserted)
            val retrieved = dao.getSetHeaders().getOrAwait()
            assertEquals(expected.size, retrieved.intersect(expected).size)
        }
    }

    @Test
    fun `update a set with new data`() {
        runBlocking {
            val inserted = TestData.SETS.random()
            val expected = inserted.copy(name = "this is the new name", d4s = inserted.d4s + 1, d8s = inserted.d8s - 1)
            dao.insert(inserted)

            val updated = dao.update(expected)

            val retrieved = dao.getSet(expected.id)
            assertEquals(expected, retrieved)
            assertEquals(1, updated)
        }
    }

    @Test
    fun `delete sets`() {
        runBlocking {
            saveAllSets()

            val deleted = TestData.SETS.shuffled().take(3)

            dao.delete(*deleted.toTypedArray())

            assertEquals(TestData.SETS.size - deleted.size, db.count("die_sets"))

            val remaining = TestData.SET_HEADERS.toMutableList().apply { removeAll(deleted.map { DieSetHeaderEntity(it.id, it.name) }) }

            assertEquals(deleted.size, remaining.intersect(deleted).size)
        }
    }

    @Test
    fun `delete set by id`() {
        runBlocking {
            dao.insert(*TestData.SETS.toTypedArray())
            val deleted = TestData.SETS.random()

            dao.delete(deleted.id)

            assertEquals(0, db.countWhere("die_sets", "id = '${deleted.id}'"))
            assertEquals(TestData.SETS.size - 1, db.count("die_sets"))
        }
    }

    @Test
    fun `delete multiple set by id`() {
        runBlocking {
            dao.insert(*TestData.SETS.toTypedArray())
            val deleted = TestData.SETS.shuffled().take(4).map { it.id }

            dao.delete(*deleted.toTypedArray())

            assertEquals(0, db.countWhere("die_sets", "id in ${deleted.joinToString("','", "('", "')")}"))
            assertEquals(TestData.SETS.size - 4, db.count("die_sets"))
        }
    }


    @Test
    fun `when sets change live data change automatically`() {
        runBlocking {
            dao.insert(*TestData.SETS.toTypedArray())

            val newSet = TestData.buildSet(3).copy(name = "this is a totally different set")

            val expected = TestData.SETS.toMutableList().apply {
                add(newSet)
            }

            val retrieved = dao.getSetHeaders()
            assertEquals(expected.size - 1, retrieved.getOrAwait().size)

            dao.insert(newSet)

            assertEquals(expected, retrieved.getOrAwait())
        }
    }


    private fun saveAllSets() {
        db.openHelper.writableDatabase.use { db ->
            db.beginTransaction()
            for (set in TestData.SETS) {
                val query = String.format(
                    "insert into die_sets (id,name,d4s,d6s,d8s,d10s,d12s,d20s) values (?,?,?,?,?,?,?,?)",
                    arrayOf(set.id.toString(), set.name, set.d4s, set.d6s, set.d8s, set.d10s, set.d12s, set.d20s)
                )
                db.execSQL(
                    "insert into die_sets (id,name,d4s,d6s,d8s,d10s,d12s,d20s) values (?,?,?,?,?,?,?,?)",
                    arrayOf(set.id.toString(), set.name, set.d4s.toString(), set.d6s.toString(), set.d8s.toString(), set.d10s.toString(), set.d12s.toString(), set.d20s.toString())
                )
            }
            db.setTransactionSuccessful()
            db.endTransaction()
        }
        assertEquals(TestData.SETS.size, db.count("die_sets"))
    }
}