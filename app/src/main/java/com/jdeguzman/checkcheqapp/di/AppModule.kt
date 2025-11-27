package com.jdeguzman.checkcheqapp.di

import android.content.Context
import androidx.room.Room
import com.jdeguzman.checkcheqapp.data.local.dao.ItemDao
import com.jdeguzman.checkcheqapp.data.local.dao.BasketDao
import com.jdeguzman.checkcheqapp.data.local.dao.PriceDao
import com.jdeguzman.checkcheqapp.data.local.db.AppDatabase
import com.jdeguzman.checkcheqapp.data.repository.PriceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "checkcheq.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao = db.itemDao()

    @Provides
    fun provideBasketDao(db: AppDatabase): BasketDao = db.basketDao()

    @Provides
    fun providePriceDao(db: AppDatabase): PriceDao = db.priceDao()

    @Provides
    @Singleton
    fun providePriceRepository(priceDao: PriceDao): PriceRepository =
        PriceRepository(priceDao)
}
