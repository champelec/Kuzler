package com.example.myapplicationforkuzlerver1

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("products/")
    suspend fun createProduct(@Body product: ProductData): Response<ProductResponse>
}

data class ProductData(
    val nfc_id: String,
    val name: String,
    val price: String
)

data class ProductResponse(
    val id: Int,
    val nfc_id: String,
    val name: String,
    val price: String,
    val created_at: String
)