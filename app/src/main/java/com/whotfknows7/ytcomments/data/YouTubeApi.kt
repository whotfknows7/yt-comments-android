package com.whotfknows7.ytcomments.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface YouTubeApiService {

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,statistics",
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): VideoListResponse

    @GET("commentThreads")
    suspend fun getCommentThreads(
        @Query("part") part: String = "snippet,replies",
        @Query("videoId") videoId: String,
        @Query("order") order: String = "relevance",
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("textFormat") textFormat: String = "plainText",
        @Query("key") apiKey: String
    ): CommentThreadListResponse

    @GET("comments")
    suspend fun getCommentReplies(
        @Query("part") part: String = "snippet",
        @Query("parentId") parentId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("textFormat") textFormat: String = "plainText",
        @Query("key") apiKey: String
    ): CommentListResponse

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

        fun create(): YouTubeApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YouTubeApiService::class.java)
        }
    }
}

data class VideoListResponse(
    @SerializedName("items") val items: List<VideoDto> = emptyList()
)

data class VideoDto(
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: VideoSnippetDto? = null,
    @SerializedName("statistics") val statistics: VideoStatisticsDto? = null
)

data class VideoSnippetDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("channelId") val channelId: String = "",
    @SerializedName("channelTitle") val channelTitle: String = "",
    @SerializedName("publishedAt") val publishedAt: String = "",
    @SerializedName("thumbnails") val thumbnails: ThumbnailsDto? = null
)

data class ThumbnailsDto(
    @SerializedName("maxres") val maxres: ThumbnailDto? = null,
    @SerializedName("standard") val standard: ThumbnailDto? = null,
    @SerializedName("high") val high: ThumbnailDto? = null,
    @SerializedName("medium") val medium: ThumbnailDto? = null,
    @SerializedName("default") val defaultThumb: ThumbnailDto? = null
) {
    fun bestUrl(): String =
        maxres?.url ?: standard?.url ?: high?.url ?: medium?.url ?: defaultThumb?.url.orEmpty()
}

data class ThumbnailDto(
    @SerializedName("url") val url: String = ""
)

data class VideoStatisticsDto(
    @SerializedName("viewCount") val viewCount: String? = null,
    @SerializedName("likeCount") val likeCount: String? = null,
    @SerializedName("commentCount") val commentCount: String? = null
)

data class CommentThreadListResponse(
    @SerializedName("nextPageToken") val nextPageToken: String? = null,
    @SerializedName("items") val items: List<CommentThreadDto> = emptyList()
)

data class CommentThreadDto(
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: CommentThreadSnippetDto,
    @SerializedName("replies") val replies: CommentThreadRepliesDto? = null
)

data class CommentThreadSnippetDto(
    @SerializedName("videoId") val videoId: String = "",
    @SerializedName("topLevelComment") val topLevelComment: CommentDto,
    @SerializedName("totalReplyCount") val totalReplyCount: Int = 0
)

data class CommentThreadRepliesDto(
    @SerializedName("comments") val comments: List<CommentDto> = emptyList()
)

data class CommentListResponse(
    @SerializedName("nextPageToken") val nextPageToken: String? = null,
    @SerializedName("items") val items: List<CommentDto> = emptyList()
)

data class CommentDto(
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: CommentSnippetDto
)

data class CommentSnippetDto(
    @SerializedName("authorDisplayName") val authorDisplayName: String = "Unknown",
    @SerializedName("authorProfileImageUrl") val authorProfileImageUrl: String = "",
    @SerializedName("authorChannelId") val authorChannelId: AuthorChannelIdDto? = null,
    @SerializedName("textDisplay") val textDisplay: String = "",
    @SerializedName("textOriginal") val textOriginal: String? = null,
    @SerializedName("likeCount") val likeCount: Long = 0L,
    @SerializedName("publishedAt") val publishedAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
)

data class AuthorChannelIdDto(
    @SerializedName("value") val value: String? = null
)

data class ApiErrorResponse(
    @SerializedName("error") val error: ApiErrorBody? = null
)

data class ApiErrorBody(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("errors") val errors: List<ApiErrorDetail>? = null
)

data class ApiErrorDetail(
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("message") val message: String? = null
)

enum class CommentSortOrder(val apiValue: String, val label: String) {
    RELEVANCE("relevance", "Top comments"),
    TIME("time", "Newest first")
}

data class VideoInfo(
    val id: String,
    val title: String,
    val channelId: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val viewCount: Long?,
    val likeCount: Long?,
    val commentCount: Long?,
    val publishedAt: String
)

data class CommentItem(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val authorChannelId: String?,
    val text: String,
    val likeCount: Long,
    val publishedAt: String,
    val isEdited: Boolean,
    val totalReplyCount: Int,
    val previewReplies: List<ReplyItem> = emptyList()
)

data class ReplyItem(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val authorChannelId: String?,
    val text: String,
    val likeCount: Long,
    val publishedAt: String,
    val isEdited: Boolean
)

data class CommentPage(
    val comments: List<CommentItem>,
    val nextPageToken: String?
)

data class ReplyPage(
    val replies: List<ReplyItem>,
    val nextPageToken: String?
)

data class RecentVideoEntry(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String
)
