package gr.blackswamp.diceroller.core.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group

class Group2 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Group(context, attrs, defStyleAttr) {


    public override fun applyLayoutFeatures() {
        val parent = (this.parent as? ConstraintLayout) ?: return
        this.applyLayoutFeatures(parent)
    }

    override fun applyLayoutFeatures(container: ConstraintLayout) {
        val visibility = this.visibility
        val enabled = this.isEnabled
        for (i in 0 until mCount) {
            val id = mIds[i]
            val view = container.getViewById(id)
            if (view != null) {
                view.visibility = visibility
                view.enabled = enabled
                if (elevation > 0.0f) {
                    view.translationZ = view.translationZ + elevation
                }
            }
        }
    }
}