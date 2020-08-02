package com.example.camerascanmvi.model

data class Product(
    val id: String,
    val saved: Boolean,
    val name: String,
    val imageUrl: String,
    val brands: String,
    val ingridients: String,
    val nutriments: Nutriments?
)