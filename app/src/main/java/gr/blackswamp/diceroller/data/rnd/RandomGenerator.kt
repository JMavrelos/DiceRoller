package gr.blackswamp.diceroller.data.rnd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * I made this class to make suspend calls to a random generation api
 * for now it calls kotlin.Random, but in the future I may (though probably won't)
 * change it to calls to Random.org
 */
class RandomGenerator {
    /**
     * gets a number between 0 and [until] exclusive
     */
    private var currentSeed = System.currentTimeMillis()
    private var rnd = Random(currentSeed)

    suspend fun nextInt(until: Int): Int {
        return withContext(Dispatchers.IO) {
            val newSeed = System.currentTimeMillis()
            if (newSeed != currentSeed) {
                currentSeed = newSeed
                rnd = Random(newSeed)
            }
            rnd.nextInt(until)
        }
    }
}