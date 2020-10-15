package gr.blackswamp.diceroller.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
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

    private val args by navArgs<NameDialogArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        binding = DialogNameBinding.inflate(LayoutInflater.from(context))
        name.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                finished(true)
            true
        }
        if (savedInstanceState == null) {
            name.value = getString(R.string.default_set_name, args.nextId)
        }

        val builder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .setCancelable(false)
        val dialog = builder.create()

        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener {
            name.selectAll()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { finished(true) }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { finished(false) }
        }
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)
        return dialog
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