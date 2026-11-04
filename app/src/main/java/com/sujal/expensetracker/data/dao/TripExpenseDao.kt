package com.sujal.expensetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sujal.expensetracker.data.entity.TripExpenseEntity

@Dao
interface TripExpenseDao {

    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId ORDER BY dateMillis DESC")
    fun getForTrip(tripId: Long): LiveData<List<TripExpenseEntity>>

    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId")
    suspend fun getForTripOnce(tripId: Long): List<TripExpenseEntity>

    @Insert
    suspend fun insert(entity: TripExpenseEntity)

    @Delete
    suspend fun delete(entity: TripExpenseEntity)

    @Query("DELETE FROM trip_expenses WHERE tripId = :tripId")
    suspend fun deleteAllForTrip(tripId: Long)
}
