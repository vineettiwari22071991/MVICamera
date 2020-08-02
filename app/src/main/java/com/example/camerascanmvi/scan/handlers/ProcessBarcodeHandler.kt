package com.example.camerascanmvi.scan.handlers

import com.example.camerascanmvi.model.ProductRepository
import com.example.camerascanmvi.scan.BarcodeError
import com.example.camerascanmvi.scan.ProcessBarcode
import com.example.camerascanmvi.scan.ProductLoaded
import com.example.camerascanmvi.scan.ScanEvent
import com.example.camerascanmvi.utils.IdlingResource
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ProcessBarcodeHandler @Inject constructor(
    private val productRepository: ProductRepository,
    private val idlingResource: IdlingResource
) :
    ObservableTransformer<ProcessBarcode, ScanEvent> {
    override fun apply(upstream: Observable<ProcessBarcode>): ObservableSource<ScanEvent> {
        return upstream.switchMap { effect ->
            productRepository.getProductFromApi(effect.barcode)
                .map { product ->
                    idlingResource.decrement()
                    ProductLoaded(product) as ScanEvent
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { error ->
                    idlingResource.decrement()
                }
                .onErrorReturnItem(BarcodeError)
                .toObservable()
        }
    }
}
