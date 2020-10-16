package gr.blackswamp.diceroller.logic

import androidx.annotation.StringRes

interface FragmentParent {
    fun showError(message: String)
    fun showError(@StringRes id: Int)
    fun showHelp()
}
