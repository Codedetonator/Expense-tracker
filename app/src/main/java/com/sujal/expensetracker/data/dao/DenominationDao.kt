package com.sujal.expensetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sujal.expensetracker.data.entity.DenominationEntity

@Dao
interface DenominationDao {

    @Query("SELECT * FROM denominations ORDER BY value DESC")
    fun getAll(): LiveData<List<DenominationEntity>>

    @Query("SELECT * FROM denominations")
    suspend fun getAllOnce(): List<DenominationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<DenominationEntity>)
}
