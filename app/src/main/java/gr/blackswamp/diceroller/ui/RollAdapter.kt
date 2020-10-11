package gr.blackswamp.diceroller.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.databinding.ListItemRollBinding
import timber.log.Timber

class RollAdapter : RecyclerView.Adapter<RollAdapter.RollViewHolder>() {
    private val rolls = mutableListOf<Roll>()


    fun submit(newRolls: List<Roll>?) {
        if (rolls == newRolls)
            return
        val diff = RollDiff(rolls.toList(), newRolls ?: listOf())
        rolls.clear()
        rolls.addAll(newRolls ?: listOf())
        Timber.d("New rolls $newRolls")
        DiffUtil.calculateDiff(diff).dispatchUpdatesTo(this)
    }


    override fun getItemCount(): Int = rolls.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RollViewHolder =
        RollViewHolder(ListItemRollBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RollViewHolder, position: Int) {
        holder.update(rolls[position])
    }

    class RollViewHolder(binding: ListItemRollBinding) : RecyclerView.ViewHolder(binding.root) {
        private val root = binding.root
        private val value = binding.value
        private val die = binding.die


        fun update(roll: Roll) {
            value.text = roll.value.toString()
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
            }
            set.setVerticalBias(R.id.value, bias)
            set.applyTo(root)
            die.setImageResource(resId)

        }
    }

    private class RollDiff(private val old: List<Roll>, private val new: List<Roll>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = false

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition] == new[newItemPosition]
    }

}
