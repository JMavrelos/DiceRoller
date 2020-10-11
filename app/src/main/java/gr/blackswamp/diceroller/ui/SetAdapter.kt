package gr.blackswamp.diceroller.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import gr.blackswamp.diceroller.databinding.ListItemSetBinding
import java.util.*

class SetAdapter(private val listener: (UUID) -> Unit) : RecyclerView.Adapter<SetAdapter.SetViewHolder>() {
    private val sets = mutableListOf<DieSetHeader>()


    fun submit(newSets: List<DieSetHeader>?) {
        val diff = DieSetHeaderDiff(sets, newSets ?: listOf())
        sets.clear()
        sets.addAll(newSets ?: listOf())
        DiffUtil.calculateDiff(diff).dispatchUpdatesTo(this)

    }

    private fun itemClicked(position: Int) {
        listener.invoke(sets[position].id)
    }


    override fun getItemCount(): Int = sets.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder =
        SetViewHolder(ListItemSetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.update(sets[position])
    }

    inner class SetViewHolder(binding: ListItemSetBinding) : RecyclerView.ViewHolder(binding.root) {
        val name = binding.name

        init {
            binding.root.setOnClickListener {
                itemClicked(adapterPosition)
            }
        }

        fun update(set: DieSetHeader) {

            name.text = set.name
        }
    }

    private class DieSetHeaderDiff(private val old: List<DieSetHeader>, private val new: List<DieSetHeader>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition].id == new[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            areItemsTheSame(oldItemPosition, newItemPosition) && old[oldItemPosition].name == new[newItemPosition].name


    }

}
