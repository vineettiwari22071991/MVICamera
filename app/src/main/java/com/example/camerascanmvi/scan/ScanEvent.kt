package com.example.camerascanmvi.scan

import com.example.camerascanmvi.model.Product
import io.fotoapparat.preview.Frame

sealed class ScanEvent

data class Captured(val frame: Frame) : ScanEvent()

data class Detected(val barcode: String) : ScanEvent()

data class ProductLoaded(val product: Product) : ScanEvent()

object BarcodeError : ScanEvent()

object ProductInfoClicked : ScanEvent()