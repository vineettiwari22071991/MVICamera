package com.example.camerascanmvi.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NutrimentsDto(
    val energy_100g: Int,
    val salt_100g: Double?,
    val carbohydrates_100g: Double,
    val fiber_100g: Double?,
    val sugars_100g: Double?,
    val proteins_100g: Double,
    val fat_100g: Double
)