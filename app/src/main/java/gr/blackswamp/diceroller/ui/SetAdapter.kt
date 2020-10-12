package gr.blackswamp.diceroller.ui

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.databinding.ListItemSetBinding
import java.util.*

class SetAdapter(private val listener: (UUID) -> Unit, private val longClickListener: (UUID) -> Unit) : RecyclerView.Adapter<SetAdapter.SetViewHolder>() {
    private val sets = mutableListOf<DieSetHeader>()
    private var selected = -1
    private val colors = mutableMapOf<Int, Int>()

    private fun getColor(ctx: Context, @AttrRes attr: Int): Int {
        val color = colors[attr]
        if (color != null)
            return color
        val typed = TypedValue()
        ctx.theme.resolveAttribute(attr, typed, true)
        val resolved = typed.data
        colors[attr] = resolved
        return resolved
    }

    fun submit(newSets: List<DieSetHeader>?) {
        val diff = DieSetHeaderDiff(sets.toList(), newSets ?: listOf())
        sets.clear()
        sets.addAll(newSets ?: listOf())
        DiffUtil.calculateDiff(diff).dispatchUpdatesTo(this)

    }

    private fun itemLongClick(position: Int) {
        longClickListener.invoke(sets[position].id)
    }

    private fun itemClick(position: Int) {
        listener.invoke(sets[position].id)
    }


    override fun getItemCount(): Int = sets.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder =
        SetViewHolder(ListItemSetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.update(sets[position], position == selected)
    }

    fun setSelected(id: UUID?) {
        val old = selected
        val new = sets.indexOfFirst { it.id == id }
        if (old == new)
            return
        selected = new

        if (old != -1)
            notifyItemChanged(old)
        if (new != -1)
            notifyItemChanged(new)
    }

    inner class SetViewHolder(binding: ListItemSetBinding) : RecyclerView.ViewHolder(binding.root) {
        private val root = binding.root
        private val name = binding.name

        init {
            binding.root.setOnClickListener {
                itemClick(adapterPosition)
            }
            binding.root.setOnLongClickListener {
                itemLongClick(adapterPosition)
                true
            }
        }

        fun update(set: DieSetHeader, selected: Boolean) {
            name.text = set.name
            if (selected) {
                root.setCardBackgroundColor(getColor(root.context, R.attr.colorPrimary))
                name.setTextColor(getColor(root.context, R.attr.colorOnPrimary))
            } else {
                root.setCardBackgroundColor(getColor(root.context, R.attr.colorSurface))
                name.setTextColor(getColor(root.context, R.attr.colorOnSurface))
            }
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
