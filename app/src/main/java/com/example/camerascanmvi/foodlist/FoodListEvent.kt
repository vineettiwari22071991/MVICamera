package com.example.camerascanmvi.foodlist

import com.example.camerascanmvi.model.Product

sealed class FoodListEvent

object Initial : FoodListEvent()

data class ProductLoaded(val products: List<Product>) : FoodListEvent()

object AddButtonClick : FoodListEvent()

data class ProductClick(val barcode: String) : FoodListEvent()