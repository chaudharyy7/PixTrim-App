package com.example.simillarphoto.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.simillarphoto.data.local.PhotoDao
import com.example.simillarphoto.data.local.PhotoEntity
import com.example.simillarphoto.domain.model.Photo
import com.example.simillarphoto.domain.model.PhotoGroup
import com.example.simillarphoto.util.HashUtils
import com.example.simillarphoto.util.MediaStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStream

class PhotoRepository(
    private val context: Context,
    private val photoDao: PhotoDao
) {

    fun getAllPhotos(): Flow<List<Photo>> {
        return photoDao.getAllPhotos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun scanAndProcessPhotos(onProgress: (Int, Int) -> Unit) {
        withContext(Dispatchers.IO) {
            val mediaStorePhotos = MediaStoreHelper.fetchPhotos(context)
                .filter { it.width >= 200 && it.height >= 200 } // Ignore very small images
            val total = mediaStorePhotos.size
            
            val mediaStoreIds = mediaStorePhotos.map { it.id }.toSet()
            
            // Cleanup
            val allDbPhotos = photoDao.getAllPhotosSync()
            val idsToDelete = allDbPhotos.filter { it.id !in mediaStoreIds }.map { it.id }
            if (idsToDelete.isNotEmpty()) {
                photoDao.deletePhotos(idsToDelete)
            }

            val photosToInsert = mutableListOf<PhotoEntity>()
            
            mediaStorePhotos.forEachIndexed { index, photo ->
                val cachedPhoto = photoDao.getPhotoById(photo.id)
                if (cachedPhoto == null || cachedPhoto.hash == null) {
                    val result = processPhotoFile(photo.uri)
                    photosToInsert.add(photo.toEntity(result.first, result.second))
                } else {
                    photosToInsert.add(photo.toEntity(cachedPhoto.hash, cachedPhoto.isBlurry))
                }
                onProgress(index + 1, total)
            }
            
            if (photosToInsert.isNotEmpty()) {
                photoDao.insertPhotos(photosToInsert)
            }
        }
    }

    private fun processPhotoFile(uri: Uri): Pair<Long?, Boolean> {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4 
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            bitmap?.let {
                val hash = HashUtils.generateHash(it)
                val isBlurry = HashUtils.isBlurry(it)
                it.recycle()
                Pair(hash, isBlurry)
            } ?: Pair(null, false)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(null, false)
        }
    }

    suspend fun getSimilarPhotoGroups(threshold: Int = 10): List<PhotoGroup> {
        return withContext(Dispatchers.Default) {
            val allEntities = photoDao.getAllPhotosSync()
            
            // 1. Handle Blurry Photos
            val blurryPhotos = allEntities.filter { it.isBlurry }.map { it.toDomain() }
            val blurryGroup = if (blurryPhotos.isNotEmpty()) {
                PhotoGroup(
                    id = "blurry_group",
                    photos = blurryPhotos,
                    bestPhotoId = -1L,
                    isBlurryGroup = true
                )
            } else null

            // 2. Handle Similar Photos
            val entities = allEntities.filter { it.hash != null && !it.isBlurry }
            val processed = mutableSetOf<Long>()
            val similarGroups = mutableListOf<PhotoGroup>()

            for (i in entities.indices) {
                val p1 = entities[i]
                if (p1.id in processed) continue

                val currentGroup = mutableListOf<PhotoEntity>()
                currentGroup.add(p1)

                for (j in i + 1 until entities.size) {
                    val p2 = entities[j]
                    if (p2.id in processed) continue

                    val distance = HashUtils.calculateHammingDistance(p1.hash!!, p2.hash!!)
                    if (distance <= threshold) {
                        currentGroup.add(p2)
                    }
                }

                if (currentGroup.size > 1) {
                    currentGroup.forEach { processed.add(it.id) }
                    val bestPhoto = currentGroup.maxByOrNull { it.width.toLong() * it.height }
                    similarGroups.add(
                        PhotoGroup(
                            photos = currentGroup.map { it.toDomain() },
                            bestPhotoId = bestPhoto?.id ?: p1.id
                        )
                    )
                }
            }
            
            val result = mutableListOf<PhotoGroup>()
            blurryGroup?.let { result.add(it) }
            result.addAll(similarGroups)
            result
        }
    }

    suspend fun deletePhotosFromDb(photoIds: List<Long>) {
        photoDao.deletePhotos(photoIds)
    }

    private fun PhotoEntity.toDomain() = Photo(
        id = id,
        uri = Uri.parse(uri),
        name = name,
        size = size,
        dateModified = dateModified,
        width = width,
        height = height,
        hash = hash,
        isBlurry = isBlurry
    )

    private fun Photo.toEntity(hash: Long?, isBlurry: Boolean) = PhotoEntity(
        id = id,
        uri = uri.toString(),
        name = name,
        size = size,
        dateModified = dateModified,
        width = width,
        height = height,
        hash = hash,
        isBlurry = isBlurry
    )
}
