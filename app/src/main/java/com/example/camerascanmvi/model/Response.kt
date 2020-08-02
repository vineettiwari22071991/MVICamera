package com.example.camerascanmvi.model

import com.example.camerascanmvi.model.dto.ProductDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Response(
    val product: ProductDto
)