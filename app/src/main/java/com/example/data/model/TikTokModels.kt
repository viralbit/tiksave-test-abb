package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class PostType {
    PHOTO_SLIDESHOW,
    VIDEO
}

data class TikTokPost(
    val id: String,
    val title: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String?,
    val type: PostType,
    val videoUrl: String?,
    val videoCoverUrl: String?,
    val imageUrls: List<String>,
    val originalUrl: String
)

@JsonClass(generateAdapter = true)
data class TikWmResponse(
    @Json(name = "code") val code: Int = -1,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "data") val data: TikWmData? = null
)

@JsonClass(generateAdapter = true)
data class TikWmData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "cover") val cover: String? = null,
    @Json(name = "origin_cover") val originCover: String? = null,
    @Json(name = "play") val play: String? = null,
    @Json(name = "hdplay") val hdPlay: String? = null,
    @Json(name = "wmplay") val wmPlay: String? = null,
    @Json(name = "images") val images: List<String>? = null,
    @Json(name = "author") val author: TikWmAuthor? = null,
    @Json(name = "music_info") val musicInfo: TikWmMusic? = null
)

@JsonClass(generateAdapter = true)
data class TikWmAuthor(
    @Json(name = "id") val id: String? = null,
    @Json(name = "unique_id") val uniqueId: String? = null,
    @Json(name = "nickname") val nickname: String? = null,
    @Json(name = "avatar") val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class TikWmMusic(
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "play") val play: String? = null
)
