package com.example.camerascanmvi.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Nutriments(
    val energy: Int,
    val salt: Double?,
    val carbohydrates: Double,
    val fiber: Double?,
    val sugars: Double?,
    val proteins: Double,
    val fat: Double
)