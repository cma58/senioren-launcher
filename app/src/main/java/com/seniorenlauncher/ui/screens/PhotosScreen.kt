package com.seniorenlauncher.ui.screens

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seniorenlauncher.ui.components.ScreenHeader

data class PhotoItem(val uri: Uri, val id: Long)

@Composable
fun PhotosScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var photos by remember { mutableStateOf<List<PhotoItem>>(emptyList()) }
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        photos = fetchPhotos(context)
        isLoading = false
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = if (selectedPhoto != null) "Foto bekijken" else "Foto's", onBack = {
            if (selectedPhoto != null) selectedPhoto = null else onBack()
        })

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (selectedPhoto != null) {
            FullScreenPhoto(selectedPhoto!!) { selectedPhoto = null }
        } else if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Geen foto's gevonden", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(photos) { photo ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedPhoto = photo.uri },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
fun FullScreenPhoto(uri: Uri, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .height(80.dp)
                .fillMaxWidth(0.7f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f), contentColor = Color.Black),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("GA TERUG", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

fun fetchPhotos(context: Context): List<PhotoItem> {
    val photoList = mutableListOf<PhotoItem>()
    val projection = arrayOf(MediaStore.Images.Media._ID)
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
            while (cursor.moveToNext() && photoList.size < 100) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                photoList.add(PhotoItem(contentUri, id))
            }
        }
    } catch (e: Exception) {}
    return photoList
}
