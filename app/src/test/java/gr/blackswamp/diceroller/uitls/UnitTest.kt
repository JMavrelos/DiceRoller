package gr.blackswamp.diceroller.uitls

import android.app.Application
import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
abstract class UnitTest {
    companion object {
        const val APP_STRING = "message"
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    protected open val stringInjections = mapOf<Int, String>()
    protected val app: Application = mock(Application::class.java)
    private val dispatcher = TestCoroutineDispatcher()

    @Before
    @CallSuper
    open fun setup() {
        reset(app)
        setUpApplicationMocks()
        Dispatchers.setMain(dispatcher)
    }

    private fun setUpApplicationMocks() {
        whenever(app.getString(anyInt())).then { mock ->
            val id = mock.arguments.first() as Int
            stringInjections.entries.firstOrNull { it.key == id }?.value ?: APP_STRING
        }
        whenever(app.getString(anyInt(), any())).thenReturn(APP_STRING)
    }

    @After
    @CallSuper
    open fun tearDown() {
        Dispatchers.resetMain()
        dispatcher.cleanupTestCoroutines()
    }

}