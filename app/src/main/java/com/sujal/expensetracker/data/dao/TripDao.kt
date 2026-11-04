package com.sujal.expensetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sujal.expensetracker.data.entity.TripEntity

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY isClosed ASC, createdDateMillis DESC")
    fun getAll(): LiveData<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    fun getById(tripId: Long): LiveData<TripEntity?>

    @Insert
    suspend fun insert(entity: TripEntity): Long

    @Update
    suspend fun update(entity: TripEntity)
}
