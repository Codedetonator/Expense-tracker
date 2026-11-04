package com.sujal.expensetracker.ui.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sujal.expensetracker.data.ExpenseRepository
import com.sujal.expensetracker.data.entity.TripEntity
import kotlinx.coroutines.launch

class TripsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExpenseRepository(application)

    val allTrips = repo.allTrips

    fun add(name: String, budget: Double) = viewModelScope.launch {
        repo.addTrip(TripEntity(name = name, budget = budget))
    }
}
