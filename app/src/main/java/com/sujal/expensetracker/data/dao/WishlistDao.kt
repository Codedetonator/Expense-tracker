package com.sujal.expensetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sujal.expensetracker.data.entity.WishlistItemEntity

@Dao
interface WishlistDao {

    @Query("SELECT * FROM wishlist_items ORDER BY createdDateMillis DESC")
    fun getAll(): LiveData<List<WishlistItemEntity>>

    @Insert
    suspend fun insert(entity: WishlistItemEntity)

    @Delete
    suspend fun delete(entity: WishlistItemEntity)
}
