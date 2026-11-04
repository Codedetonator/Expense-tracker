package com.sujal.expensetracker.ui.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.sujal.expensetracker.data.ExpenseRepository
import com.sujal.expensetracker.data.entity.TripExpenseEntity
import kotlinx.coroutines.launch

class TripDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExpenseRepository(application)
    private var tripId: Long = -1

    private val _personTotals = MutableLiveData<List<Pair<String, Double>>>(emptyList())
    val personTotals: LiveData<List<Pair<String, Double>>> = _personTotals

    lateinit var trip: LiveData<com.sujal.expensetracker.data.entity.TripEntity?>
        private set
    lateinit var expenses: LiveData<List<TripExpenseEntity>>
        private set

    val totalSpent: LiveData<Double> get() = expenses.map { list -> list.sumOf { it.amount } }

    fun init(tripId: Long) {
        if (this.tripId == tripId) return
        this.tripId = tripId
        trip = repo.getTrip(tripId)
        expenses = repo.getTripExpenses(tripId)
    }

    fun addExpense(description: String, amount: Double, paidBy: String) = viewModelScope.launch {
        repo.addTripExpense(
            TripExpenseEntity(tripId = tripId, description = description, amount = amount, paidBy = paidBy)
        )
        refreshTotals()
    }

    fun deleteExpense(entity: TripExpenseEntity) = viewModelScope.launch {
        repo.deleteTripExpense(entity)
        refreshTotals()
    }

    fun closeTrip() = viewModelScope.launch {
        trip.value?.let { repo.closeTrip(it) }
    }

    fun refreshTotals() = viewModelScope.launch {
        _personTotals.value = repo.getTripTotalsByPerson(tripId)
    }
}
