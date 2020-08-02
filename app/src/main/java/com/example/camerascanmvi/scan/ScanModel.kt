package com.example.camerascanmvi.scan

import com.example.camerascanmvi.model.Product

data class ScanModel(
    val activity: Boolean = false,
    val processBarcodeResult: ProcessBarcodeResult = ProcessBarcodeResult.Empty
)

sealed class ProcessBarcodeResult {
    object Empty : ProcessBarcodeResult()
    object Error : ProcessBarcodeResult()
    data class ProductLoaded(val product: Product) : ProcessBarcodeResult()
}