package com.example.marsphotos.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.marsphotos.R
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.ui.theme.MarsPhotosTheme

@Composable
fun HomeScreen(
    marsUiState: MarsUiState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE) }

    val isFirstLaunch = sharedPreferences.getBoolean("firstLaunch", true)
    val lastLoadedPhotoUrl =
        remember { mutableStateOf(sharedPreferences.getString("lastLoadedPhotoUrl", "")) }

    val expandedImageUrl = sharedPreferences.getString("expandedImageUrl", "")

    var displayUrl by remember { mutableStateOf(!isFirstLaunch) }

    if (isFirstLaunch) {
        // It's the first time ever opening the app
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(5.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    //horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Autorė: Vestina Bertašiūtė", fontWeight = FontWeight.Bold)
                }
            }
        }
        // Update the flag to indicate that the app has been launched at least once
        sharedPreferences.edit().putBoolean("firstLaunch", false).apply()
    } else {
        if (displayUrl) {
            // Display the last loaded picture in a Dialog
            Dialog(
                onDismissRequest = { displayUrl = false }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Paskutinė peržiurėta nuotrauka",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = rememberAsyncImagePainter(model = expandedImageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp) // Adjust the height of the image
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                displayUrl = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Uždaryti")
                        }
                    }
                }
            }
        }


        when (marsUiState) {
            is MarsUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
            is MarsUiState.Success -> {
                // Update the last loaded photo URL
                val newPhotoUrl = marsUiState.photos.firstOrNull()?.imgSrc ?: ""
                lastLoadedPhotoUrl.value = newPhotoUrl
                sharedPreferences.edit().putString("lastLoadedPhotoUrl", newPhotoUrl).apply()
                PhotosGridScreen(marsUiState.photos, modifier = modifier.fillMaxWidth())
            }

            is MarsUiState.Error -> ErrorScreen(retryAction, modifier = modifier.fillMaxSize())
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}

@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
fun PhotosGridScreen(photos: List<MarsPhoto>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier,
        contentPadding = PaddingValues(6.dp)
    ) {
        items(items = photos, key = { photo -> photo.id }) { photo ->
            MarsPhotoCard(
                photo,
                modifier = modifier
                    .padding(6.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
            )
        }
    }
}

@Composable
fun ExpandedImageDialog(
    photo: MarsPhoto,
    dismissAction: () -> Unit
) {
    val context = LocalContext.current
    val expandedImageUrl = photo.imgSrc // URL to save

    Dialog(
        onDismissRequest = dismissAction
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp), // Adjust the height as needed
            shape = RoundedCornerShape(5.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center, // Center the content
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn {
                    item {
                        Image(
                            painter = rememberAsyncImagePainter(model = photo.imgSrc),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                dismissAction()
                                saveExpandedImageUrl(context, expandedImageUrl)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Uždaryti")
                        }
                    }
                }
            }
        }
    }
}

fun saveExpandedImageUrl(context: Context, url: String) {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("expandedImageUrl", url).apply()
}

@Composable
fun MarsPhotoCard(photo: MarsPhoto, modifier: Modifier = Modifier) {
    val dialogState = remember { mutableStateOf(false) }

    Card(
        modifier = modifier.clickable { dialogState.value = true },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current).data(photo.imgSrc)
                .crossfade(true).build(),
            error = painterResource(R.drawable.ic_broken_image),
            placeholder = painterResource(R.drawable.loading_img),
            contentDescription = stringResource(R.string.mars_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (dialogState.value) {
        ExpandedImageDialog(photo) {
            dialogState.value = false
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    MarsPhotosTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    MarsPhotosTheme {
        ErrorScreen({})
    }
}

@Preview(showBackground = true)
@Composable
fun PhotosGridScreenPreview() {
    MarsPhotosTheme {
        val mockData = List(10) { MarsPhoto("$it", "") }
        PhotosGridScreen(mockData)
    }
}
