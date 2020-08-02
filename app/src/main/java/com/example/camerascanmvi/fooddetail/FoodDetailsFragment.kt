package com.example.camerascanmvi.fooddetail

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.camerascanmvi.R
import com.example.camerascanmvi.getViewModel
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.food_details_fragment.*

class FoodDetailsFragment : Fragment(R.layout.food_details_fragment) {


    private val args: FoodDetailsFragmentArgs by navArgs()

    private lateinit var disposable: Disposable

    override fun onStart() {
        super.onStart()
        disposable = actionButton.clicks().map {
            ActionButtonClick
        }
            .compose(getViewModel(FoodDetailViewModel::class).init(Initial(args.barcode)))
            .subscribe { model ->
                loadingIndicatorfood.isVisible = model.activity
                contentView.isVisible = model.product != null
                model.product?.let { product ->
                    productNameView.text = product.name
                    brandNameView.text = product.brands
                    barcodeView.text = product.id
                    energyValueView.text = getString(
                        R.string.food_details_energy_value,
                        product.nutriments?.energy
                    )
                    carbsValueView.text = getString(
                        R.string.food_details_macro_value,
                        product.nutriments?.carbohydrates
                    )
                    fatValueView.text = getString(
                        R.string.food_details_macro_value,
                        product.nutriments?.fat
                    )
                    proteinValueView.text = getString(
                        R.string.food_details_macro_value,
                        product.nutriments?.proteins
                    )
                    ingridientsText.text = getString(
                        R.string.food_details_ingridients,
                        product.ingridients
                    )
                    actionButton.text = if (product.saved) {
                        getString(R.string.food_details_delete)
                    } else {
                        getString(R.string.food_details_save)
                    }

                    Glide.with(requireContext())
                        .load(product.imageUrl)
                        .fitCenter()
                        .into(productImageView)
                }
            }

    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}