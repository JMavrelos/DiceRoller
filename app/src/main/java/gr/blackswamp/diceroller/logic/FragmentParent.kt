package gr.blackswamp.diceroller.logic

import gr.blackswamp.diceroller.data.repos.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

interface FragmentParent {
    private val supervisor = SupervisorJob()
    override val coroutineContext: CoroutineContext = supervisor + Dispatchers.Main.immediate
    private val repo by inject<HomeRepository>()

}
