package gr.blackswamp.diceroller.ui.dialogs

import android.view.LayoutInflater
import gr.blackswamp.diceroller.databinding.DialogHelpBinding
import gr.blackswamp.diceroller.util.DialogFragment

class HelpDialog : DialogFragment<DialogHelpBinding>() {
    override val positive: Int = android.R.string.ok
    override fun getBinding(inflater: LayoutInflater): DialogHelpBinding = DialogHelpBinding.inflate(inflater)
}