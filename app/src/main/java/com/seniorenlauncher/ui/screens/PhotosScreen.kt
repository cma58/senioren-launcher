package com.seniorenlauncher.ui.screens

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.launch
import java.io.File

data class PhotoItem(val uri: Uri, val id: Long, val folderName: String)

data class FolderItem(val name: String, val firstPhotoUri: Uri, val photoCount: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotosScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var allPhotos by remember { mutableStateOf<List<PhotoItem>>(emptyList()) }
    var folders by remember { mutableStateOf<List<FolderItem>>(emptyList()) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var initialPageIndex by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val displayedPhotos = remember(allPhotos, selectedFolder) {
        if (selectedFolder == null) emptyList()
        else allPhotos.filter { it.folderName == selectedFolder }
    }

    LaunchedEffect(Unit) {
        val fetched = fetchPhotos(context)
        allPhotos = fetched
        folders = fetched.groupBy { it.folderName }.map { (name, photos) ->
            FolderItem(name, photos.first().uri, photos.size)
        }.sortedBy { it.name }
        isLoading = false
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        val title = when {
            initialPageIndex != null -> "Foto bekijken"
            selectedFolder != null -> selectedFolder!!
            else -> "Foto Albums"
        }
        
        ScreenHeader(title = title, onBack = {
            when {
                initialPageIndex != null -> initialPageIndex = null
                selectedFolder != null -> selectedFolder = null
                else -> onBack()
            }
        })

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (allPhotos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Geen foto's gevonden", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        } else if (initialPageIndex != null) {
            PhotoPagerView(
                photos = displayedPhotos,
                initialIndex = initialPageIndex!!,
                onClose = { initialPageIndex = null },
                onDelete = { photoToDelete ->
                    try {
                        context.contentResolver.delete(photoToDelete.uri, null, null)
                        allPhotos = allPhotos.filter { it.id != photoToDelete.id }
                        Toast.makeText(context, "Foto verwijderd", Toast.LENGTH_SHORT).show()
                        if (displayedPhotos.isEmpty()) initialPageIndex = null
                    } catch (e: Exception) {
                        Toast.makeText(context, "Kan foto niet verwijderen", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else if (selectedFolder == null) {
            // Folder Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(folders) { folder ->
                    FolderCard(folder) { selectedFolder = folder.name }
                }
            }
        } else {
            // Photos Grid in Folder
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(displayedPhotos) { index, photo ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { initialPageIndex = index },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FolderCard(folder: FolderItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Box(Modifier.weight(1f)) {
                AsyncImage(
                    model = folder.firstPhotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${folder.photoCount}",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = folder.name,
                modifier = Modifier.padding(12.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoPagerView(
    photos: List<PhotoItem>,
    initialIndex: Int,
    onClose: () -> Unit,
    onDelete: (PhotoItem) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { photos.size })
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = photos[page].uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Top toolbar
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)))
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CircleIconButton(Icons.Default.ArrowBack, "Terug") { onClose() }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircleIconButton(Icons.Default.Share, "Delen") {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, photos[pagerState.currentPage].uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Foto delen"))
                    }
                    CircleIconButton(Icons.Default.Delete, "Verwijderen", Color.Red) {
                        showDeleteConfirm = true
                    }
                }
            }
        }

        // Navigation helpers
        if (pagerState.currentPage > 0) {
            Box(Modifier.align(Alignment.CenterStart).padding(8.dp)) {
                CircleIconButton(Icons.Default.ChevronLeft, "Vorige", size = 64.dp) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }
            }
        }

        if (pagerState.currentPage < photos.size - 1) {
            Box(Modifier.align(Alignment.CenterEnd).padding(8.dp)) {
                CircleIconButton(Icons.Default.ChevronRight, "Volgende", size = 64.dp) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            }
        }

        // Page indicator
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Text(
                "${pagerState.currentPage + 1} / ${photos.size}",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Verwijderen?", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Wilt u deze foto uit uw album verwijderen?", fontSize = 18.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(photos[pagerState.currentPage])
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(60.dp)
                ) {
                    Text("JA, VERWIJDER", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("ANNULEREN", fontSize = 18.sp)
                }
            }
        )
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color = Color.Black,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onClick() }
            .shadow(2.dp, CircleShape),
        color = Color.White.copy(alpha = 0.9f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, modifier = Modifier.size(size * 0.6f), tint = tint)
        }
    }
}

fun fetchPhotos(context: Context): List<PhotoItem> {
    val photoList = mutableListOf<PhotoItem>()
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    try {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            
            while (cursor.moveToNext() && photoList.size < 1000) {
                val id = cursor.getLong(idColumn)
                val folderName = cursor.getString(bucketColumn) ?: "Onbekend"
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                photoList.add(PhotoItem(contentUri, id, folderName))
            }
        }
    } catch (e: Exception) {}
    return photoList
}
