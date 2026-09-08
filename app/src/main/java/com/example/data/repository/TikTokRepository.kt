package com.example.data.repository

import com.example.data.api.TikWmApi
import com.example.data.model.PostType
import com.example.data.model.TikTokPost
import com.example.data.model.TikWmResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

interface TikTokResolver {
    suspend fun resolve(url: String): Result<TikTokPost>
}

class TikTokRepository : TikTokResolver {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val primaryApi: TikWmApi = Retrofit.Builder()
        .baseUrl("https://www.tikwm.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(TikWmApi::class.java)

    private val fallbackApi: TikWmApi = Retrofit.Builder()
        .baseUrl("https://tikwm.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(TikWmApi::class.java)

    fun extractTikTokUrl(text: String): String? {
        if (text.isBlank()) return null
        val pattern = Pattern.compile("https?://[\\w-]+\\.(tiktok\\.com|douyin\\.com)[/\\w\\-?=&%.]*", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(0)
        }
        
        // General URL fallback
        val genPattern = Pattern.compile("https?://[^\\s]+", Pattern.CASE_INSENSITIVE)
        val genMatcher = genPattern.matcher(text)
        if (genMatcher.find()) {
            val url = genMatcher.group(0)
            if (url.contains("tiktok", ignoreCase = true) || url.contains("douyin", ignoreCase = true)) {
                return url
            }
            return url
        }
        return if (text.startsWith("http://") || text.startsWith("https://")) text.trim() else null
    }

    override suspend fun resolve(url: String): Result<TikTokPost> = withContext(Dispatchers.IO) {
        val extractedUrl = extractTikTokUrl(url) ?: return@withContext Result.failure(
            IllegalArgumentException("Please enter or paste a valid TikTok link")
        )

        // Try Primary API (POST)
        try {
            val response = primaryApi.resolvePostPost(extractedUrl)
            if (response.isSuccessful && response.body()?.code == 0) {
                val post = mapResponseToPost(response.body()!!, extractedUrl)
                return@withContext Result.success(post)
            }
        } catch (_: Exception) {
        }

        // Try Primary API (GET)
        try {
            val response = primaryApi.resolvePostGet(extractedUrl)
            if (response.isSuccessful && response.body()?.code == 0) {
                val post = mapResponseToPost(response.body()!!, extractedUrl)
                return@withContext Result.success(post)
            }
        } catch (_: Exception) {
        }

        // Try Fallback API (POST)
        try {
            val response = fallbackApi.resolvePostPost(extractedUrl)
            if (response.isSuccessful && response.body()?.code == 0) {
                val post = mapResponseToPost(response.body()!!, extractedUrl)
                return@withContext Result.success(post)
            }
        } catch (_: Exception) {
        }

        // Try Fallback API (GET)
        try {
            val response = fallbackApi.resolvePostGet(extractedUrl)
            if (response.isSuccessful && response.body()?.code == 0) {
                val post = mapResponseToPost(response.body()!!, extractedUrl)
                return@withContext Result.success(post)
            }
        } catch (_: Exception) {
        }

        return@withContext Result.failure(
            Exception("Couldn't fetch this link. Please check if the post is public and try again.")
        )
    }

    private fun mapResponseToPost(response: TikWmResponse, originalUrl: String): TikTokPost {
        val data = response.data ?: throw Exception("Empty response data")
        val id = data.id ?: System.currentTimeMillis().toString()
        val title = data.title.orEmpty().ifBlank { "TikTok Post $id" }
        val authorName = data.author?.nickname.orEmpty().ifBlank { "TikTok Creator" }
        val authorHandle = data.author?.uniqueId.orEmpty().let { if (it.isNotBlank()) "@$it" else "" }
        val authorAvatar = data.author?.avatar

        val images = data.images
        val isPhotoPost = !images.isNullOrEmpty()

        return if (isPhotoPost) {
            val fullImageUrls = images!!.map { img ->
                if (img.startsWith("http://") || img.startsWith("https://")) img else "https://www.tikwm.com$img"
            }
            TikTokPost(
                id = id,
                title = title,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatarUrl = authorAvatar,
                type = PostType.PHOTO_SLIDESHOW,
                videoUrl = null,
                videoCoverUrl = data.cover ?: data.originCover,
                imageUrls = fullImageUrls,
                originalUrl = originalUrl
            )
        } else {
            var rawVideoUrl = data.hdPlay.orEmpty().ifBlank { data.play.orEmpty() }
            if (rawVideoUrl.isNotBlank() && !rawVideoUrl.startsWith("http")) {
                rawVideoUrl = "https://www.tikwm.com$rawVideoUrl"
            }
            TikTokPost(
                id = id,
                title = title,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatarUrl = authorAvatar,
                type = PostType.VIDEO,
                videoUrl = rawVideoUrl,
                videoCoverUrl = data.cover ?: data.originCover,
                imageUrls = emptyList(),
                originalUrl = originalUrl
            )
        }
    }
}
