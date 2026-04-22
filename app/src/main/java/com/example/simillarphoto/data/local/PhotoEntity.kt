package com.example.simillarphoto.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: Long,
    val uri: String,
    val name: String,
    val size: Long,
    val dateModified: Long,
    val width: Int,
    val height: Int,
    val hash: Long? = null,
    val isBlurry: Boolean = false
)
