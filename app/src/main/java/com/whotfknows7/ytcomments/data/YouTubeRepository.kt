package com.whotfknows7.ytcomments.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.whotfknows7.ytcomments.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yt_comments_prefs")

class YouTubeRepository(
    private val context: Context,
    private val api: YouTubeApiService = YouTubeApiService.create(),
    private val gson: Gson = Gson()
) {
    private val apiKeyPref = stringPreferencesKey("youtube_api_key")
    private val recentVideosPref = stringPreferencesKey("recent_videos_json")

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val saved = prefs[apiKeyPref]?.trim().orEmpty()
        if (saved.isNotEmpty()) saved else BuildConfig.YOUTUBE_API_KEY.trim()
    }

    val recentVideosFlow: Flow<List<RecentVideoEntry>> = context.dataStore.data.map { prefs ->
        val raw = prefs[recentVideosPref].orEmpty()
        if (raw.isBlank()) {
            emptyList()
        } else {
            runCatching {
                val type = object : TypeToken<List<RecentVideoEntry>>() {}.type
                gson.fromJson<List<RecentVideoEntry>>(raw, type) ?: emptyList()
            }.getOrDefault(emptyList())
        }
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { prefs ->
            val trimmed = key.trim()
            if (trimmed.isEmpty()) {
                prefs.remove(apiKeyPref)
            } else {
                prefs[apiKeyPref] = trimmed
            }
        }
    }

    suspend fun getEffectiveApiKey(): String = apiKeyFlow.first()

    suspend fun recordRecentVideo(video: VideoInfo) {
        val current = recentVideosFlow.first().toMutableList()
        current.removeAll { it.videoId == video.id }
        current.add(
            0,
            RecentVideoEntry(
                videoId = video.id,
                title = video.title,
                channelTitle = video.channelTitle,
                thumbnailUrl = video.thumbnailUrl
            )
        )
        val trimmed = current.take(15)
        context.dataStore.edit { prefs ->
            prefs[recentVideosPref] = gson.toJson(trimmed)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(recentVideosPref)
        }
    }

    suspend fun fetchVideoDetails(videoId: String): Result<VideoInfo> = runApiCall { apiKey ->
        val response = api.getVideoDetails(videoId = videoId, apiKey = apiKey)
        val item = response.items.firstOrNull()
            ?: throw IllegalStateException("Video not found or unavailable.")
        val snippet = item.snippet
        val stats = item.statistics
        VideoInfo(
            id = item.id,
            title = snippet?.title?.ifBlank { "Untitled Video" } ?: "Untitled Video",
            channelId = snippet?.channelId.orEmpty(),
            channelTitle = snippet?.channelTitle.orEmpty(),
            thumbnailUrl = snippet?.thumbnails?.bestUrl().orEmpty(),
            viewCount = stats?.viewCount?.toLongOrNull(),
            likeCount = stats?.likeCount?.toLongOrNull(),
            commentCount = stats?.commentCount?.toLongOrNull(),
            publishedAt = snippet?.publishedAt.orEmpty()
        )
    }

    suspend fun fetchCommentThreads(
        videoId: String,
        order: CommentSortOrder,
        pageToken: String? = null
    ): Result<CommentPage> = runApiCall { apiKey ->
        val response = api.getCommentThreads(
            videoId = videoId,
            order = order.apiValue,
            pageToken = pageToken,
            apiKey = apiKey
        )
        val comments = response.items.map { thread ->
            val top = thread.snippet.topLevelComment
            val s = top.snippet
            val previews = thread.replies?.comments.orEmpty().map { replyDto ->
                replyDto.toReplyItem()
            }.reversed()

            CommentItem(
                id = top.id,
                authorName = s.authorDisplayName.ifBlank { "Anonymous" },
                authorAvatarUrl = s.authorProfileImageUrl,
                authorChannelId = s.authorChannelId?.value,
                text = (s.textOriginal ?: s.textDisplay).trim(),
                likeCount = s.likeCount,
                publishedAt = s.publishedAt,
                isEdited = s.updatedAt.isNotBlank() && s.updatedAt != s.publishedAt,
                totalReplyCount = thread.snippet.totalReplyCount,
                previewReplies = previews
            )
        }
        CommentPage(
            comments = comments,
            nextPageToken = response.nextPageToken
        )
    }

    suspend fun fetchReplies(
        parentCommentId: String,
        pageToken: String? = null
    ): Result<ReplyPage> = runApiCall { apiKey ->
        val response = api.getCommentReplies(
            parentId = parentCommentId,
            pageToken = pageToken,
            apiKey = apiKey
        )
        ReplyPage(
            replies = response.items.map { it.toReplyItem() },
            nextPageToken = response.nextPageToken
        )
    }

    private fun CommentDto.toReplyItem(): ReplyItem {
        val s = snippet
        return ReplyItem(
            id = id,
            authorName = s.authorDisplayName.ifBlank { "Anonymous" },
            authorAvatarUrl = s.authorProfileImageUrl,
            authorChannelId = s.authorChannelId?.value,
            text = (s.textOriginal ?: s.textDisplay).trim(),
            likeCount = s.likeCount,
            publishedAt = s.publishedAt,
            isEdited = s.updatedAt.isNotBlank() && s.updatedAt != s.publishedAt
        )
    }

    private suspend fun <T> runApiCall(block: suspend (apiKey: String) -> T): Result<T> {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(MissingApiKeyException())
        }
        return try {
            Result.success(block(apiKey))
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Check your internet connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseHttpError(e: HttpException): String {
        val rawBody = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val parsed = rawBody?.let {
            runCatching { gson.fromJson(it, ApiErrorResponse::class.java) }.getOrNull()
        }
        val reason = parsed?.error?.errors?.firstOrNull()?.reason
        val message = parsed?.error?.message

        return when (reason) {
            "commentsDisabled" -> "Comments are turned off for this video."
            "videoNotFound" -> "Video not found. It may be private or deleted."
            "quotaExceeded", "dailyLimitExceeded" ->
                "YouTube API daily quota exceeded for this API key."
            "keyInvalid", "forbidden", "accessNotConfigured" ->
                "Invalid or unconfigured YouTube Data API v3 key. Check your key in Settings."
            else -> message?.takeIf { it.isNotBlank() }
                ?: "YouTube API request failed (HTTP ${e.code()})."
        }
    }

    companion object {
        private val VIDEO_ID_PATTERNS = listOf(
            Regex("""(?:v=|vi=)([A-Za-z0-9_-]{11})"""),
            Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
            Regex("""youtube\.com/(?:shorts|embed|live|v)/([A-Za-z0-9_-]{11})"""),
            Regex("""^([A-Za-z0-9_-]{11})$""")
        )

        fun extractVideoId(input: String?): String? {
            if (input.isNullOrBlank()) return null
            val trimmed = input.trim()
            for (pattern in VIDEO_ID_PATTERNS) {
                val match = pattern.find(trimmed)
                if (match != null) return match.groupValues[1]
            }
            return null
        }
    }
}

class MissingApiKeyException : Exception(
    "Add your official YouTube Data API v3 key in Settings to load comments."
)
