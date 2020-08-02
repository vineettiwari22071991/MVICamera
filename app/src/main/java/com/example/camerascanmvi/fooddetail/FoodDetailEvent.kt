package com.example.camerascanmvi.fooddetail

import com.example.camerascanmvi.model.Product

sealed class FoodDetailEvent


data class Initial(val barcode: String) : FoodDetailEvent()

object ActionButtonClick : FoodDetailEvent()

data class ProductLoaded(val product: Product) : FoodDetailEvent()

object ErrorLoadingProduct : FoodDetailEvent()