package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdeguzman.checkcheqapp.data.local.entity.PricePostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PricePostDao {

    @Query("SELECT * FROM price_posts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PricePostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: PricePostEntity): Long

    @Query("DELETE FROM price_posts")
    suspend fun clear()
}
