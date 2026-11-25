package com.whotfknows7.ytcomments.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ThumbUpOffAlt
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.whotfknows7.ytcomments.data.CommentItem
import com.whotfknows7.ytcomments.data.CommentSortOrder
import com.whotfknows7.ytcomments.data.RecentVideoEntry
import com.whotfknows7.ytcomments.data.ReplyItem
import com.whotfknows7.ytcomments.data.VideoInfo
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

private val DarkPalette = darkColorScheme(
    primary = Color(0xFFFF4E5B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF4B1218),
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = Color(0xFF7CB4FF),
    onSecondary = Color(0xFF002D5A),
    background = Color(0xFF0F0F12),
    surface = Color(0xFF0F0F12),
    surfaceVariant = Color(0xFF1D1E24),
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFFA7ACB8)
)

private val LightPalette = lightColorScheme(
    primary = Color(0xFFD91E2A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE5E7),
    onPrimaryContainer = Color(0xFF5A060B),
    secondary = Color(0xFF065FD4),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF9FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEEFF3),
    onBackground = Color(0xFF111318),
    onSurface = Color(0xFF111318),
    onSurfaceVariant = Color(0xFF5B616E)
)

@Composable
fun YTCommentsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkPalette
        else -> LightPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsApp(viewModel: CommentsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    val shouldLoadMore by remember(listState, state.comments.size, state.nextPageToken) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            state.nextPageToken != null &&
                !state.isLoadingMore &&
                !state.isInitialLoading &&
                total > 0 &&
                lastVisible >= total - 5
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMoreComments()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    if (state.isSearchActive) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("Search loaded comments...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.setSearchActive(false) }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close search")
                                }
                            }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "YT Comments",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    if (!state.isSearchActive && state.comments.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchActive(true) }) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search comments")
                        }
                    }
                    if (!state.isSearchActive) {
                        IconButton(onClick = { viewModel.setShowHistorySheet(true) }) {
                            Icon(Icons.Rounded.History, contentDescription = "Recent videos")
                        }
                        IconButton(onClick = { viewModel.setShowApiKeyDialog(true) }) {
                            Icon(
                                imageVector = Icons.Rounded.Key,
                                contentDescription = "YouTube API Key",
                                tint = if (state.hasApiKey) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item(key = "url_bar") {
                    UrlInputBar(
                        value = state.urlInput,
                        onValueChange = viewModel::onUrlInputChange,
                        onPasteClick = {
                            val clipText = clipboard.getText()?.text.orEmpty()
                            if (clipText.isNotBlank()) {
                                viewModel.onUrlInputChange(clipText)
                                viewModel.onSubmitUrl(clipText)
                                focusManager.clearFocus()
                            }
                        },
                        onSubmit = {
                            viewModel.onSubmitUrl()
                            focusManager.clearFocus()
                        }
                    )
                }

                if (!state.hasApiKey) {
                    item(key = "api_key_banner") {
                        ApiKeySetupBanner(
                            onConfigureClick = { viewModel.setShowApiKeyDialog(true) }
                        )
                    }
                }

                state.errorMessage?.let { error ->
                    item(key = "error_banner") {
                        ErrorBanner(
                            message = error,
                            onDismiss = viewModel::dismissError
                        )
                    }
                }

                state.videoInfo?.let { video ->
                    item(key = "video_header_${video.id}") {
                        VideoHeaderCard(
                            video = video,
                            onOpenInYouTube = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=${video.id}")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                if (state.currentVideoId != null) {
                    item(key = "sort_bar") {
                        SortAndCountBar(
                            selectedOrder = state.sortOrder,
                            totalCount = state.videoInfo?.commentCount,
                            loadedCount = state.filteredComments.size,
                            onSelectOrder = viewModel::onSortOrderChange
                        )
                    }
                }

                if (state.isInitialLoading) {
                    items(6, key = { "shimmer_$it" }) {
                        CommentSkeletonRow()
                    }
                } else if (state.currentVideoId == null) {
                    item(key = "empty_home") {
                        WelcomeEmptyState(
                            hasApiKey = state.hasApiKey,
                            onConfigureApiKey = { viewModel.setShowApiKeyDialog(true) }
                        )
                    }
                } else if (state.filteredComments.isEmpty() && state.errorMessage == null) {
                    item(key = "empty_comments") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.searchQuery.isNotBlank()) {
                                    "No loaded comments match \"${state.searchQuery}\"."
                                } else {
                                    "No comments found on this video."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = state.filteredComments,
                        key = { it.id }
                    ) { comment ->
                        val repliesState = state.repliesByCommentId[comment.id] ?: RepliesUiState()
                        CommentThreadRow(
                            comment = comment,
                            videoChannelId = state.videoInfo?.channelId,
                            repliesState = repliesState,
                            onToggleReplies = { viewModel.onToggleReplies(comment) },
                            onLoadMoreReplies = { viewModel.onLoadMoreReplies(comment.id) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            thickness = 0.75.dp
                        )
                    }

                    if (state.isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = state.apiKey,
            onDismiss = { viewModel.setShowApiKeyDialog(false) },
            onSave = viewModel::saveApiKey,
            onOpenConsole = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://console.cloud.google.com/apis/credentials")
                )
                context.startActivity(intent)
            }
        )
    }

    if (state.showHistorySheet) {
        RecentVideosBottomSheet(
            videos = state.recentVideos,
            onDismiss = { viewModel.setShowHistorySheet(false) },
            onSelectVideo = { viewModel.loadVideoById(it.videoId) },
            onClearHistory = viewModel::clearHistory
        )
    }
}

@Composable
private fun UrlInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Paste YouTube link or video ID") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onSubmit() }),
            trailingIcon = {
                Row {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    } else {
                        IconButton(onClick = onPasteClick) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste link")
                        }
                    }
                    IconButton(onClick = onSubmit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Load comments",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun ApiKeySetupBanner(onConfigureClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "YouTube API Key Needed",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Add your official YouTube Data API v3 key to start loading comments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
            FilledTonalButton(onClick = onConfigureClick) {
                Text("Set Key")
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun VideoHeaderCard(
    video: VideoInfo,
    onOpenInYouTube: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(116.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = video.channelTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val statsLine = buildList {
                    video.viewCount?.let { add("${formatCompactNumber(it)} views") }
                    video.likeCount?.let { add("${formatCompactNumber(it)} likes") }
                }.joinToString(" • ")

                if (statsLine.isNotEmpty()) {
                    Text(
                        text = statsLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onOpenInYouTube) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = "Open in YouTube",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SortAndCountBar(
    selectedOrder: CommentSortOrder,
    totalCount: Long?,
    loadedCount: Int,
    onSelectOrder: (CommentSortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CommentSortOrder.entries.forEach { order ->
                FilterChip(
                    selected = selectedOrder == order,
                    onClick = { onSelectOrder(order) },
                    label = { Text(order.label) }
                )
            }
        }

        val countLabel = totalCount?.let { "${formatCompactNumber(it)} total" }
            ?: if (loadedCount > 0) "$loadedCount loaded" else ""
        if (countLabel.isNotEmpty()) {
            Text(
                text = countLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommentThreadRow(
    comment: CommentItem,
    videoChannelId: String?,
    repliesState: RepliesUiState,
    onToggleReplies: () -> Unit,
    onLoadMoreReplies: () -> Unit
) {
    val isCreator = !videoChannelId.isNullOrBlank() && comment.authorChannelId == videoChannelId
    var isTextExpanded by remember(comment.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = comment.totalReplyCount > 0, onClick = onToggleReplies)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = comment.authorAvatarUrl,
                contentDescription = comment.authorName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isCreator) {
                        Surface(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = comment.authorName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.surface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = comment.authorName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = buildString {
                            append(formatRelativeTime(comment.publishedAt))
                            if (comment.isEdited) append(" (edited)")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isTextExpanded) Int.MAX_VALUE else 6,
                    overflow = TextOverflow.Ellipsis
                )

                if (comment.text.length > 260) {
                    Text(
                        text = if (isTextExpanded) "Show less" else "Read more",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { isTextExpanded = !isTextExpanded }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ThumbUpOffAlt,
                            contentDescription = "Likes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        if (comment.likeCount > 0) {
                            Text(
                                text = formatCompactNumber(comment.likeCount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (comment.totalReplyCount > 0) {
                        Surface(
                            onClick = onToggleReplies,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (repliesState.isExpanded) {
                                        Icons.Rounded.KeyboardArrowUp
                                    } else {
                                        Icons.Rounded.KeyboardArrowDown
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (comment.totalReplyCount == 1) {
                                        "1 reply"
                                    } else {
                                        "${formatCompactNumber(comment.totalReplyCount.toLong())} replies"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = repliesState.isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, top = 10.dp)
            ) {
                repliesState.replies.forEach { reply ->
                    ReplyRow(
                        reply = reply,
                        isCreator = !videoChannelId.isNullOrBlank() &&
                            reply.authorChannelId == videoChannelId
                    )
                }

                if (repliesState.isLoading) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Loading replies...",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                repliesState.error?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                if (repliesState.nextPageToken != null) {
                    TextButton(
                        onClick = onLoadMoreReplies,
                        enabled = !repliesState.isLoadingMore,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        if (repliesState.isLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Load more replies",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyRow(
    reply: ReplyItem,
    isCreator: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(34.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        AsyncImage(
            model = reply.authorAvatarUrl,
            contentDescription = reply.authorName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isCreator) {
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = reply.authorName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    Text(
                        text = reply.authorName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = buildString {
                        append(formatRelativeTime(reply.publishedAt))
                        if (reply.isEdited) append(" (edited)")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = reply.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (reply.likeCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbUpOffAlt,
                        contentDescription = "Likes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatCompactNumber(reply.likeCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeEmptyState(
    hasApiKey: Boolean,
    onConfigureApiKey: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Forum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Text(
            text = "Send Any YouTube Video Here",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tap Share in the YouTube app and choose YT Comments, or paste any YouTube video, Short, or Live link above to browse comments and tap any thread to load replies.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!hasApiKey) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onConfigureApiKey) {
                Text("Add Official YouTube API Key")
            }
        }
    }
}

@Composable
private fun CommentSkeletonRow() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(shimmerColor)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerColor)
            )
        }
    }
}

@Composable
private fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onOpenConsole: () -> Unit
) {
    var draftKey by remember(currentKey) { mutableStateOf(currentKey) }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("YouTube Data API v3 Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter your official Google Cloud YouTube Data API v3 key. It is stored locally on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = draftKey,
                    onValueChange = { draftKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    visualTransformation = if (visible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector = if (visible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = "Toggle key visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = onOpenConsole,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Get a free API key in Google Cloud Console")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(draftKey) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentVideosBottomSheet(
    videos: List<RecentVideoEntry>,
    onDismiss: () -> Unit,
    onSelectVideo: (RecentVideoEntry) -> Unit,
    onClearHistory: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recent Videos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (videos.isNotEmpty()) {
                    TextButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }

            if (videos.isEmpty()) {
                Text(
                    text = "Videos you open or share here will appear in your history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(videos, key = { it.videoId }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectVideo(entry) }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = entry.thumbnailUrl,
                                contentDescription = entry.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(92.dp)
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = entry.channelTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCompactNumber(value: Long): String {
    val absVal = abs(value)
    return when {
        absVal >= 1_000_000_000L -> String.format(Locale.US, "%.1fB", value / 1_000_000_000.0)
        absVal >= 1_000_000L -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
        absVal >= 1_000L -> String.format(Locale.US, "%.1fK", value / 1_000.0)
        else -> NumberFormat.getIntegerInstance(Locale.US).format(value)
    }.replace(".0", "")
}

private fun formatRelativeTime(isoTimestamp: String): String {
    if (isoTimestamp.isBlank()) return ""
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = runCatching { parser.parse(isoTimestamp) }.getOrNull() ?: return ""
    val diffSeconds = ((System.currentTimeMillis() - date.time) / 1000L).coerceAtLeast(0L)

    return when {
        diffSeconds < 60L -> "just now"
        diffSeconds < 3600L -> "${diffSeconds / 60L}m ago"
        diffSeconds < 86400L -> "${diffSeconds / 3600L}h ago"
        diffSeconds < 2592000L -> "${diffSeconds / 86400L}d ago"
        diffSeconds < 31536000L -> "${diffSeconds / 2592000L}mo ago"
        else -> "${diffSeconds / 31536000L}y ago"
    }
}
