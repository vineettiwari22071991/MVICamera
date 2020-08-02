package com.example.camerascanmvi.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.camerascanmvi.R
import com.example.camerascanmvi.fooddetail.FoodDetailViewModel
import com.example.camerascanmvi.foodlist.FoodListViewModel
import com.example.camerascanmvi.model.ProductService
import com.example.camerascanmvi.model.database.ApplicationDatabase
import com.example.camerascanmvi.scan.ScanViewModel
import com.example.camerascanmvi.scan.utils.FrameProcessorOnSubscribe
import com.example.camerascanmvi.utils.ActivityService
import com.example.camerascanmvi.utils.IdlingResource
import com.example.camerascanmvi.utils.Navigator
import dagger.*
import dagger.multibindings.IntoMap
import kotlinx.android.synthetic.main.activity.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModuleKey(val value: KClass<out ViewModel>)

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
internal annotation class ApiBaseUrl

interface ApplicationComponent {

    fun viewModelFactory(): ViewModelProvider.Factory

    fun activityService(): ActivityService

    fun frameProcessorOnSubscribe(): FrameProcessorOnSubscribe
}

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class, ApiModule::class, DatabaseModule::class,
    RealModule::class])

/*interface ApplicationComponent {
    fun viewModelFactory(): ViewModelProvider.Factory

    fun activityService(): ActivityService

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): ApplicationComponent
    }
}*/

interface RealComponent : ApplicationComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): RealComponent
    }
}

@Module
object ApplicationModule {
    @Provides
    @Singleton
    @JvmStatic
    fun viewModels(provides: MutableMap<Class<out ViewModel>, Provider<ViewModel>>): ViewModelProvider.Factory {

        return ViewModelFactory(provides)
    }

    @Provides
    @Singleton
    @JvmStatic
    fun activityService(): ActivityService = ActivityService()


    @Provides
    @Singleton
    @JvmStatic
    fun navigator(activityService: ActivityService): Navigator {

        return Navigator(R.id.navigationHostFragment, activityService)
    }

    @Provides
    @ApiBaseUrl
    @JvmStatic
    fun apiBaseUrl(context: Context): String = context.getString(R.string.api_base_url)


}

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModuleKey(FoodListViewModel::class)
    abstract fun foodlistViewModel(viewModel: FoodListViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModuleKey(ScanViewModel::class)
    abstract fun scanViewModel(viewModel: ScanViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModuleKey(FoodDetailViewModel::class)
    abstract fun FoodDetailViewModel(viewModel: FoodDetailViewModel): ViewModel

}

@Module
object ApiModule {

    @Provides
    @Singleton
    @JvmStatic
    fun okHttpClient(): OkHttpClient {

        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }


    @Provides
    @Singleton
    @JvmStatic
    fun retrofit(@ApiBaseUrl apiBaseUrl: String, OkHttpClient: OkHttpClient): Retrofit {

        return Retrofit.Builder()
            .baseUrl(apiBaseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(OkHttpClient)
            .build()
    }


    @Provides
    @Singleton
    @JvmStatic
    fun productService(retrofit: Retrofit): ProductService {
        return retrofit.create(ProductService::class.java)
    }


}


@Module
object DatabaseModule {

    @Provides
    @Singleton
    @JvmStatic
    fun applicationDatabase(context: Context): ApplicationDatabase {
        return Room.databaseBuilder(context, ApplicationDatabase::class.java, "application")
            .build()
    }

    /*@Provides
    @Singleton
    @JvmStatic
    fun productDao(database: ApplicationDatabase) = database.productDao()*/
}


@Module
object RealModule {

    @Provides
    @Singleton
    @JvmStatic
    fun frameProcessorOnSubscribe(): FrameProcessorOnSubscribe = FrameProcessorOnSubscribe()

    @Provides
    @Singleton
    @JvmStatic
    fun idlingResource(): IdlingResource = object : IdlingResource {
        override fun increment() {}
        override fun decrement() {}
    }

    @Provides
    @Singleton
    @JvmStatic
    fun productDao(database: ApplicationDatabase) = database.productDao()
}