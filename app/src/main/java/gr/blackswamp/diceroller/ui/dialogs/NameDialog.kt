package gr.blackswamp.diceroller.ui.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.core.ui.DialogFragment
import gr.blackswamp.diceroller.core.widget.value
import gr.blackswamp.diceroller.databinding.DialogNameBinding

class NameDialog : DialogFragment<DialogNameBinding>() {
    companion object {
        const val REQUEST_ID = "NameDialog_request"
        const val RESULT_NAME = "${REQUEST_ID}_name"
    }

    override fun getBinding(inflater: LayoutInflater): DialogNameBinding =
        DialogNameBinding.inflate(inflater)

    private val args by navArgs<NameDialogArgs>()
    override val positive: Int = android.R.string.ok
    override val negative: Int = android.R.string.cancel
    override val cancelable: Boolean = false
    private val name by lazy { binding.name }

    override fun dialogCreated(root: View) {
        name.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                dialogFinished(DialogResult.Positive)
            true
        }
    }

    override fun dialogInitialized() {
        name.value = getString(R.string.default_set_name, args.nextId)
    }

    override fun dialogShown() {
        name.requestFocus()
        name.selectAll()
    }

    override fun dialogFinished(result: DialogResult) {
        if (result == DialogResult.Positive) {
            name.text?.toString()?.let {
                setFragmentResult(REQUEST_ID, bundleOf(RESULT_NAME to it))
            }
        }
        super.dialogFinished(result)
    }
}