package com.example.cravory.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cravory.model.CartItem

@Database(entities = [CartItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}