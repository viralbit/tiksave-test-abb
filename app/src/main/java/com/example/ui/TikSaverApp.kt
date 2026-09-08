package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.PostType
import com.example.ui.components.LinkInputSection
import com.example.ui.components.PhotoSlideshowView
import com.example.ui.components.PhotoViewerDialog
import com.example.ui.components.SaveProgressDialog
import com.example.ui.components.VideoPreviewView
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TikSaverApp(
    viewModel: TikSaverViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Runtime Permission Request Launcher for Storage on older devices if required
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            viewModel.saveSelectedMedia(context)
        }
    }

    val requestSave = {
        val permissionsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (permissionsNeeded.isNotEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            viewModel.saveSelectedMedia(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("app_top_bar")
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            color = HighDensityPrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.tiksaver_icon_1785795053596),
                                    contentDescription = "TikSaver Icon",
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "TikSaver",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "No Watermark Downloader",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = com.example.ui.theme.HighDensitySecondary,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = com.example.ui.theme.HighDensityOnSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "HD 1080p",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.HighDensityOnSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.HighDensityBg
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp)
            ) {
                // Always render Link Input Section at top
                LinkInputSection(
                    urlInput = uiState.urlInput,
                    isLoading = uiState.fetchState is FetchUiState.Loading,
                    onUrlChange = viewModel::onUrlInputChanged,
                    onPasteClicked = viewModel::onPasteClicked,
                    onClearClicked = viewModel::onClearUrlClicked,
                    onFetchClicked = { viewModel.fetchMedia() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message Card if any
                if (uiState.fetchState is FetchUiState.Error) {
                    val errorMsg = (uiState.fetchState as FetchUiState.Error).message
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("error_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Main Content Preview or Success
                Crossfade(
                    targetState = uiState.fetchState,
                    label = "fetchStateTransition"
                ) { state ->
                    when (state) {
                        is FetchUiState.Success -> {
                            val post = state.post
                            if (post.type == PostType.PHOTO_SLIDESHOW) {
                                PhotoSlideshowView(
                                    post = post,
                                    selectedIndices = uiState.selectedIndices,
                                    onToggleSelect = viewModel::toggleImageSelection,
                                    onSelectAll = viewModel::selectAllImages,
                                    onDeselectAll = viewModel::deselectAllImages,
                                    onSaveSelected = requestSave,
                                    onOpenFullScreen = viewModel::openFullScreenImage,
                                    onNewSearchClicked = viewModel::resetPost
                                )
                            } else {
                                VideoPreviewView(
                                    post = post,
                                    onSaveVideo = requestSave,
                                    onNewSearchClicked = viewModel::resetPost
                                )
                            }
                        }

                        else -> {
                            // Blank when idle or error or loading
                        }
                    }
                }
            }

            // Fullscreen Image Dialog
            PhotoViewerDialog(
                imageUrl = uiState.fullScreenImageUrl,
                onDismiss = viewModel::closeFullScreenImage
            )

            // Save Progress Dialog
            SaveProgressDialog(
                status = uiState.saveStatus,
                onDismiss = viewModel::dismissSaveStatus
            )
        }
    }
}
