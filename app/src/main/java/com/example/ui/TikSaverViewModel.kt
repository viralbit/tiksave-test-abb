package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PostType
import com.example.data.model.TikTokPost
import com.example.data.repository.TikTokRepository
import com.example.util.MediaSaver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface FetchUiState {
    object Idle : FetchUiState
    data class Loading(val message: String = "Fetching TikTok post...") : FetchUiState
    data class Success(val post: TikTokPost) : FetchUiState
    data class Error(val message: String) : FetchUiState
}

sealed interface SaveStatus {
    object Idle : SaveStatus
    data class Saving(val current: Int, val total: Int, val progress: Float = 0f) : SaveStatus
    data class Success(val count: Int, val isVideo: Boolean, val folderName: String) : SaveStatus
    data class Error(val message: String) : SaveStatus
}

data class TikSaverUiState(
    val urlInput: String = "",
    val fetchState: FetchUiState = FetchUiState.Idle,
    val selectedIndices: Set<Int> = emptySet(),
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val fullScreenImageUrl: String? = null,
    val isAutoFetchingFromShare: Boolean = false
)

class TikSaverViewModel(
    private val repository: TikTokRepository = TikTokRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TikSaverUiState())
    val uiState: StateFlow<TikSaverUiState> = _uiState.asStateFlow()

    fun onUrlInputChanged(newUrl: String) {
        _uiState.update { it.copy(urlInput = newUrl) }
    }

    fun onClearUrlClicked() {
        _uiState.update { it.copy(urlInput = "", fetchState = FetchUiState.Idle, selectedIndices = emptySet()) }
    }

    fun onPasteClicked(clipboardText: String) {
        val extracted = repository.extractTikTokUrl(clipboardText) ?: clipboardText
        _uiState.update { it.copy(urlInput = extracted) }
        if (extracted.isNotBlank()) {
            fetchMedia(extracted)
        }
    }

    fun handleSharedLink(sharedText: String) {
        val extracted = repository.extractTikTokUrl(sharedText) ?: sharedText
        _uiState.update { it.copy(urlInput = extracted, isAutoFetchingFromShare = true) }
        if (extracted.isNotBlank()) {
            fetchMedia(extracted)
        }
    }

    fun fetchMedia(targetUrl: String? = null) {
        val url = targetUrl ?: _uiState.value.urlInput
        if (url.isBlank()) {
            _uiState.update {
                it.copy(fetchState = FetchUiState.Error("Please enter or paste a TikTok link"))
            }
            return
        }

        val extractedUrl = repository.extractTikTokUrl(url) ?: url

        _uiState.update {
            it.copy(
                urlInput = extractedUrl,
                fetchState = FetchUiState.Loading("Resolving media links without watermark..."),
                selectedIndices = emptySet(),
                saveStatus = SaveStatus.Idle
            )
        }

        viewModelScope.launch {
            val result = repository.resolve(extractedUrl)
            result.fold(
                onSuccess = { post ->
                    val defaultSelection = if (post.type == PostType.PHOTO_SLIDESHOW) {
                        post.imageUrls.indices.toSet()
                    } else {
                        emptySet()
                    }
                    _uiState.update {
                        it.copy(
                            fetchState = FetchUiState.Success(post),
                            selectedIndices = defaultSelection
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            fetchState = FetchUiState.Error(error.message ?: "Unable to fetch post. Check the link and try again.")
                        )
                    }
                }
            )
        }
    }

    fun toggleImageSelection(index: Int) {
        _uiState.update { state ->
            val current = state.selectedIndices.toMutableSet()
            if (current.contains(index)) {
                current.remove(index)
            } else {
                current.add(index)
            }
            state.copy(selectedIndices = current)
        }
    }

    fun selectAllImages() {
        val currentPost = (_uiState.value.fetchState as? FetchUiState.Success)?.post ?: return
        _uiState.update {
            it.copy(selectedIndices = currentPost.imageUrls.indices.toSet())
        }
    }

    fun deselectAllImages() {
        _uiState.update { it.copy(selectedIndices = emptySet()) }
    }

    fun saveSelectedMedia(context: Context) {
        val currentPost = (_uiState.value.fetchState as? FetchUiState.Success)?.post ?: return
        val mediaSaver = MediaSaver(context)

        viewModelScope.launch {
            if (currentPost.type == PostType.VIDEO) {
                val videoUrl = currentPost.videoUrl
                if (videoUrl.isNullOrBlank()) {
                    _uiState.update { it.copy(saveStatus = SaveStatus.Error("Video link is not available")) }
                    return@launch
                }

                _uiState.update { it.copy(saveStatus = SaveStatus.Saving(1, 1, 0f)) }

                val result = mediaSaver.saveVideo(
                    videoUrl = videoUrl,
                    postId = currentPost.id,
                    onProgress = { progress ->
                        _uiState.update { it.copy(saveStatus = SaveStatus.Saving(1, 1, progress)) }
                    }
                )

                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(saveStatus = SaveStatus.Success(count = 1, isVideo = true, folderName = "Movies/TikSaver"))
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(saveStatus = SaveStatus.Error(err.message ?: "Failed to save video"))
                        }
                    }
                )
            } else {
                val selectedIndices = _uiState.value.selectedIndices
                if (selectedIndices.isEmpty()) {
                    _uiState.update { it.copy(saveStatus = SaveStatus.Error("Please select at least 1 photo to save")) }
                    return@launch
                }

                val selectedUrls = selectedIndices.map { index -> index to currentPost.imageUrls[index] }
                var savedCount = 0
                val totalCount = selectedUrls.size

                _uiState.update { it.copy(saveStatus = SaveStatus.Saving(0, totalCount, 0f)) }

                for ((itemIndex, pair) in selectedUrls.withIndex()) {
                    val (originalIndex, url) = pair
                    _uiState.update { it.copy(saveStatus = SaveStatus.Saving(itemIndex + 1, totalCount, 0f)) }

                    val res = mediaSaver.saveImage(
                        imageUrl = url,
                        postId = currentPost.id,
                        index = originalIndex
                    )
                    if (res.isSuccess) {
                        savedCount++
                    }
                }

                if (savedCount > 0) {
                    _uiState.update {
                        it.copy(
                            saveStatus = SaveStatus.Success(
                                count = savedCount,
                                isVideo = false,
                                folderName = "Pictures/TikSaver"
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(saveStatus = SaveStatus.Error("Failed to save photos to phone storage"))
                    }
                }
            }
        }
    }

    fun dismissSaveStatus() {
        _uiState.update { it.copy(saveStatus = SaveStatus.Idle) }
    }

    fun openFullScreenImage(url: String) {
        _uiState.update { it.copy(fullScreenImageUrl = url) }
    }

    fun closeFullScreenImage() {
        _uiState.update { it.copy(fullScreenImageUrl = null) }
    }

    fun resetPost() {
        _uiState.update {
            it.copy(
                fetchState = FetchUiState.Idle,
                selectedIndices = emptySet(),
                saveStatus = SaveStatus.Idle,
                urlInput = ""
            )
        }
    }
}
