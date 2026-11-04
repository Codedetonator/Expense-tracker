package com.sujal.expensetracker.ui.wallet

import android.app.Application
import androidx.lifecycle.*
import com.sujal.expensetracker.data.ExpenseRepository
import kotlinx.coroutines.launch

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExpenseRepository(application)

    val onlineBalance: LiveData<Double> = repo.balance.map { it?.onlineAmount ?: 0.0 }
    val cashBalance: LiveData<Double> = repo.balance.map { it?.cashAmount ?: 0.0 }

    val grandTotal: MediatorLiveData<Double> = MediatorLiveData<Double>().apply {
        var cash = 0.0
        var online = 0.0
        addSource(cashBalance) { cash = it; value = cash + online }
        addSource(onlineBalance) { online = it; value = cash + online }
    }

    fun setOnlineAmount(amount: Double) = viewModelScope.launch {
        repo.setOnlineAmount(amount)
    }

    fun setCashAmount(amount: Double) = viewModelScope.launch {
        repo.setCashAmount(amount)
    }

    // ---- Note/coin counting tool (separate from the cash balance above) ----

    /** One-shot read of the last saved note/coin counts, used to start a fresh draft. */
    fun loadDenominationCounts(onLoaded: (Map<Int, Int>) -> Unit) = viewModelScope.launch {
        onLoaded(repo.getDenominationCounts())
    }

    /** Saves the counts and sets Cash to their total. Only called when the user taps Update. */
    fun updateCashFromCount(counts: Map<Int, Int>) = viewModelScope.launch {
        repo.saveCountAndSetCash(counts)
    }
}
