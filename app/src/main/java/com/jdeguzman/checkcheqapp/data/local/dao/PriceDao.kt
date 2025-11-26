// In PriceDao.kt

package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.*
import com.jdeguzman.checkcheqapp.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {

    @Query("SELECT * FROM prices WHERE sku = :sku")
    fun observeBySku(sku: String): Flow<PriceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(price: PriceEntity)

    // --- CORRECTED METHOD ---
    // Changed to query by 'sku' which is a String, not a Long.
    // This now matches your PriceEntity.
    @Query("SELECT * FROM prices WHERE sku = :sku")
    suspend fun allFor(sku: String): List<PriceEntity>
}