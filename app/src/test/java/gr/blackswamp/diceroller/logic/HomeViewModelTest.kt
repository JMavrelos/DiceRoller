package gr.blackswamp.diceroller.logic

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import gr.blackswamp.diceroller.TestData
import gr.blackswamp.diceroller.data.repos.HomeRepository
import gr.blackswamp.diceroller.data.repos.toData
import gr.blackswamp.diceroller.ui.model.Die
import gr.blackswamp.diceroller.ui.model.HomeFragmentState
import gr.blackswamp.diceroller.ui.model.Roll
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
            whenever(repo.generateValue(Die.D4)).thenReturn(24)
            val expected = HomeFragmentState(listOf(Roll.Result(Die.D4, 24)))

            //run
            vm.roll(Die.D4)

            //check
            verify(repo).generateValue(Die.D4)
            verify(repo).getSets()
            verifyNoMoreInteractions(repo)
            val newState = vm._state.getOrAwait()
            assertEquals(expected, newState)
        }
    }

    @Test
    fun `roll a d4 but there is a set selected`() {
        runBlocking {
            //setup
            val set = TestData.SETS.random().toData()
            vm._state.value = HomeFragmentState(set = set)

            //run
            vm.roll(Die.D4)

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
    fun `update rolls works correctly`() {

    }
}