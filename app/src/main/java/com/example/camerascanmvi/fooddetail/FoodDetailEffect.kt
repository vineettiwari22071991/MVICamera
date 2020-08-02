package com.example.camerascanmvi.fooddetail

import com.example.camerascanmvi.model.Product

sealed class FoodDetailEffect

data class LoadProduct(val barcode: String) : FoodDetailEffect()

data class DeleteProduct(val barcode: String) : FoodDetailEffect()

data class SaveProduct(val product: Product) : FoodDetailEffect()
