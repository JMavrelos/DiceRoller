package gr.blackswamp.diceroller.ui.dialogs

import android.view.LayoutInflater
import gr.blackswamp.diceroller.core.ui.DialogFragment
import gr.blackswamp.diceroller.databinding.DialogHelpBinding

class HelpDialog : DialogFragment<DialogHelpBinding>() {
    override val positive: Int = android.R.string.ok
    override fun getBinding(inflater: LayoutInflater): DialogHelpBinding = DialogHelpBinding.inflate(inflater)
}