package com.example.cravory

import android.app.Application
import androidx.room.Room
import com.example.cravory.database.AppDatabase

class CravoryApp : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "cravory-cart-database"
        ).build()
    }
}