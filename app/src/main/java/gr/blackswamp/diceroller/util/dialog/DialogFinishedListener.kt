package gr.blackswamp.diceroller.util.dialog

import android.os.Bundle
import android.view.View

interface DialogFinishedListener {
    /**
     * informs the activity that the dialog with [id] has finished, [which] button was pressed, and if the [payload] that may have been provided at the start
     * if the implemented return value is true then the dialog is dismissed, otherwise it stays on the screen
     */
    fun onDialogFinished(id: Int, which: Int, dialog: View, payload: Bundle?): Boolean
}