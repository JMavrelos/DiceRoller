package gr.blackswamp.diceroller.core.widget

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible

var View.visible
    get() = this.isVisible
    set(value) {
        if (this.isVisible && !value) {
            this.isVisible = false
        } else if (!this.isVisible && value) {
            this.isVisible = true
        }
    }

var View.enabled
    get() = this.isEnabled
    set(value) {
        if (isEnabled && !value) {
            isEnabled = false
            if (this is Group2) this.applyLayoutFeatures()
        } else if (!isEnabled && value) {
            isEnabled = true
            if (this is Group2) this.applyLayoutFeatures()
        }
    }

var TextView.value: String
    get() = this.text?.toString() ?: ""
    set(value) {
        if (this.text != value)
            text = value
    }

var TextView.res: Int
    get() = throw RuntimeException("String resource cannot be retrieved after being set")
    set(value) {
        this.text = this.context.getText(value)
    }
