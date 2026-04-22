package com.example.simillarphoto.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.simillarphoto.domain.model.Photo
import com.example.simillarphoto.domain.model.PhotoGroup
import com.example.simillarphoto.ui.components.PermissionHandler
import com.example.simillarphoto.ui.viewmodel.ScanState
import com.example.simillarphoto.ui.viewmodel.ScanViewModel
import com.example.simillarphoto.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ScanViewModel) {

    val state by viewModel.scanState.collectAsStateWithLifecycle()
    val selectedPhotos by viewModel.selectedPhotos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var previewPhoto by remember { mutableStateOf<Photo?>(null) }

    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onDeletionConfirmed(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.RequestDeletion -> {
                    val request = androidx.activity.result.IntentSenderRequest.Builder(event.pendingIntent.intentSender).build()
                    intentSenderLauncher.launch(request)
                }
                is UiEvent.DeletionComplete -> {
                    // Handled via state update to CleaningComplete
                }
            }
        }
    }

    PermissionHandler(
        onPermissionGranted = { /* Optional: auto start scan if desired */ }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    showRescan = state is ScanState.Success,
                    onRescan = { viewModel.startScan() }
                )
            },
            bottomBar = {
                if (state is ScanState.Success && selectedPhotos.isNotEmpty()) {
                    val sizeMb = viewModel.getSelectedSize() / (1024 * 1024)
                    Button(
                        onClick = { viewModel.deleteSelectedPhotos(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5A7A)
                        )
                    ) {
                        Text(
                            "DELETE (${selectedPhotos.size} • ${sizeMb}MB)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF020617),
                                Color(0xFF020B1F),
                                Color(0xFF000000)
                            )
                        )
                    )
                    .padding(padding)
            ) {
                when (val currentState = state) {
                    is ScanState.Idle -> {
                        IdleScreen(onStartScan = { viewModel.startScan() })
                    }
                    is ScanState.Scanning -> {
                        ScanningScreen(
                            progress = currentState.progress,
                            total = currentState.total
                        )
                    }
                    is ScanState.Success -> {
                        SuccessScreen(
                            groups = currentState.groups,
                            selectedPhotos = selectedPhotos,
                            onToggleSelection = { viewModel.toggleSelection(it) },
                            onPhotoClick = { previewPhoto = it }
                        )
                    }
                    is ScanState.Error -> {
                        ErrorScreen(
                            message = currentState.message,
                            onRetry = { viewModel.startScan() }
                        )
                    }
                    is ScanState.CleaningComplete -> {
                        CleaningCompleteScreen(
                            deletedCount = currentState.deletedCount,
                            spaceSaved = currentState.spaceSaved,
                            onScanAgain = { viewModel.startScan() },
                            onDone = { viewModel.resetToIdle() }
                        )
                    }
                }

                // 🔥 Full Screen Preview Dialog
                if (previewPhoto != null) {
                    FullScreenPreview(
                        photo = previewPhoto!!,
                        onClose = { previewPhoto = null }
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenPreview(photo: Photo, onClose: () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = photo.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Rounded.Close, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ModernTopBar(showRescan: Boolean, onRescan: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFF8EA2FF)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            "PixTrim",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        if (showRescan) {
            IconButton(onClick = onRescan) {
                Icon(Icons.Rounded.Refresh, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun IdleScreen(onStartScan: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(120.dp))

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF1A233A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                null,
                tint = Color(0xFF8EA2FF),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Ready to Clean?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Find and remove similar photos to free up space",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8EA2FF)
            )
        ) {
            Text("Start Scan →", color = Color.Black)
        }
    }
}

@Composable
fun ScanningScreen(progress: Int, total: Int) {

    val percent = if (total > 0) (progress * 100 / total) else 0

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.size(240.dp),
                strokeWidth = 12.dp,
                color = Color(0xFF8EA2FF),
                trackColor = Color(0xFF1A233A)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$percent%",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$progress / $total",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            "Scanning photos...",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Analyzing visual similarities",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SuccessScreen(
    groups: List<PhotoGroup>,
    selectedPhotos: Set<Long>,
    onToggleSelection: (Long) -> Unit,
    onPhotoClick: (Photo) -> Unit
) {

    if (groups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF020617), Color.Black)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF8EA2FF),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Your gallery is clean!",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    "No similar photos found",
                    color = Color.Gray
                )
            }
        }

    } else {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF020617), Color.Black)
                    )
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            items(groups, key = { it.id }) { group ->
                PhotoGroupItem(group, selectedPhotos, onToggleSelection, onPhotoClick)
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun PhotoGroupItem(
    group: PhotoGroup,
    selectedPhotos: Set<Long>,
    onToggleSelection: (Long) -> Unit,
    onPhotoClick: (Photo) -> Unit
) {

    Column {

        val headerText = if (group.isBlurryGroup) "BLURRY PHOTOS" else "${group.photos.size} SIMILAR PHOTOS"
        val headerColor = if (group.isBlurryGroup) Color(0xFFFF5A7A) else Color.Gray

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = headerText,
                color = headerColor,
                fontWeight = FontWeight.Bold
            )

            if (group.isBlurryGroup) {
                val sizeMb = group.totalSize / (1024 * 1024)
                Text(
                    text = "Free up ${sizeMb}MB",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            items(group.photos, key = { it.id }) { photo ->

                val isSelected = selectedPhotos.contains(photo.id)

                PhotoThumbnail(
                    photo = photo,
                    isBest = photo.id == group.bestPhotoId,
                    isSelected = isSelected,
                    onToggle = { onToggleSelection(photo.id) },
                    onClick = { onPhotoClick(photo) }
                )
            }
        }
    }
}

@Composable
fun PhotoThumbnail(
    photo: Photo,
    isBest: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {

        AsyncImage(
            model = photo.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔥 Selected Overlay
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        // 🔥 Check circle
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color(0xFFFF5A7A)
                    else Color.Black.copy(alpha = 0.5f)
                )
                .clickable { onToggle() }
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        // 🔥 BEST PHOTO badge
        if (isBest) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .size(width = 100.dp, height = 30.dp) // 🔥 fixed size
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF8EA2FF), Color(0xFF6C63FF))
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BEST PHOTO",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // 🔥 BLUR badge
        if (photo.isBlurry) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFFFF5A7A), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "BLUR",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFFF5A7A),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Something went wrong",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            message,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8EA2FF))
        ) {
            Text("Retry", color = Color.Black)
        }
    }
}

@Composable
fun CleaningCompleteScreen(
    deletedCount: Int,
    spaceSaved: Long,
    onScanAgain: () -> Unit,
    onDone: () -> Unit
) {

    val mb = spaceSaved / (1024 * 1024)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020617), Color.Black)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF8EA2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(50.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Cleaning Complete! 🎉",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Your library is optimized",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 🔥 Stats Cards
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            StatCard("PHOTOS", "$deletedCount Deleted")
            StatCard("SPACE", "$mb MB Saved")
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onScanAgain,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8EA2FF))
        ) {
            Text("Scan Again", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onDone) {
            Text("Done", color = Color.Gray)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A))
            .padding(20.dp)
    ) {

        Text(
            title,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

