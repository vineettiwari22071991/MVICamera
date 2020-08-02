package com.example.camerascanmvi.foodlist

import com.example.camerascanmvi.MobiusVM
import com.example.camerascanmvi.model.ProductRepository
import com.example.camerascanmvi.utils.Navigator
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

fun foodlistupdate(
    model: FoodListModel,
    event: FoodListEvent
): Next<FoodListModel, FoodListEffect> {
    return when (event) {
        is AddButtonClick -> dispatch(setOf(NavigatetoScanner))
        is Initial -> dispatch(setOf(ObserverProduct))
        is ProductLoaded -> next(model.copy(products = event.products))
        is ProductClick -> dispatch(setOf(NavigatetoFoodDetail(event.barcode)))
    }
}

class FoodListViewModel @Inject constructor(
    navigator: Navigator,
    productRepository: ProductRepository
) :
    MobiusVM<FoodListModel, FoodListEvent, FoodListEffect>(
        "FoodListViewModel",
        Update(::foodlistupdate),
        FoodListModel(),
        RxMobius.subtypeEffectHandler<FoodListEffect, FoodListEvent>()
            .addAction(NavigatetoScanner::class.java)
            {
                navigator.to(FoodlistFragmentDirections.scan())
            }
            .addTransformer(ObserverProduct::class.java) { upstream ->
                upstream.switchMap {
                    productRepository.get().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).map { ProductLoaded(it) }
                }

            }
            .addConsumer(NavigatetoFoodDetail::class.java) { effect ->
                navigator.to(FoodlistFragmentDirections.fooddetailaction(effect.barcode))
            }
            .build()
    )