package com.example.camerascanmvi.scan

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.camerascanmvi.R
import com.example.camerascanmvi.component
import com.example.camerascanmvi.getViewModel
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.jakewharton.rxbinding3.view.clicks
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.preview.Frame
import io.fotoapparat.selector.continuousFocusPicture
import io.fotoapparat.selector.manualExposure
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.product_layout_small.*
import kotlinx.android.synthetic.main.scan_fragment.*
import java.lang.IllegalStateException

class ScanFragment : Fragment(R.layout.scan_fragment) {

    private val ORIENTATIONS = SparseIntArray()

    private lateinit var disposable: Disposable

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }

    private lateinit var fotoapparat: Fotoapparat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val frameProcessor = requireContext().component.frameProcessorOnSubscribe()
        fotoapparat = Fotoapparat(
            context = requireContext(),
            view = cameraView,
            cameraConfiguration = CameraConfiguration(
                frameProcessor = frameProcessor,
                exposureCompensation = manualExposure(4),
                focusMode = continuousFocusPicture()
            )
        )

        val cameraId = findRearFacingCameraId()

        disposable = Observable.mergeArray(Observable.create(frameProcessor)
            .map { frame ->
                Captured(
                    frame.copy(
                        rotation = getRotationCompensation(
                            cameraId,
                            this.activity as Activity,
                            this.context!!
                        )
                    )
                )
            },
            productView.clicks().map { ProductInfoClicked }
        )
            .compose(getViewModel(ScanViewModel::class))
            .subscribe { model ->
                loadingIndicator.isVisible = model.activity
                productView.isVisible =
                    model.processBarcodeResult is ProcessBarcodeResult.ProductLoaded
                errorView.isVisible = model.processBarcodeResult is ProcessBarcodeResult.Error

                if (model.processBarcodeResult is ProcessBarcodeResult.ProductLoaded) {
                    productNameView.text = model.processBarcodeResult.product.name
                    brandNameView.text = model.processBarcodeResult.product.brands
                    energyValueView.text = getString(
                        R.string.scan_energy_value,
                        model.processBarcodeResult.product.nutriments?.energy
                    )
                    carbsValueView.text = getString(
                        R.string.scan_macro_value,
                        model.processBarcodeResult.product.nutriments?.carbohydrates
                    )
                    fatValueView.text = getString(
                        R.string.scan_macro_value,
                        model.processBarcodeResult.product.nutriments?.fat
                    )
                    proteinValueView.text = getString(
                        R.string.scan_macro_value,
                        model.processBarcodeResult.product.nutriments?.proteins
                    )

                    Glide.with(requireContext())
                        .load(model.processBarcodeResult.product.imageUrl)
                        .fitCenter()
                        .into(productImageView)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        handleCameraPermission(false)
    }

    override fun onStop() {
        fotoapparat.stop()
        super.onStop()
    }

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleCameraPermission(true)
    }

    private fun handleCameraPermission(permissionResult: Boolean) {
        if (hasCameraPermission()) {
            fotoapparat.start()
        } else if (!permissionResult || shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun findRearFacingCameraId(): String {
        val cameraManager = activity?.getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        cameraIds.forEach { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val orientation = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (orientation == CameraCharacteristics.LENS_FACING_BACK) return id
        }
        throw IllegalStateException("Unable to find camera id")
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(
        cameraId: String,
        activity: Activity,
        context: Context
    ): Int {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        val deviceRotation = activity.windowManager.defaultDisplay.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        val sensorOrientation = cameraManager
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        val result: Int
        when (rotationCompensation) {
            0 -> result = FirebaseVisionImageMetadata.ROTATION_0
            90 -> result = FirebaseVisionImageMetadata.ROTATION_90
            180 -> result = FirebaseVisionImageMetadata.ROTATION_180
            270 -> result = FirebaseVisionImageMetadata.ROTATION_270
            else -> {
                result = FirebaseVisionImageMetadata.ROTATION_0
                Log.e("Rotation", "Bad rotation value: $rotationCompensation")
            }
        }
        return result
    }
}
