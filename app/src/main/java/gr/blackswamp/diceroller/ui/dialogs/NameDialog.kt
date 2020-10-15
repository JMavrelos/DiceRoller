package gr.blackswamp.diceroller.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.databinding.DialogNameBinding
import gr.blackswamp.diceroller.util.value

class NameDialog : DialogFragment() {
    companion object {
        const val REQUEST_ID = "NameDialog_request"
        const val RESULT_NAME = "${REQUEST_ID}_name"
    }

    private lateinit var binding: DialogNameBinding

    private val name by lazy { binding.name }
    private val ok by lazy { binding.ok }
    private val cancel by lazy { binding.cancel }
    private val args by navArgs<NameDialogArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ok.setOnClickListener { finished(true) }
        cancel.setOnClickListener { finished(false) }
        name.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                finished(true)
            true
        }
        name.value = getString(R.string.default_set_name, args.nextId)
        dialog?.setCancelable(false)
        dialog?.setTitle(R.string.enter_set_name)
        dialog?.setCanceledOnTouchOutside(false)
    }

    private fun finished(submit: Boolean) {
        if (submit) {
            name.text?.toString()?.let {
                setFragmentResult(REQUEST_ID, bundleOf(RESULT_NAME to it))
            }
        }
        dismiss()
    }
}