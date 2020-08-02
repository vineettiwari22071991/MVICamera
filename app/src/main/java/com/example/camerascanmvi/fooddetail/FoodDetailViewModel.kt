package com.example.camerascanmvi.fooddetail

import com.example.camerascanmvi.MobiusVM
import com.example.camerascanmvi.model.ProductRepository
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next
import com.spotify.mobius.Next.*
import com.spotify.mobius.Update
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

fun fooddetailUpdate(
    model: FoodDetailModel,
    event: FoodDetailEvent
): Next<FoodDetailModel, FoodDetailEffect> {
    return when (event) {
        is Initial -> next(
            model.copy(activity = true),
            setOf(LoadProduct(event.barcode))
        )
        is ActionButtonClick -> if (model.product != null) {
            if (model.product.saved) {
                dispatch<FoodDetailModel, FoodDetailEffect>(
                    setOf(DeleteProduct(model.product.id))
                )

            } else {
                dispatch<FoodDetailModel, FoodDetailEffect>(
                    setOf(SaveProduct(model.product))
                )
            }

        } else {
            noChange()
        }
        is ProductLoaded -> next(
            model.copy(activity = false, product = event.product)
        )
        is ErrorLoadingProduct -> next(
            model.copy(activity = false, error = true)
        )
    }
}


class FoodDetailViewModel @Inject constructor(
    productRepository: ProductRepository
) : MobiusVM<FoodDetailModel, FoodDetailEvent, FoodDetailEffect>(
    "FoodDetailsViewModel",
    Update(::fooddetailUpdate),
    FoodDetailModel(),
    RxMobius.subtypeEffectHandler<FoodDetailEffect, FoodDetailEvent>()
        .addTransformer(LoadProduct::class.java) { upstream ->
            upstream.switchMap { effect ->
                productRepository.loadProduct(effect.barcode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .map { product ->
                        ProductLoaded(product) as FoodDetailEvent
                    }
                    .onErrorReturn { ErrorLoadingProduct }
            }
        }
        .addTransformer(SaveProduct::class.java) { upstream ->
            upstream.switchMap { effect ->
                productRepository.saveProduct(effect.product)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable<FoodDetailEvent>()
            }
        }
        .addTransformer(DeleteProduct::class.java) { upstream ->
            upstream.switchMap { effect ->
                productRepository.deleteProduct(effect.barcode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable<FoodDetailEvent>()
            }
        }
        .build()
)
