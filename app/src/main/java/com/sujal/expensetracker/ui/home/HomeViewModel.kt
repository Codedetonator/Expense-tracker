package com.sujal.expensetracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.sujal.expensetracker.data.ExpenseRepository
import com.sujal.expensetracker.data.entity.ExpenseEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Everything the Home screen needs, computed from the full expense list. */
data class HomeStats(
    val monthTotal: Double,
    val lastMonthTotal: Double,
    val todayTotal: Double,
    /** Last 7 days, oldest first; the final entry is today. */
    val weekLabels: List<String>,
    val weekValues: List<Double>,
    /** This month's spend per category, biggest first. */
    val categoryTotals: List<Pair<String, Double>>,
    val recent: List<ExpenseEntity>
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExpenseRepository(application)

    val stats: LiveData<HomeStats> = repo.allExpenses.map { computeHomeStats(it) }

    /** Cash + online, i.e. all the money you currently have. */
    val totalMoney: LiveData<Double> = repo.balance.map {
        (it?.cashAmount ?: 0.0) + (it?.onlineAmount ?: 0.0)
    }
}

private fun dayStart(daysAgo: Int): Long = Calendar.getInstance().apply {
    add(Calendar.DAY_OF_MONTH, -daysAgo)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun monthStart(monthsAgo: Int): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_MONTH, 1)
    add(Calendar.MONTH, -monthsAgo)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/** [expenses] is expected newest-first (that's how the DAO returns it). */
fun computeHomeStats(expenses: List<ExpenseEntity>): HomeStats {
    val thisMonthStart = monthStart(0)
    val lastMonthStart = monthStart(1)
    val todayStart = dayStart(0)

    val thisMonth = expenses.filter { it.dateMillis >= thisMonthStart }
    val lastMonthTotal = expenses
        .filter { it.dateMillis in lastMonthStart until thisMonthStart }
        .sumOf { it.amount }

    val labelFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val weekLabels = mutableListOf<String>()
    val weekValues = mutableListOf<Double>()
    for (offset in 6 downTo 0) {
        val start = dayStart(offset)
        val end = if (offset == 0) Long.MAX_VALUE else dayStart(offset - 1)
        weekLabels.add(labelFormat.format(Date(start)))
        weekValues.add(expenses.filter { it.dateMillis in start until end }.sumOf { it.amount })
    }

    val categoryTotals = thisMonth
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
        .filterValues { it > 0 }
        .toList()
        .sortedByDescending { it.second }

    return HomeStats(
        monthTotal = thisMonth.sumOf { it.amount },
        lastMonthTotal = lastMonthTotal,
        todayTotal = expenses.filter { it.dateMillis >= todayStart }.sumOf { it.amount },
        weekLabels = weekLabels,
        weekValues = weekValues,
        categoryTotals = categoryTotals,
        recent = expenses.take(5)
    )
}
