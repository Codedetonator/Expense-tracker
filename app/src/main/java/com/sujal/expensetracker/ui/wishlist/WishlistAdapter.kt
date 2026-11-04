package com.sujal.expensetracker.ui.wishlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujal.expensetracker.R
import com.sujal.expensetracker.data.entity.WishlistItemEntity
import com.sujal.expensetracker.databinding.ItemWishlistBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WishlistAdapter(
    private val onDelete: (WishlistItemEntity) -> Unit
) : ListAdapter<WishlistRow, WishlistAdapter.ViewHolder>(DIFF) {

    private val moneyFormat = DecimalFormat("₹#,##0.##")
    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    inner class ViewHolder(val binding: ItemWishlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = getItem(position)
        with(holder.binding) {
            textName.text = row.item.name
            textPrice.text = moneyFormat.format(row.item.price)
            progressBar.max = 1000
            progressBar.progress = (row.progress * 1000).toInt()

            textAfford.text = when {
                row.canAffordNow -> root.context.getString(R.string.you_can_afford_now)
                row.needsWeeklyAmount -> {
                    val remaining = row.item.price - row.totalMoney
                    "${moneyFormat.format(remaining)} ${root.context.getString(R.string.add_more_needed)} — ${root.context.getString(R.string.add_weekly_hint)}"
                }
                else -> {
                    val dateStr = dateFormat.format(Date(row.affordByMillis ?: 0L))
                    root.context.getString(R.string.afford_by, dateStr, row.item.weeklyContribution.toInt().toString())
                }
            }

            btnDelete.setOnClickListener { onDelete(row.item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WishlistRow>() {
            override fun areItemsTheSame(a: WishlistRow, b: WishlistRow) = a.item.id == b.item.id
            override fun areContentsTheSame(a: WishlistRow, b: WishlistRow) = a == b
        }
    }
}
