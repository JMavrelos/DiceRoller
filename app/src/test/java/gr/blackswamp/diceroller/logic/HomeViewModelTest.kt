package gr.blackswamp.diceroller.logic

import com.nhaarman.mockitokotlin2.*
import gr.blackswamp.diceroller.TestData
import gr.blackswamp.diceroller.TestData.EmptyUUID
import gr.blackswamp.diceroller.data.repos.HomeRepository
import gr.blackswamp.diceroller.data.repos.Reply
import gr.blackswamp.diceroller.data.repos.toData
import gr.blackswamp.diceroller.ui.model.Die
import gr.blackswamp.diceroller.ui.model.Die.*
import gr.blackswamp.diceroller.ui.model.HomeEffect
import gr.blackswamp.diceroller.ui.model.HomeEvent
import gr.blackswamp.diceroller.ui.model.HomeState
import gr.blackswamp.diceroller.ui.model.Roll.Result
import gr.blackswamp.diceroller.ui.model.Roll.Text
import gr.blackswamp.diceroller.uitls.KoinUnitTest
import gr.blackswamp.diceroller.uitls.getOrAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.*

@ExperimentalCoroutinesApi
class HomeViewModelTest : KoinUnitTest() {
    private val repo = mock<HomeRepository>()
    private lateinit var vm: HomeViewModel

    override val modules: Module = module {
        single { repo }
        single { Dispatchers.Default }
    }

    @Before
    override fun setup() {
        super.setup()
        vm = HomeViewModel(app)
    }

    //<editor-fold desc="Roll Event">
    @Test
    fun `roll a d4`() {
        runBlocking {
            //setup
            whenever(repo.generateValue(D4)).thenReturn(24)
            val expected = listOf(Result(D4, 24))

            //run
            vm.process(HomeEvent.Roll(D4))

            //check
            verify(repo).generateValue(D4)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm.state.getOrAwait()
            assertEquals(expected, (newState as HomeState.Viewing).rolls)
        }
    }

    @Test
    fun `roll a d6`() {
        runBlocking {
            //setup
            whenever(repo.generateValue(D4)).thenReturn(24)
            val expected = listOf(Result(D4, 24))

            //run
            vm.process(HomeEvent.Roll(D4))

            //check
            verify(repo).generateValue(D4)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm.state.getOrAwait()
            assertEquals(expected, (newState as HomeState.Viewing).rolls)
        }
    }

    @Test
    fun `roll a d8`() {
        runBlocking {
            //setup
            whenever(repo.generateValue(D8)).thenReturn(24)
            val expected = listOf(Result(D8, 24))

            //run
            vm.process(HomeEvent.Roll(D8))

            //check
            verify(repo).generateValue(D8)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm.state.getOrAwait()
            assertEquals(expected, (newState as HomeState.Viewing).rolls)
        }
    }

    @Test
    fun `roll a d10`() {
        runBlocking {
            //setup
            whenever(repo.generateValue(D10)).thenReturn(24)
            val expected = listOf(Result(D10, 24))

            //run
            vm.process(HomeEvent.Roll(D10))

            //check
            verify(repo).generateValue(D10)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm.state.getOrAwait()
            assertEquals(expected, (newState as HomeState.Viewing).rolls)
        }
    }

    @Test
    fun `roll a d12`() {
        runBlocking {
            //setup
            whenever(repo.generateValue(D12)).thenReturn(24)
            val expected = listOf(Result(D12, 24))

            //run
            vm.process(HomeEvent.Roll(D12))

            //check
            verify(repo).generateValue(D12)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm.state.getOrAwait()
            assertEquals(expected, (newState as HomeState.Viewing).rolls)
        }
    }

    @Test
    fun `roll a d20`() {
        runBlocking {
            //setup
            whenever(repo.generateValue(D20)).thenReturn(24)
            val expected = listOf(Result(D20, 24))

            //run
            vm.process(HomeEvent.Roll(D20))

            //check
            verify(repo).generateValue(D20)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm.state.getOrAwait()
            assertEquals(expected, (newState as HomeState.Viewing).rolls)
        }
    }

    @Test
    fun `roll a percent die`() {
        runBlocking {
            //setup
            whenever(repo.generateValue(D100)).thenReturn(124)
            val expected = listOf(Result(D100, 124), Text("%"))

            //run
            vm.process(HomeEvent.Roll(D100))

            //check
            verify(repo).generateValue(D100)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm.state.getOrAwait()
            assertEquals(expected, (newState as HomeState.Viewing).rolls)
        }
    }

    @Test
    fun `roll a die but there is a set selected(in theory this cannot happen)`() {
        runBlocking {
            //setup
            whenever(repo.buildNewSet("dummy name")).thenReturn(Reply.Success(TestData.SETS.random().toData()))
            vm.process(HomeEvent.NameSelected("dummy name"))
            val set = (vm.state.getOrAwait() as HomeState.Creating).set

            //run
            vm.process(HomeEvent.Roll(Die.values().random()))

            //check
            val newState = vm.state.getOrAwait()
            verify(repo, never()).generateRolls(any())
            assertTrue(newState is HomeState.Creating)
            assertEquals(set, (newState as HomeState.Creating).set)
        }
    }
    //</editor-fold>

    //<editor-fold desc="Roll Set Event">
    @Test
    fun `roll a set with a set already selected does nothing`() {
        runBlocking {
            whenever(repo.buildNewSet("dummy name")).thenReturn(Reply.Success(TestData.SETS.random().toData()))
            vm.process(HomeEvent.NameSelected("dummy name"))
            val set = (vm.state.getOrAwait() as HomeState.Creating).set

            vm.process(HomeEvent.RollSet(TestData.SETS.random().id))

            val newState = vm.state.getOrAwait()
            verify(repo, never()).generateRolls(any())
            assertTrue(newState is HomeState.Creating)
            assertEquals(set, (newState as HomeState.Creating).set)
        }
    }

    @Test
    fun `roll a set that doesn't exist shows a message`() {
        runBlocking {
            val id = UUID.randomUUID()
            whenever(repo.getSet(id)).thenReturn(Reply.Failure(32, Throwable("set with id $id not found")))

            vm.process(HomeEvent.RollSet(id))

            verify(repo).getSet(id)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            assertEquals(32, ((vm.effect.getOrAwait()) as HomeEffect.ShowError).id)
        }
    }

    @Test
    fun `roll a valid set updates the state correctly`() {
        runBlocking {
            val set = TestData.SETS.random().toData()
            whenever(repo.getSet(set.id)).thenReturn(Reply.Success(set))
            whenever(repo.generateRolls(set)).thenReturn(listOf(RollData(D4, 3), RollData(D12, 20)))
            val expected = listOf(Result(D4, 3), Text("+"), Result(D12, 20), Text("+"), Text(set.modifier.toString()), Text("="), Text((23 + set.modifier).toString()))

            vm.process(HomeEvent.RollSet(set.id))

            val rolls = (vm.state.getOrAwait() as HomeState.Viewing).rolls
            verify(repo).getSet(set.id)
            verify(repo).getSets()
            verify(repo).generateRolls(set)
            verifyNoMoreInteractions(repo)
            assertEquals(expected, rolls)
        }

    }
    //</editor-fold>

    //<editor-fold desc="Edit set event">
    @Test
    fun `edit existing set`() {
        runBlocking {
            val set = TestData.SETS.random().toData()
            whenever(repo.getSet(set.id)).thenReturn(Reply.Success(set))

            vm.process(HomeEvent.EditSet(set.id))

            assertEquals(set, (vm.state.getOrAwait() as HomeState.Editing).set)
            verify(repo).getSet(set.id)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
        }
    }

    @Test
    fun `edit set but it can't be found`() {
        runBlocking {
            val set = TestData.SETS.random().toData()
            whenever(repo.getSet(set.id)).thenReturn(Reply.Failure(33, Throwable("error and stuff")))

            vm.process(HomeEvent.EditSet(set.id))

            assertTrue(vm.state.getOrAwait() is HomeState.Viewing)
            verify(repo).getSet(set.id)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            assertEquals(33, (vm.effect.getOrAwait() as HomeEffect.ShowError).id)
        }
    }
    //</editor-fold>

    //<editor-fold desc="action 1">
    @Test
    fun `action 1 when viewing starts creating a new set`() {
        runBlocking {
            whenever(repo.getNextAvailableId()).thenReturn(32)

            vm.process(HomeEvent.Action1)

            verify(repo).getNextAvailableId()
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val effect = vm.effect.getOrAwait()
            assertTrue(effect is HomeEffect.ShowNameDialog)
            assertEquals(32, (effect as HomeEffect.ShowNameDialog).nextId)
        }
    }

    @Test
    fun `action 1 when editing saves current set`() {
        runBlocking {
            val data = TestData.SETS.random().toData()
            whenever(repo.saveSet(data)).thenReturn(Reply.Success(data))
            vm.privateState.postValue(HomeState.Editing(data))

            vm.process(HomeEvent.Action1)

            verify(repo).saveSet(data)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val state = vm.privateState.getOrAwait()
            assertTrue(state is HomeState.Viewing)
        }
    }

    @Test
    fun `action 1 when editing saves current set but it fails`() {
        runBlocking {
            val data = TestData.SETS.random().toData()
            vm.privateState.postValue(HomeState.Editing(data))
            whenever(repo.saveSet(data)).thenReturn(Reply.Failure(440))

            vm.process(HomeEvent.Action1)

            verify(repo).getSets()
            verify(repo).saveSet(data)
            verifyNoMoreInteractions(repo)
            val state = vm.privateState.getOrAwait()
            assertTrue(state is HomeState.Editing)
            assertEquals(data, (state as HomeState.Editing).set)
            val effect = vm.effect.getOrAwait()
            assertTrue(effect is HomeEffect.ShowError)
            assertEquals(440, (effect as HomeEffect.ShowError).id)
        }
    }

    @Test
    fun `action 1 when creating saves current set`() {
        runBlocking {
            val data = TestData.SETS.random().copy(id = EmptyUUID).toData()
            whenever(repo.saveSet(data)).thenReturn(Reply.Success(data))
            vm.privateState.postValue(HomeState.Creating(data))

            vm.process(HomeEvent.Action1)

            verify(repo).saveSet(data)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val state = vm.privateState.getOrAwait()
            assertTrue(state is HomeState.Viewing)
        }
    }

    @Test
    fun `action 1 when creating saves current set but it fails`() {
        runBlocking {
            val data = TestData.SETS.random().copy(id = EmptyUUID).toData()
            vm.privateState.postValue(HomeState.Creating(data))
            whenever(repo.saveSet(data)).thenReturn(Reply.Failure(440))

            vm.process(HomeEvent.Action1)

            verify(repo).getSets()
            verify(repo).saveSet(data)
            verifyNoMoreInteractions(repo)
            val state = vm.privateState.getOrAwait()
            assertTrue(state is HomeState.Creating)
            assertEquals(data, (state as HomeState.Creating).set)
            val effect = vm.effect.getOrAwait()
            assertTrue(effect is HomeEffect.ShowError)
            assertEquals(440, (effect as HomeEffect.ShowError).id)
        }
    }
    //</editor-fold>

    //<editor-fold desc="name has been selected while creating new set">
    @Test
    fun `name selected with no error`() {
        runBlocking {
            val set = TestData.SETS.random().toData()
            whenever(repo.buildNewSet("hello world")).thenReturn(Reply.Success(set))

            vm.process(HomeEvent.NameSelected("hello world"))

            val state = vm.state.getOrAwait()
            assertTrue(state is HomeState.Creating)
            assertEquals(set, (state as HomeState.Creating).set)
        }
    }

    @Test
    fun `name selected with error`() {
        runBlocking {
            whenever(repo.buildNewSet("hello world")).thenReturn(Reply.Failure(33))

            vm.process(HomeEvent.NameSelected("hello world"))

            val state = vm.state.getOrAwait()
            assertTrue(state is HomeState.Viewing)
            val effect = vm.effect.getOrAwait()
            assertEquals(33, (effect as HomeEffect.ShowError).id)
        }
    }
    //</editor-fold>

    //<editor-fold desc="roll transformations">
    @Test
    fun `update rolls a set works correctly`() {
        val rollData = listOf(RollData(D4, 5), RollData(D20, 32))
        val expected = listOf(
            Result(D4, 5), Text("+"), Result(D20, 32), Text("+"), Text(5.toString()),
            Text("="), Text(42.toString())
        )

        val rolls = vm.transform(rollData, 5)
        assertEquals(expected, rolls)
    }

    @Test
    fun `update rolls with no parameters clears the rolls`() {
        val rolls = vm.transform(listOf())

        assertEquals(0, rolls.size)
    }

    @Test
    fun `update rolls with only modifier clears the rolls`() {
        val rolls = vm.transform(listOf(), 100)

        assertEquals(0, rolls.size)
    }

    @Test
    fun `update rolls with percent and modifier`() {
        val rollData = listOf(RollData(D4, 5), RollData(D100, 132))
        val expected = listOf(
            Result(D4, 5), Text("+"), Result(D100, 132), Text("%"), Text("+"), Text(15.toString()),
            Text("="), Text(152.toString())
        )

        val rolls = vm.transform(rollData, 15)

        assertEquals(expected, rolls)
    }
    //</editor-fold>

}