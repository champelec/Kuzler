package com.example.myapplicationforkuzlerver1

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Модель данных, которую вы будете отправлять
data class DataModel(val subject: String, val price: String)

interface ApiService {
    @POST("/submitData")
    suspend fun submitData(@Body data: DataModel)

    companion object {
        private const val BASE_URL = "https://your_api_base_url.com"

        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}
