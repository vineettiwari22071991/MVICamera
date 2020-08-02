package com.example.camerascanmvi.scan.handlers

import com.example.camerascanmvi.scan.Detected
import com.example.camerascanmvi.scan.ProcessCameraFrame
import com.example.camerascanmvi.scan.ScanEvent
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ProcessFrameHandler @Inject constructor() :
    ObservableTransformer<ProcessCameraFrame, ScanEvent> {

    val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(
            FirebaseVisionBarcode.FORMAT_CODE_128,
            FirebaseVisionBarcode.FORMAT_EAN_13
        ).build()


    override fun apply(upstream: Observable<ProcessCameraFrame>): ObservableSource<ScanEvent> {

        return upstream.flatMap { effect ->
            Observable.create<ScanEvent> { emitter ->
                val metadata =
                    FirebaseVisionImageMetadata.Builder().setWidth(effect.frame.size.width)
                        .setHeight(effect.frame.size.height)
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setRotation(effect.frame.rotation)
                        .build()

                val image = FirebaseVisionImage.fromByteArray(effect.frame.image, metadata)
                val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

                detector.detectInImage(image)
                    .addOnSuccessListener { barcodes ->

                        if (barcodes.isNotEmpty()) {
                            emitter.onNext(Detected(barcodes[0].rawValue.toString()))
                        }

                    }
                    .addOnFailureListener {
                        emitter.onComplete()
                    }
            }

        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


}