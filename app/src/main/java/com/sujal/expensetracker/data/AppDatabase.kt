package com.sujal.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sujal.expensetracker.data.dao.BalanceDao
import com.sujal.expensetracker.data.dao.DenominationDao
import com.sujal.expensetracker.data.dao.ExpenseDao
import com.sujal.expensetracker.data.dao.LendingDao
import com.sujal.expensetracker.data.dao.TripDao
import com.sujal.expensetracker.data.dao.TripExpenseDao
import com.sujal.expensetracker.data.dao.WishlistDao
import com.sujal.expensetracker.data.entity.BalanceEntity
import com.sujal.expensetracker.data.entity.DenominationEntity
import com.sujal.expensetracker.data.entity.ExpenseEntity
import com.sujal.expensetracker.data.entity.LendingEntity
import com.sujal.expensetracker.data.entity.TripEntity
import com.sujal.expensetracker.data.entity.TripExpenseEntity
import com.sujal.expensetracker.data.entity.WishlistItemEntity

@Database(
    entities = [
        DenominationEntity::class, BalanceEntity::class, LendingEntity::class, ExpenseEntity::class,
        WishlistItemEntity::class, TripEntity::class, TripExpenseEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun denominationDao(): DenominationDao
    abstract fun balanceDao(): BalanceDao
    abstract fun lendingDao(): LendingDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun tripDao(): TripDao
    abstract fun tripExpenseDao(): TripExpenseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
