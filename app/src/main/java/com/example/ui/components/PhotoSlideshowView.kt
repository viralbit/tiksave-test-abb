package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.TikTokPost
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityOnSecondary
import com.example.ui.theme.HighDensityOutline
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensitySecondary
import com.example.ui.theme.HighDensitySurfaceCard
import com.example.ui.theme.HighDensityTextSecondary

@Composable
fun PhotoSlideshowView(
    post: TikTokPost,
    selectedIndices: Set<Int>,
    onToggleSelect: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onSaveSelected: () -> Unit,
    onOpenFullScreen: (String) -> Unit,
    onNewSearchClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPhotos = post.imageUrls.size
    val selectedCount = selectedIndices.size
    val allSelected = selectedCount == totalPhotos && totalPhotos > 0

    Column(modifier = modifier.fillMaxWidth()) {
        // Author Card Header
        AuthorHeaderCard(post = post, onNewSearchClicked = onNewSearchClicked)

        Spacer(modifier = Modifier.height(16.dp))

        // High Density Content Grid Card Container
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, HighDensityOutline)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Toolbar Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, HighDensityOutline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = HighDensityPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$totalPhotos Photos found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (allSelected) onDeselectAll() else onSelectAll()
                                }
                                .testTag("select_all_toggle_button"),
                            color = HighDensityPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (allSelected) Icons.Default.CheckCircle else Icons.Default.SelectAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = HighDensityPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (allSelected) "Deselect All" else "Select All",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = HighDensityPrimary
                                )
                            }
                        }
                    }
                }

                // 2-Column Grid of Photos inside card
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(380.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    itemsIndexed(
                        items = post.imageUrls,
                        key = { index, url -> "$index-$url" }
                    ) { index, imageUrl ->
                        val isSelected = selectedIndices.contains(index)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) HighDensityPrimary else HighDensityOutline,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onToggleSelect(index) }
                                .testTag("photo_item_$index"),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Image
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Slideshow photo ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Gradient Overlay at Bottom
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.5f)
                                                )
                                            )
                                        )
                                )

                                // Photo Filename / Label Bottom Left
                                Text(
                                    text = "IMG_${index + 1}.jpg",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.BottomStart)
                                )

                                // Checkbox Circle Badge (Top Right)
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) HighDensityPrimary else Color.Black.copy(alpha = 0.4f))
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                // Full Screen Preview Button Bottom Right
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .clickable { onOpenFullScreen(imageUrl) }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "View full image",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High Density Action Button Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onSaveSelected,
                enabled = selectedCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_selected_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HighDensityPrimary,
                    contentColor = com.example.ui.theme.MintGlowOnPrimary,
                    disabledContainerColor = HighDensitySurfaceCard,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SaveAlt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (selectedCount > 0) "SAVE $selectedCount PHOTOS TO GALLERY" else "SELECT PHOTOS TO SAVE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SAVED TO PICTURES/TIKSAVER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = HighDensityTextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AuthorHeaderCard(
    post: TikTokPost,
    onNewSearchClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, HighDensityOutline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = "Author avatar",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (post.authorHandle.isNotBlank()) {
                            Text(
                                text = post.authorHandle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onNewSearchClicked,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("new_link_button"),
                    border = BorderStroke(1.dp, HighDensityPrimary)
                ) {
                    Text(
                        "New Link",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityPrimary
                    )
                }
            }

            if (post.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                    maxLines = 3
                )
            }
        }
    }
}

