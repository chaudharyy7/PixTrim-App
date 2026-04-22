package com.example.simillarphoto

import android.app.Application
import androidx.room.Room
import com.example.simillarphoto.data.local.AppDatabase
import com.example.simillarphoto.data.repository.PhotoRepository

class PhotoApplication : Application() {
    
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "photo_database"
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val repository by lazy {
        PhotoRepository(this, database.photoDao())
    }
}
