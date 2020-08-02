package com.example.camerascanmvi.foodlist

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.camerascanmvi.R
import com.example.camerascanmvi.foodlist.widget.FoodListAdapter
import com.example.camerascanmvi.getViewModel
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.foodlistfrgamentlayout.*

class FoodlistFragment : Fragment(R.layout.foodlistfrgamentlayout) {

    lateinit var disposable: Disposable
    override fun onStart() {
        super.onStart()

        rv.layoutManager = LinearLayoutManager(context)
        val adapter = FoodListAdapter()

        rv.adapter = adapter


        disposable = Observable.mergeArray(addButton.clicks().map {
            AddButtonClick
        },
            adapter.productClicks.map {
                ProductClick(it.id)
            }
        )
            .compose(getViewModel(FoodListViewModel::class).init(Initial))
            .subscribe { model ->
                adapter.submitList(model.products)
            }

    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}