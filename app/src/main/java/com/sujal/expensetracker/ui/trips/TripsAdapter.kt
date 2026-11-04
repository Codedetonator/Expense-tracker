package com.sujal.expensetracker.ui.trips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujal.expensetracker.R
import com.sujal.expensetracker.data.entity.TripEntity
import com.sujal.expensetracker.databinding.ItemTripBinding
import java.text.DecimalFormat

class TripsAdapter(
    private val onClick: (TripEntity) -> Unit
) : ListAdapter<TripEntity, TripsAdapter.ViewHolder>(DIFF) {

    private val moneyFormat = DecimalFormat("₹#,##0.##")

    inner class ViewHolder(val binding: ItemTripBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = getItem(position)
        with(holder.binding) {
            textName.text = trip.name
            textSubtitle.text = if (trip.budget > 0)
                "Budget ${moneyFormat.format(trip.budget)}"
            else
                root.context.getString(R.string.tab_trips)
            textStatus.text = if (trip.isClosed) root.context.getString(R.string.closed) else root.context.getString(R.string.open)
            root.setOnClickListener { onClick(trip) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TripEntity>() {
            override fun areItemsTheSame(a: TripEntity, b: TripEntity) = a.id == b.id
            override fun areContentsTheSame(a: TripEntity, b: TripEntity) = a == b
        }
    }
}
