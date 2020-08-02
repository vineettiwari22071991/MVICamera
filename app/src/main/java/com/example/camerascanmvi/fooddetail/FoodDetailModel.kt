package com.example.camerascanmvi.fooddetail

import com.example.camerascanmvi.model.Product

data class FoodDetailModel(
    val activity: Boolean = false, val product: Product? = null,
    val error: Boolean = false
)