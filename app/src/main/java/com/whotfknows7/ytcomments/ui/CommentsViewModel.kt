package com.whotfknows7.ytcomments.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whotfknows7.ytcomments.data.CommentItem
import com.whotfknows7.ytcomments.data.CommentSortOrder
import com.whotfknows7.ytcomments.data.MissingApiKeyException
import com.whotfknows7.ytcomments.data.RecentVideoEntry
import com.whotfknows7.ytcomments.data.ReplyItem
import com.whotfknows7.ytcomments.data.VideoInfo
import com.whotfknows7.ytcomments.data.YouTubeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RepliesUiState(
    val isExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val replies: List<ReplyItem> = emptyList(),
    val nextPageToken: String? = null,
    val error: String? = null,
    val hasFetchedFromApi: Boolean = false
)

data class CommentsUiState(
    val urlInput: String = "",
    val apiKey: String = "",
    val showApiKeyDialog: Boolean = false,
    val showHistorySheet: Boolean = false,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: CommentSortOrder = CommentSortOrder.RELEVANCE,
    val currentVideoId: String? = null,
    val videoInfo: VideoInfo? = null,
    val comments: List<CommentItem> = emptyList(),
    val nextPageToken: String? = null,
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val repliesByCommentId: Map<String, RepliesUiState> = emptyMap(),
    val recentVideos: List<RecentVideoEntry> = emptyList()
) {
    val hasApiKey: Boolean get() = apiKey.isNotBlank()

    val filteredComments: List<CommentItem>
        get() {
            val q = searchQuery.trim()
            if (q.isEmpty()) return comments
            return comments.filter { comment ->
                comment.text.contains(q, ignoreCase = true) ||
                    comment.authorName.contains(q, ignoreCase = true)
            }
        }
}

class CommentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = YouTubeRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    private var pendingSharedVideoId: String? = null

    init {
        viewModelScope.launch {
            repository.apiKeyFlow.collect { key ->
                _uiState.update { it.copy(apiKey = key) }
                val pending = pendingSharedVideoId
                if (key.isNotBlank() && pending != null) {
                    pendingSharedVideoId = null
                    loadVideoById(pending)
                }
            }
        }
        viewModelScope.launch {
            repository.recentVideosFlow.collect { recent ->
                _uiState.update { it.copy(recentVideos = recent) }
            }
        }
    }

    fun onUrlInputChange(value: String) {
        _uiState.update { it.copy(urlInput = value) }
    }

    fun onSubmitUrl(rawInput: String = _uiState.value.urlInput) {
        val videoId = YouTubeRepository.extractVideoId(rawInput)
        if (videoId == null) {
            _uiState.update {
                it.copy(errorMessage = "Could not recognize a YouTube video link or ID.")
            }
            return
        }
        _uiState.update { it.copy(urlInput = rawInput.trim(), errorMessage = null) }
        loadVideoById(videoId)
    }

    fun handleIncomingSharedText(sharedText: String?) {
        val videoId = YouTubeRepository.extractVideoId(sharedText) ?: return
        _uiState.update { it.copy(urlInput = sharedText.orEmpty().trim()) }
        loadVideoById(videoId)
    }

    fun loadVideoById(videoId: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            val effectiveKey = repository.getEffectiveApiKey()
            if (effectiveKey.isBlank()) {
                pendingSharedVideoId = videoId
                _uiState.update {
                    it.copy(
                        currentVideoId = videoId,
                        showApiKeyDialog = true,
                        errorMessage = "Enter your official YouTube Data API v3 key to load comments."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    currentVideoId = videoId,
                    isInitialLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null,
                    showHistorySheet = false,
                    comments = if (isRefresh) it.comments else emptyList(),
                    nextPageToken = null,
                    repliesByCommentId = if (isRefresh) it.repliesByCommentId else emptyMap()
                )
            }

            val videoResult = repository.fetchVideoDetails(videoId)
            videoResult.onSuccess { info ->
                _uiState.update { it.copy(videoInfo = info) }
                repository.recordRecentVideo(info)
            }.onFailure { err ->
                if (err is MissingApiKeyException) {
                    pendingSharedVideoId = videoId
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            showApiKeyDialog = true,
                            errorMessage = err.message
                        )
                    }
                    return@launch
                }
            }

            val commentsResult = repository.fetchCommentThreads(
                videoId = videoId,
                order = _uiState.value.sortOrder
            )

            commentsResult.onSuccess { page ->
                _uiState.update {
                    it.copy(
                        comments = page.comments,
                        nextPageToken = page.nextPageToken,
                        isInitialLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                        repliesByCommentId = emptyMap()
                    )
                }
            }.onFailure { err ->
                val isMissingKey = err is MissingApiKeyException
                if (isMissingKey) pendingSharedVideoId = videoId
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        showApiKeyDialog = isMissingKey,
                        errorMessage = err.message ?: "Failed to load comments."
                    )
                }
            }
        }
    }

    fun loadMoreComments() {
        val state = _uiState.value
        val videoId = state.currentVideoId ?: return
        val token = state.nextPageToken ?: return
        if (state.isLoadingMore || state.isInitialLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            repository.fetchCommentThreads(
                videoId = videoId,
                order = state.sortOrder,
                pageToken = token
            ).onSuccess { page ->
                _uiState.update { current ->
                    val existingIds = current.comments.mapTo(HashSet()) { it.id }
                    val uniqueNew = page.comments.filterNot { it.id in existingIds }
                    current.copy(
                        comments = current.comments + uniqueNew,
                        nextPageToken = page.nextPageToken,
                        isLoadingMore = false
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = err.message ?: "Failed to load more comments."
                    )
                }
            }
        }
    }

    fun onSortOrderChange(order: CommentSortOrder) {
        if (_uiState.value.sortOrder == order) return
        _uiState.update { it.copy(sortOrder = order) }
        val videoId = _uiState.value.currentVideoId ?: return
        loadVideoById(videoId)
    }

    fun onRefresh() {
        val videoId = _uiState.value.currentVideoId ?: return
        loadVideoById(videoId, isRefresh = true)
    }

    fun onToggleReplies(comment: CommentItem) {
        if (comment.totalReplyCount <= 0) return
        val currentReplies = _uiState.value.repliesByCommentId[comment.id] ?: RepliesUiState()

        if (currentReplies.isExpanded) {
            updateRepliesState(comment.id) { it.copy(isExpanded = false) }
            return
        }

        if (currentReplies.hasFetchedFromApi) {
            updateRepliesState(comment.id) { it.copy(isExpanded = true) }
            return
        }

        val initialPreview = comment.previewReplies
        val alreadyComplete = initialPreview.size >= comment.totalReplyCount && initialPreview.isNotEmpty()

        if (alreadyComplete) {
            updateRepliesState(comment.id) {
                it.copy(
                    isExpanded = true,
                    replies = initialPreview,
                    hasFetchedFromApi = true,
                    error = null
                )
            }
            return
        }

        updateRepliesState(comment.id) {
            it.copy(
                isExpanded = true,
                isLoading = true,
                replies = initialPreview,
                error = null
            )
        }

        viewModelScope.launch {
            repository.fetchReplies(parentCommentId = comment.id)
                .onSuccess { page ->
                    updateRepliesState(comment.id) {
                        it.copy(
                            isLoading = false,
                            replies = page.replies,
                            nextPageToken = page.nextPageToken,
                            hasFetchedFromApi = true,
                            error = null
                        )
                    }
                }
                .onFailure { err ->
                    updateRepliesState(comment.id) {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "Could not load replies."
                        )
                    }
                }
        }
    }

    fun onLoadMoreReplies(commentId: String) {
        val state = _uiState.value.repliesByCommentId[commentId] ?: return
        val token = state.nextPageToken ?: return
        if (state.isLoadingMore) return

        updateRepliesState(commentId) { it.copy(isLoadingMore = true, error = null) }

        viewModelScope.launch {
            repository.fetchReplies(parentCommentId = commentId, pageToken = token)
                .onSuccess { page ->
                    updateRepliesState(commentId) { current ->
                        val existing = current.replies.mapTo(HashSet()) { it.id }
                        val fresh = page.replies.filterNot { it.id in existing }
                        current.copy(
                            isLoadingMore = false,
                            replies = current.replies + fresh,
                            nextPageToken = page.nextPageToken
                        )
                    }
                }
                .onFailure { err ->
                    updateRepliesState(commentId) {
                        it.copy(
                            isLoadingMore = false,
                            error = err.message ?: "Could not load more replies."
                        )
                    }
                }
        }
    }

    fun setSearchActive(active: Boolean) {
        _uiState.update {
            it.copy(
                isSearchActive = active,
                searchQuery = if (active) it.searchQuery else ""
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show) }
    }

    fun saveApiKey(newKey: String) {
        viewModelScope.launch {
            repository.saveApiKey(newKey)
            _uiState.update { it.copy(showApiKeyDialog = false, errorMessage = null) }
            val currentVideo = _uiState.value.currentVideoId
            if (newKey.isNotBlank() && currentVideo != null && _uiState.value.comments.isEmpty()) {
                loadVideoById(currentVideo)
            }
        }
    }

    fun setShowHistorySheet(show: Boolean) {
        _uiState.update { it.copy(showHistorySheet = show) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun updateRepliesState(
        commentId: String,
        transform: (RepliesUiState) -> RepliesUiState
    ) {
        _uiState.update { state ->
            val current = state.repliesByCommentId[commentId] ?: RepliesUiState()
            state.copy(
                repliesByCommentId = state.repliesByCommentId + (commentId to transform(current))
            )
        }
    }
}
