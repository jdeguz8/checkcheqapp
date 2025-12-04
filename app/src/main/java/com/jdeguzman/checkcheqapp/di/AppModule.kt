package com.jdeguzman.checkcheqapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jdeguzman.checkcheqapp.BuildConfig
import com.jdeguzman.checkcheqapp.data.local.dao.BasketDao
import com.jdeguzman.checkcheqapp.data.local.dao.ItemDao
import com.jdeguzman.checkcheqapp.data.local.dao.PricePostDao
import com.jdeguzman.checkcheqapp.data.local.db.AppDatabase
import com.jdeguzman.checkcheqapp.data.remote.yelp.YelpApiService
import com.jdeguzman.checkcheqapp.data.repository.PricePostRepository
import com.jdeguzman.checkcheqapp.data.repository.RoomPricePostRepository
import com.jdeguzman.checkcheqapp.data.repository.YelpRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Application-wide dependency container for CheckCheq.
 *
 * Provides:
 * - Room database + DAOs
 * - DataStore<Preferences> for settings
 * - Firebase Firestore + Storage
 * - Yelp Retrofit client + repository
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ---------- Room / Database ----------

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "checkcheq.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao = db.itemDao()

    @Provides
    fun provideBasketDao(db: AppDatabase): BasketDao = db.basketDao()

    @Provides
    fun providePricePostDao(db: AppDatabase): PricePostDao = db.pricePostDao()

    @Provides
    @Singleton
    fun providePricePostRepository(dao: PricePostDao): PricePostRepository =
        RoomPricePostRepository(dao)

    // ---------- DataStore (Settings) ----------

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext ctx: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { ctx.preferencesDataStoreFile("settings") }
        )
    }

    // ---------- Firebase ----------

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    // ---------- Moshi (shared for Yelp) ----------

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())   // <-- Kotlin-aware adapter
            .build()

    // ---------- Yelp: OkHttp + Retrofit + API + Repository ----------

    @Provides
    @Singleton
    fun provideYelpOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.YELP_API_KEY.trim()}")
                .header("Accept", "application/json")

            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideYelpRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.yelp.com/v3/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // <-- use our Moshi
            .build()

    @Provides
    @Singleton
    fun provideYelpApiService(retrofit: Retrofit): YelpApiService =
        retrofit.create(YelpApiService::class.java)

    @Provides
    @Singleton
    fun provideYelpRepository(api: YelpApiService): YelpRepository =
        YelpRepository(api)
}
