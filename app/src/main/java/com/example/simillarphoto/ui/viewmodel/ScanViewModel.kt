package com.example.simillarphoto.ui.viewmodel

import android.app.PendingIntent
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simillarphoto.data.repository.PhotoRepository
import com.example.simillarphoto.domain.model.Photo
import com.example.simillarphoto.domain.model.PhotoGroup
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ScanState {
    object Idle : ScanState()
    data class Scanning(val progress: Int, val total: Int) : ScanState()
    data class Success(val groups: List<PhotoGroup>) : ScanState()
    data class Error(val message: String) : ScanState()

    data class CleaningComplete(
        val deletedCount: Int,
        val spaceSaved: Long
    ) : ScanState()
}

sealed class UiEvent {
    data class RequestDeletion(val pendingIntent: PendingIntent) : UiEvent()
    data class DeletionComplete(val photosDeleted: Int, val spaceSaved: Long) : UiEvent()
}

class ScanViewModel(private val repository: PhotoRepository) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _selectedPhotos = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotos: StateFlow<Set<Long>> = _selectedPhotos.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var currentPhotosForDeletion: List<Photo> = emptyList()

    fun startScan() {
        viewModelScope.launch {
            try {
                _scanState.value = ScanState.Scanning(0, 0)
                repository.scanAndProcessPhotos { progress, total ->
                    _scanState.value = ScanState.Scanning(progress, total)
                }
                val groups = repository.getSimilarPhotoGroups()
                _scanState.value = ScanState.Success(groups)
                
                // Smart Suggest: Select all except best in each group + ALL blurry
                val toSelect = mutableSetOf<Long>()
                groups.forEach { group ->
                    if (group.isBlurryGroup) {
                        group.photos.forEach { toSelect.add(it.id) }
                    } else {
                        group.photos.forEach { photo ->
                            if (photo.id != group.bestPhotoId) {
                                toSelect.add(photo.id)
                            }
                        }
                    }
                }
                _selectedPhotos.value = toSelect
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun toggleSelection(photoId: Long) {
        _selectedPhotos.update { current ->
            if (current.contains(photoId)) {
                current - photoId
            } else {
                current + photoId
            }
        }
    }

    fun deleteSelectedPhotos(context: android.content.Context) {
        val currentState = _scanState.value
        if (currentState is ScanState.Success) {
            val selectedIds = _selectedPhotos.value
            val photosToDelete = currentState.groups.flatMap { it.photos }.filter { it.id in selectedIds }
            
            if (photosToDelete.isEmpty()) return
            
            currentPhotosForDeletion = photosToDelete

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pendingIntent = MediaStore.createDeleteRequest(
                    context.contentResolver,
                    photosToDelete.map { it.uri }
                )
                viewModelScope.launch {
                    _uiEvent.emit(UiEvent.RequestDeletion(pendingIntent))
                }
            } else {
                // For older versions, we could do it directly or via some other logic
                // But let's handle the R+ case primarily as requested for scoped storage
                performDeletion(context)
            }
        }
    }

    fun onDeletionConfirmed(context: android.content.Context) {
        performDeletion(context)
    }

    private fun performDeletion(context: android.content.Context) {
        viewModelScope.launch {
            val photosToDelete = currentPhotosForDeletion
            val ids = photosToDelete.map { it.id }
            val spaceSaved = photosToDelete.sumOf { it.size }

            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    photosToDelete.forEach { photo ->
                        context.contentResolver.delete(photo.uri, null, null)
                    }
                }

                repository.deletePhotosFromDb(ids)

                _selectedPhotos.value = emptySet()

                // ✅ SET FINAL STATE
                _scanState.value = ScanState.CleaningComplete(
                    deletedCount = photosToDelete.size,
                    spaceSaved = spaceSaved
                )

                // (Optional toast event)
                _uiEvent.emit(
                    UiEvent.DeletionComplete(
                        photosDeleted = photosToDelete.size,
                        spaceSaved = spaceSaved
                    )
                )

            } catch (e: Exception) {
                _scanState.value = ScanState.Error("Failed to delete photos: ${e.message}")
            }
        }
    }

    fun resetToIdle() {
        _scanState.value = ScanState.Idle
    }

    fun getSelectedSize(): Long {
        val currentState = _scanState.value
        if (currentState is ScanState.Success) {
            val selectedIds = _selectedPhotos.value
            return currentState.groups.flatMap { it.photos }
                .filter { it.id in selectedIds }
                .sumOf { it.size }
        }
        return 0
    }

}
