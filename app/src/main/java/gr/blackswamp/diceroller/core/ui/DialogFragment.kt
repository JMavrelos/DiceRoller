package gr.blackswamp.diceroller.core.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import gr.blackswamp.diceroller.R

abstract class DialogFragment<T : ViewBinding> : DialogFragment() {
    @StringRes
    protected open val positive: Int = -1

    @StringRes
    protected open val negative: Int = -1

    @StringRes
    protected open val neutral: Int = -1

    protected open val cancelable: Boolean = true

    @DrawableRes
    protected open val backgroundResource: Int = R.drawable.bg_dialog
    protected lateinit var binding: T

    protected abstract fun getBinding(inflater: LayoutInflater): T


    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = getBinding(LayoutInflater.from(context))
        dialogCreated(binding.root)
        if (savedInstanceState == null)
            dialogInitialized()

        val builder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(cancelable)
        if (positive > 0)
            builder.setPositiveButton(positive, null)
        if (negative > 0)
            builder.setNegativeButton(negative, null)
        if (neutral > 0)
            builder.setNeutralButton(neutral, null)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(cancelable)
        dialog.setOnCancelListener {
            dialogFinished(DialogResult.Cancel)
        }
        dialog.setOnShowListener {
            if (positive > 0)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { dialogFinished(DialogResult.Positive) }
            if (negative > 0)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dialogFinished(DialogResult.Negative) }
            if (neutral > 0)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dialogFinished(DialogResult.Neutral) }
            dialogShown()
        }
        if (backgroundResource > 0)
            dialog.window?.setBackgroundDrawableResource(backgroundResource)
        return dialog
    }


    /**
     * runs this code every time the dialog is created
     */
    protected open fun dialogCreated(root: View) {}

    /**
     * runs this code only the first time the dialog is created
     */
    protected open fun dialogInitialized() {}

    /**
     * runs this code every time the dialog is shown
     */
    protected open fun dialogShown() {}

    /**
     * uses this to deliver the dialog's results
     */
    protected open fun dialogFinished(result: DialogResult) {
        this.dismiss()
    }

    protected enum class DialogResult {
        Positive,
        Negative,
        Neutral,
        Cancel
    }
}