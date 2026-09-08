package com.example.data.api

import com.example.data.model.TikWmResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TikWmApi {
    @FormUrlEncoded
    @POST("api/")
    suspend fun resolvePostPost(
        @Field("url") url: String,
        @Field("hd") hd: Int = 1
    ): Response<TikWmResponse>

    @GET("api/")
    suspend fun resolvePostGet(
        @Query("url") url: String,
        @Query("hd") hd: Int = 1
    ): Response<TikWmResponse>
}
