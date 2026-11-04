package com.sujal.expensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A temporary shared wallet for a trip or event — e.g. a fest weekend or a group outing. */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Optional planned budget. 0 = no budget set. */
    val budget: Double = 0.0,
    val createdDateMillis: Long = System.currentTimeMillis(),
    val isClosed: Boolean = false
)
