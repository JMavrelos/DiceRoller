package gr.blackswamp.diceroller.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.core.Logger
import gr.blackswamp.diceroller.core.widget.value
import gr.blackswamp.diceroller.databinding.ListItemModBinding
import gr.blackswamp.diceroller.databinding.ListItemResultBinding
import gr.blackswamp.diceroller.ui.model.Die
import gr.blackswamp.diceroller.ui.model.Roll

class RollAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rolls = mutableListOf<Roll>()

    fun submit(newRolls: List<Roll>?) {
        if (rolls == newRolls)
            return
        val diff = RollDiff(rolls.toList(), newRolls ?: listOf())
        rolls.clear()
        rolls.addAll(newRolls ?: listOf())
        Logger.log { "New rolls $newRolls" }
        DiffUtil.calculateDiff(diff).dispatchUpdatesTo(this)
    }

    private fun getRoll(position: Int): Roll? {
        return if (position < rolls.size && position >= 0)
            rolls[position]
        else
            null
    }

    override fun getItemCount(): Int = rolls.size

    override fun getItemViewType(position: Int): Int = when (rolls[position]) {
        is Roll.Result -> R.layout.list_item_result
        is Roll.Text -> R.layout.list_item_mod
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.list_item_mod -> ModViewHolder(ListItemModBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.list_item_result -> ResultViewHolder(ListItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw Throwable("Invalid view id $viewType")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getRoll(position)?.let {
            when (it) {
                is Roll.Text -> (holder as ModViewHolder).update(it)
                is Roll.Result -> (holder as ResultViewHolder).update(it)
            }
        }
    }


    class ModViewHolder(binding: ListItemModBinding) : RecyclerView.ViewHolder(binding.root) {
        val text = binding.root
        fun update(roll: Roll.Text) {
            text.value = roll.text
        }
    }

    class ResultViewHolder(binding: ListItemResultBinding) : RecyclerView.ViewHolder(binding.root) {
        private val root = binding.root
        private val value = binding.value
        private val die = binding.die

        fun update(roll: Roll.Result) {
            val text = "${roll.value}"
            value.value = text
            val set = ConstraintSet()
            set.clone(root)
            val bias: Float
            val resId: Int
            when (roll.die) {
                Die.D4 -> {
                    resId = R.drawable.ic_d4
                    bias = 0.7f
                }
                Die.D6 -> {
                    resId = R.drawable.ic_d6
                    bias = 0.5f
                }
                Die.D8 -> {
                    resId = R.drawable.ic_d8
                    bias = 0.5f
                }
                Die.D10 -> {
                    resId = R.drawable.ic_d10
                    bias = 0.2f
                }
                Die.D12 -> {
                    resId = R.drawable.ic_d12
                    bias = 0.4f
                }
                Die.D20 -> {
                    resId = R.drawable.ic_d20
                    bias = 0.5f
                }
                Die.D100 -> {
                    resId = -1
                    bias = 0.5f
                }
            }
            set.setVerticalBias(R.id.value, bias)
            set.applyTo(root)
            if (resId == -1)
                die.setImageDrawable(null)
            else
                die.setImageResource(resId)
        }
    }

    private class RollDiff(private val old: List<Roll>, private val new: List<Roll>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = false

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = old[oldItemPosition]
            val new = new[newItemPosition]
            return (old is Roll.Text && new is Roll.Text && old.text == new.text) ||
                    (old is Roll.Result && new is Roll.Result && old.die == new.die && old.value == new.value)
        }
    }

}
