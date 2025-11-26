package com.jdeguzman.checkcheqapp.di

import android.content.Context
import androidx.room.Room
import com.jdeguzman.checkcheqapp.data.local.dao.BasketDao
import com.jdeguzman.checkcheqapp.data.local.dao.ItemDao
import com.jdeguzman.checkcheqapp.data.local.dao.PriceDao
import com.jdeguzman.checkcheqapp.data.local.db.AppDatabase
import com.jdeguzman.checkcheqapp.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 🔹 TODO: replace this with your real base URL
    private const val BASE_URL = "https://your-api-base-url.com/"

    // --- Network / API ---

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    // --- Room DB / DAOs ---

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "checkcheq.db"
        )
            // you can remove this later once you add proper migrations
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao = db.itemDao()

    @Singleton
    @Provides
    fun provideBasketDao(db: AppDatabase): BasketDao = db.basketDao()

    @Singleton
    @Provides
    fun providePriceDao(db: AppDatabase): PriceDao = db.priceDao()
}
