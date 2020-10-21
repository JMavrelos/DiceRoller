package gr.blackswamp.diceroller.logic

import com.nhaarman.mockitokotlin2.*
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.TestData
import gr.blackswamp.diceroller.data.repos.HomeRepository
import gr.blackswamp.diceroller.data.repos.Reply
import gr.blackswamp.diceroller.data.repos.toData
import gr.blackswamp.diceroller.ui.model.Die
import gr.blackswamp.diceroller.ui.model.Die.*
import gr.blackswamp.diceroller.ui.model.HomeFragmentState
import gr.blackswamp.diceroller.ui.model.Roll.Result
import gr.blackswamp.diceroller.ui.model.Roll.Text
import gr.blackswamp.diceroller.uitls.KoinUnitTest
import gr.blackswamp.diceroller.uitls.getOrAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.*

@ExperimentalCoroutinesApi
class HomeViewModelTest : KoinUnitTest() {
    private val repo = mock<HomeRepository>()
    private val parent = mock<FragmentParent>()
    private lateinit var vm: HomeViewModel


    override val modules: Module = module {
        single { repo }
        single { Dispatchers.Default }
    }

    @Before
    override fun setup() {
        super.setup()
        vm = HomeViewModel(app, parent)
    }

    @Test
    fun `roll a d4`() {
        runBlocking {
            //setup
            vm._state.value = HomeFragmentState()
            whenever(repo.generateValue(D4)).thenReturn(24)
            val expected = HomeFragmentState(listOf(Result(D4, 24)))

            //run
            vm.roll(D4)

            //check
            verify(repo).generateValue(D4)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(expected, newState)
        }
    }

    @Test
    fun `roll a d6`() {
        runBlocking {
            //setup
            vm._state.value = HomeFragmentState()
            whenever(repo.generateValue(D4)).thenReturn(24)
            val expected = HomeFragmentState(listOf(Result(D4, 24)))

            //run
            vm.roll(D4)

            //check
            verify(repo).generateValue(D4)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(expected, newState)
        }
    }

    @Test
    fun `roll a d8`() {
        runBlocking {
            //setup
            vm._state.value = HomeFragmentState()
            whenever(repo.generateValue(D8)).thenReturn(24)
            val expected = HomeFragmentState(listOf(Result(D8, 24)))

            //run
            vm.roll(D8)

            //check
            verify(repo).generateValue(D8)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(expected, newState)
        }
    }

    @Test
    fun `roll a d10`() {
        runBlocking {
            //setup
            vm._state.value = HomeFragmentState()
            whenever(repo.generateValue(D10)).thenReturn(24)
            val expected = HomeFragmentState(listOf(Result(D10, 24)))

            //run
            vm.roll(D10)

            //check
            verify(repo).generateValue(D10)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(expected, newState)
        }
    }

    @Test
    fun `roll a d12`() {
        runBlocking {
            //setup
            vm._state.value = HomeFragmentState()
            whenever(repo.generateValue(D12)).thenReturn(24)
            val expected = HomeFragmentState(listOf(Result(D12, 24)))

            //run
            vm.roll(D12)

            //check
            verify(repo).generateValue(D12)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(expected, newState)
        }
    }

    @Test
    fun `roll a d20`() {
        runBlocking {
            //setup
            vm._state.value = HomeFragmentState()
            whenever(repo.generateValue(D20)).thenReturn(24)
            val expected = HomeFragmentState(listOf(Result(D20, 24)))

            //run
            vm.roll(D20)

            //check
            verify(repo).generateValue(D20)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(expected, newState)
        }
    }

    @Test
    fun `roll a percent die`() {
        runBlocking {
            //setup
            vm._state.value = HomeFragmentState()
            whenever(repo.generateValue(D100)).thenReturn(124)
            val expected = HomeFragmentState(listOf(Result(D100, 124), Text("%")))

            //run
            vm.roll(D100)

            //check
            verify(repo).generateValue(D100)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(expected, newState)
        }
    }

    @Test
    fun `roll a die but there is a set selected`() {
        runBlocking {
            //setup
            val set = TestData.SETS.random().toData()
            vm._state.value = HomeFragmentState(set = set)

            //run
            vm.roll(Die.values().random())

            //check
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(0, newState.rolls.size)
            assertFalse(newState.editing)
            assertEquals(set, newState.set)
        }
    }

    @Test
    fun `roll a set with a set already selected does nothing`() {
        runBlocking {
            val startState = HomeFragmentState(set = TestData.SETS.random().toData())
            vm._state.value = startState

            vm.rollSet(TestData.SETS.random().id)

            verify(repo, never()).generateRolls(any())
            assertEquals(startState, vm.state.getOrAwait())
        }
    }

    @Test
    fun `roll a set that doesn't exist shows a message`() {
        runBlocking {
            val id = UUID.randomUUID()
            vm._state.value = HomeFragmentState()
            whenever(repo.getSet(id)).thenReturn(Reply.failure(Throwable("set with id $id not found")))

            vm.rollSet(id)

            verify(repo).getSet(id)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            verify(parent).showError(R.string.error_loading_set)
        }
    }

    @Test
    fun `roll a valid set updates the state correctly`() {
        runBlocking {
            val set = TestData.SETS.random().toData()
            whenever(repo.getSet(set.id)).thenReturn(Reply.success(set))
            whenever(repo.generateRolls(set)).thenReturn(listOf(RollData(D4, 3), RollData(D12, 20)))
            val expected = listOf(Result(D4, 3), Text("+"), Result(D12, 20), Text("+"), Text(set.modifier.toString()), Text("="), Text((23 + set.modifier).toString()))

            vm.rollSet(set.id)

            val rolls = vm.state.getOrAwait().rolls
            verify(repo).getSet(set.id)
            verify(repo).getSets()
            verify(repo).generateRolls(set)
            verifyNoMoreInteractions(repo)
            assertEquals(expected, rolls)
        }

    }

    @Test
    fun `edit existing set`() {
        runBlocking {
            vm._state.value = HomeFragmentState(listOf(Result(D4, 3)), null, false)
            val set = TestData.SETS.random().toData()
            val expected = HomeFragmentState(listOf(), set, true)
            whenever(repo.getSet(set.id)).thenReturn(Reply.success(set))

            vm.editSet(set.id)

            assertEquals(expected, vm._state.getOrAwait())
            verify(repo).getSet(set.id)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
        }
    }

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

}