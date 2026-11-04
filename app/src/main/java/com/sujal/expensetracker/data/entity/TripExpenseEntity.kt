package com.sujal.expensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One spend within a trip's shared wallet, with who paid for it. */
@Entity(tableName = "trip_expenses")
data class TripExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val dateMillis: Long = System.currentTimeMillis()
)
