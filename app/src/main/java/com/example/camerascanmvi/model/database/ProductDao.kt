package com.example.camerascanmvi.model.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import com.example.camerascanmvi.model.dto.ProductDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
abstract class ProductDao {

    @Query("SELECT * FROM ProductDto WHERE id=:barcode")
    abstract fun getProduct(barcode: String): Single<ProductDto>


    @Insert
    abstract fun insert(productDto: ProductDto): Completable

    @Query("DELETE FROM productdto WHERE id=:barcode")
    abstract fun delete(barcode: String): Completable

    @Query("SELECT * FROM ProductDto")
    abstract fun get(): Observable<List<ProductDto>>

}
