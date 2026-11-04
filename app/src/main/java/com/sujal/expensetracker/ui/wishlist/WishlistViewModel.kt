package com.sujal.expensetracker.ui.wishlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.sujal.expensetracker.data.ExpenseRepository
import com.sujal.expensetracker.data.entity.WishlistItemEntity
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.ceil

/** A wishlist item paired with what it'll take to afford it, computed against the
 * current total money and the weekly amount the user said they'd set aside. */
data class WishlistRow(
    val item: WishlistItemEntity,
    val totalMoney: Double
) {
    val progress: Float get() = if (item.price <= 0) 1f else (totalMoney / item.price).coerceIn(0.0, 1.0).toFloat()

    /** null means already affordable right now. */
    val affordByMillis: Long? get() {
        val remaining = item.price - totalMoney
        if (remaining <= 0) return null
        if (item.weeklyContribution <= 0) return null
        val weeksNeeded = ceil(remaining / item.weeklyContribution).toInt()
        return Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, weeksNeeded) }.timeInMillis
    }

    val canAffordNow: Boolean get() = totalMoney >= item.price
    val needsWeeklyAmount: Boolean get() = !canAffordNow && item.weeklyContribution <= 0
}

class WishlistViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExpenseRepository(application)

    val rows: LiveData<List<WishlistRow>> = MediatorLiveData<List<WishlistRow>>().apply {
        var items: List<WishlistItemEntity> = emptyList()
        var total = 0.0
        fun recompute() { value = items.map { WishlistRow(it, total) } }
        addSource(repo.allWishlistItems) { items = it; recompute() }
        addSource(repo.balance) { total = (it?.cashAmount ?: 0.0) + (it?.onlineAmount ?: 0.0); recompute() }
    }

    fun add(name: String, price: Double, weeklyContribution: Double) = viewModelScope.launch {
        repo.addWishlistItem(
            WishlistItemEntity(name = name, price = price, weeklyContribution = weeklyContribution)
        )
    }

    fun delete(item: WishlistItemEntity) = viewModelScope.launch {
        repo.deleteWishlistItem(item)
    }
}
