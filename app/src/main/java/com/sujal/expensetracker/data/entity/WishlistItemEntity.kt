package com.sujal.expensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val createdDateMillis: Long = System.currentTimeMillis(),
    /** How much the user plans to set aside per week toward this item. 0 = not set. */
    val weeklyContribution: Double = 0.0
)
