package gr.blackswamp.diceroller.data.rnd

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomGeneratorTest {
    private val gen = RandomGenerator()

    @Test
    fun `make a hundred calls and make sure they all are within range`() {
        runBlocking {
            for (iteration in 0..100) {
                val response = gen.nextInt(10)
                assertTrue("iteration $iteration failed with value $response", response < 10)
                assertTrue("iteration $iteration failed with value $response", response >= 0)
            }
        }
    }
}