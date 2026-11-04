package com.sujal.expensetracker.ui.wallet

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujal.expensetracker.databinding.ItemDenominationBinding

/**
 * Shows one row per fixed denomination value. [rows] is an ordered list of
 * (value, count) pairs. [onCountChanged] fires whenever the user edits a count.
 */
class DenominationAdapter(
    private val onCountChanged: (value: Int, count: Int) -> Unit
) : ListAdapter<Pair<Int, Int>, DenominationAdapter.ViewHolder>(DIFF) {

    fun submitRows(newRows: List<Pair<Int, Int>>) = submitList(newRows)

    inner class ViewHolder(val binding: ItemDenominationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var boundValue: Int = 0

        init {
            // Attached ONCE per row (not on every bind), so retyping never has to juggle
            // listener add/remove — and it never fires for a count that came from this
            // same keystroke being echoed back in from outside.
            binding.editCount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val newCount = s?.toString()?.toIntOrNull() ?: 0
                    binding.textSubtotal.text = "₹${boundValue.toLong() * newCount}"
                    onCountChanged(boundValue, newCount)
                }
            })
        }

        fun bind(value: Int, count: Int) {
            boundValue = value
            binding.textValue.text = "₹$value"
            binding.textSubtotal.text = "₹${value.toLong() * count}"

            // Never overwrite a field the user is actively typing into — that's what was
            // wiping out keystrokes. Only sync it when it doesn't have focus right now.
            if (!binding.editCount.hasFocus()) {
                val current = binding.editCount.text?.toString()
                val countStr = if (count == 0) "" else count.toString()
                if (current != countStr) binding.editCount.setText(countStr)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDenominationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (value, count) = getItem(position)
        holder.bind(value, count)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Pair<Int, Int>>() {
            // Identity is the denomination value, not the count — so DiffUtil treats an
            // edited row as the same row (and only rebinds it) instead of a different one.
            override fun areItemsTheSame(a: Pair<Int, Int>, b: Pair<Int, Int>) = a.first == b.first
            override fun areContentsTheSame(a: Pair<Int, Int>, b: Pair<Int, Int>) = a == b
        }
    }
}
