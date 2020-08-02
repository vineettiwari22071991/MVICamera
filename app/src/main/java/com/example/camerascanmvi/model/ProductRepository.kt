package com.example.camerascanmvi.model

import com.example.camerascanmvi.model.database.ProductDao
import com.example.camerascanmvi.model.dto.NutrimentsDto
import com.example.camerascanmvi.model.dto.ProductDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productService: ProductService,
    private val productDao: ProductDao
) {

    fun get(): Observable<List<Product>> {
        return productDao.get()
            .map { it.map { dto -> mapProduct(dto, true) } }
    }

    fun loadProduct(barcode: String): Single<Product> {
        return getProductFromDatabase(barcode).onErrorResumeNext { getProductFromApi(barcode) }
    }

    fun getProductFromDatabase(barcode: String): Single<Product> {
        return productDao.getProduct(barcode).map { product ->
            mapProduct(product, true)
        }
    }

    fun getProductFromApi(barcode: String): Single<Product> {

        return productService.getProduct(barcode).map { response ->
            mapProduct(dto = response.product, saved = false)
        }
    }

    fun saveProduct(product: Product): Completable {
        return Single.fromCallable { mapProductDto(product) }.flatMapCompletable { productDto ->
            productDao.insert(productDto)
        }
    }

    fun deleteProduct(barcode: String): Completable {
        return productDao.delete(barcode)
    }

}

fun mapProduct(dto: ProductDto, saved: Boolean): Product {
    return Product(
        id = dto.id,
        name = dto.product_name,
        imageUrl = dto.image_url ?: "",
        brands = dto.brands,
        ingridients = dto.ingredients_text_debug ?: "",
        saved = saved,
        nutriments = mapNutriments(dto = dto.nutriments)

    )
}

fun mapNutriments(dto: NutrimentsDto?): Nutriments? {

    if (dto == null) return null
    return Nutriments(
        energy = dto.energy_100g,
        salt = dto.salt_100g,
        carbohydrates = dto.carbohydrates_100g,
        fiber = dto.fiber_100g,
        sugars = dto.sugars_100g,
        proteins = dto.proteins_100g,
        fat = dto.fat_100g
    )

}


private fun mapProductDto(product: Product): ProductDto {
    return ProductDto(
        id = product.id,
        product_name = product.name,
        brands = product.brands,
        image_url = product.imageUrl,
        ingredients_text_debug = product.ingridients,
        nutriments = mapNutrimentsDto(product.nutriments)
    )
}

fun mapNutrimentsDto(nutriments: Nutriments?): NutrimentsDto? {
    if (nutriments == null) return null
    return NutrimentsDto(
        energy_100g = nutriments.energy,
        salt_100g = nutriments.salt,
        carbohydrates_100g = nutriments.carbohydrates,
        fiber_100g = nutriments.fiber,
        sugars_100g = nutriments.sugars,
        proteins_100g = nutriments.proteins,
        fat_100g = nutriments.fat
    )
}