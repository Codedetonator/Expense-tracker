package com.sujal.expensetracker.ui.trips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujal.expensetracker.data.entity.TripExpenseEntity
import com.sujal.expensetracker.databinding.ItemTripExpenseBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripExpenseAdapter(
    private val onDelete: (TripExpenseEntity) -> Unit
) : ListAdapter<TripExpenseEntity, TripExpenseAdapter.ViewHolder>(DIFF) {

    private val moneyFormat = DecimalFormat("₹#,##0.##")
    private val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    var deletable: Boolean = true

    inner class ViewHolder(val binding: ItemTripExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemTripExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            textDescription.text = item.description
            textPaidBy.text = "${item.paidBy} · ${dateFormat.format(Date(item.dateMillis))}"
            textAmount.text = moneyFormat.format(item.amount)
            btnDelete.visibility = if (deletable) android.view.View.VISIBLE else android.view.View.GONE
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TripExpenseEntity>() {
            override fun areItemsTheSame(a: TripExpenseEntity, b: TripExpenseEntity) = a.id == b.id
            override fun areContentsTheSame(a: TripExpenseEntity, b: TripExpenseEntity) = a == b
        }
    }
}
