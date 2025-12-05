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

    /**
     * Provide the singleton Room [AppDatabase] instance.
     */
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "checkcheq.db")
            .fallbackToDestructiveMigration()
            .build()

    /**
     * Provide [ItemDao] from [AppDatabase].
     */
    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao = db.itemDao()

    /**
     * Provide [BasketDao] from [AppDatabase].
     */
    @Provides
    fun provideBasketDao(db: AppDatabase): BasketDao = db.basketDao()

    /**
     * Provide [PricePostDao] from [AppDatabase].
     */
    @Provides
    fun providePricePostDao(db: AppDatabase): PricePostDao = db.pricePostDao()

    /**
     * Provide the [PricePostRepository] backed by Room.
     */
    @Provides
    @Singleton
    fun providePricePostRepository(dao: PricePostDao): PricePostRepository =
        RoomPricePostRepository(dao)

    // ---------- DataStore (Settings) ----------

    /**
     * Provide the singleton [DataStore] of [Preferences] for settings.
     */
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

    /**
     * Provide the singleton Firestore instance.
     */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Provide the singleton Firebase Storage instance.
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    // ---------- Moshi (shared for Yelp) ----------

    /**
     * Provide a configured [Moshi] instance with Kotlin support.
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    // ---------- Yelp: OkHttp + Retrofit + API + Repository ----------

    /**
     * Provide an [OkHttpClient] with Yelp authorization + logging.
     */
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

    /**
     * Provide the Yelp [Retrofit] instance using Moshi.
     */
    @Provides
    @Singleton
    fun provideYelpRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.yelp.com/v3/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    /**
     * Provide the [YelpApiService] created from [Retrofit].
     */
    @Provides
    @Singleton
    fun provideYelpApiService(retrofit: Retrofit): YelpApiService =
        retrofit.create(YelpApiService::class.java)

    /**
     * Provide the [YelpRepository] that wraps Yelp API calls.
     */
    @Provides
    @Singleton
    fun provideYelpRepository(api: YelpApiService): YelpRepository =
        YelpRepository(api)
}
