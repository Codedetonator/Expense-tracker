package com.sujal.expensetracker.data

import android.content.Context
import androidx.room.withTransaction
import com.sujal.expensetracker.data.entity.BalanceEntity
import com.sujal.expensetracker.data.entity.DenominationEntity
import com.sujal.expensetracker.data.entity.ExpenseEntity
import com.sujal.expensetracker.data.entity.LendingEntity
import com.sujal.expensetracker.data.entity.PaymentMethod
import com.sujal.expensetracker.data.entity.TripEntity
import com.sujal.expensetracker.data.entity.TripExpenseEntity
import com.sujal.expensetracker.data.entity.WishlistItemEntity
import com.sujal.expensetracker.widget.WidgetUpdater

class ExpenseRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.getInstance(context)
    private val denominationDao = db.denominationDao()
    private val balanceDao = db.balanceDao()
    private val lendingDao = db.lendingDao()
    private val expenseDao = db.expenseDao()
    private val wishlistDao = db.wishlistDao()
    private val tripDao = db.tripDao()
    private val tripExpenseDao = db.tripExpenseDao()

    // Denominations — the "count my cash" tool. Counts are only written when the user taps Update.
    suspend fun getDenominationCounts(): Map<Int, Int> =
        denominationDao.getAllOnce().associate { it.value to it.count }

    /** Saves the whole note/coin count and sets the Cash balance to its total, in one transaction. */
    suspend fun saveCountAndSetCash(counts: Map<Int, Int>) {
        db.withTransaction {
            denominationDao.upsertAll(counts.map { (value, count) -> DenominationEntity(value, count) })
            setCashAmount(counts.entries.sumOf { it.key.toDouble() * it.value })
        }
    }

    // Balances
    val balance = balanceDao.get()

    suspend fun setOnlineAmount(amount: Double) {
        val current = balanceDao.getOnce() ?: BalanceEntity()
        balanceDao.upsert(current.copy(onlineAmount = amount))
        WidgetUpdater.notifyChanged(appContext)
    }

    /** Adds [delta] to the online balance. Pass a negative delta to deduct. */
    suspend fun adjustOnline(delta: Double) {
        val current = balanceDao.getOnce() ?: BalanceEntity()
        balanceDao.upsert(current.copy(onlineAmount = current.onlineAmount + delta))
    }

    /** Sets the cash balance directly — used when you type a number, or when you sync it
     * from the note/coin counting tool. */
    suspend fun setCashAmount(amount: Double) {
        val current = balanceDao.getOnce() ?: BalanceEntity()
        balanceDao.upsert(current.copy(cashAmount = amount))
        WidgetUpdater.notifyChanged(appContext)
    }

    /** Adds [delta] to the cash balance. Pass a negative delta to deduct. */
    suspend fun adjustCash(delta: Double) {
        val current = balanceDao.getOnce() ?: BalanceEntity()
        balanceDao.upsert(current.copy(cashAmount = current.cashAmount + delta))
    }

    // Lending
    val allLending = lendingDao.getAll()
    suspend fun addLending(entity: LendingEntity) = lendingDao.insert(entity)
    suspend fun updateLending(entity: LendingEntity) = lendingDao.update(entity)
    suspend fun deleteLending(entity: LendingEntity) = lendingDao.delete(entity)

    // Expenses
    val allExpenses = expenseDao.getAll()

    /** Logs a spend and deducts it from the matching balance. */
    suspend fun addExpense(entity: ExpenseEntity) {
        db.withTransaction {
            expenseDao.insert(entity)
            if (entity.paymentMethod == PaymentMethod.ONLINE) adjustOnline(-entity.amount)
            else adjustCash(-entity.amount)
        }
        WidgetUpdater.notifyChanged(appContext)
    }

    /** Deletes an expense and refunds its amount back to the balance it was deducted from. */
    suspend fun deleteExpense(entity: ExpenseEntity) {
        db.withTransaction {
            expenseDao.delete(entity)
            if (entity.paymentMethod == PaymentMethod.ONLINE) adjustOnline(entity.amount)
            else adjustCash(entity.amount)
        }
        WidgetUpdater.notifyChanged(appContext)
    }

    // Wishlist
    val allWishlistItems = wishlistDao.getAll()
    suspend fun addWishlistItem(entity: WishlistItemEntity) = wishlistDao.insert(entity)
    suspend fun deleteWishlistItem(entity: WishlistItemEntity) = wishlistDao.delete(entity)

    // Trips — temporary shared wallets for a fest, outing or trip.
    val allTrips = tripDao.getAll()
    fun getTrip(tripId: Long) = tripDao.getById(tripId)
    fun getTripExpenses(tripId: Long) = tripExpenseDao.getForTrip(tripId)

    suspend fun addTrip(entity: TripEntity): Long = tripDao.insert(entity)
    suspend fun closeTrip(entity: TripEntity) = tripDao.update(entity.copy(isClosed = true))

    suspend fun addTripExpense(entity: TripExpenseEntity) = tripExpenseDao.insert(entity)
    suspend fun deleteTripExpense(entity: TripExpenseEntity) = tripExpenseDao.delete(entity)

    /** Total paid by each person in a trip, biggest first. */
    suspend fun getTripTotalsByPerson(tripId: Long): List<Pair<String, Double>> =
        tripExpenseDao.getForTripOnce(tripId)
            .groupBy { it.paidBy }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

    // One-shot reads for the home-screen widget, which can run before anything in the app
    // is observing the LiveData above (e.g. right after the process was killed).
    suspend fun getTotalMoneyOnce(): Double {
        val balance = balanceDao.getOnce()
        return (balance?.cashAmount ?: 0.0) + (balance?.onlineAmount ?: 0.0)
    }

    suspend fun getTodaySpentOnce(): Double {
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return expenseDao.getSince(todayStart).sumOf { it.amount }
    }
}