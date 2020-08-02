package com.example.camerascanmvi.foodlist

sealed class FoodListEffect

object ObserverProduct : FoodListEffect()

object NavigatetoScanner : FoodListEffect()

data class NavigatetoFoodDetail(val barcode: String) : FoodListEffect()