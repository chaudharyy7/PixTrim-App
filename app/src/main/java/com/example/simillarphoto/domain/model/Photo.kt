package com.example.simillarphoto.domain.model

import android.net.Uri

data class Photo(
    val id: Long,
    val uri: Uri,
    val name: String,
    val size: Long,
    val dateModified: Long,
    val width: Int,
    val height: Int,
    val hash: Long? = null,
    val isBlurry: Boolean = false
)

data class PhotoGroup(
    val id: String = java.util.UUID.randomUUID().toString(),
    val photos: List<Photo>,
    val bestPhotoId: Long,
    val totalSize: Long = photos.sumOf { it.size },
    val isBlurryGroup: Boolean = false
)
