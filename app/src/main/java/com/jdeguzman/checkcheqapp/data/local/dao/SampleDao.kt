package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jdeguzman.checkcheqapp.data.local.entity.SampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleDao {
    @Query("SELECT * FROM samples ORDER BY id DESC")
    fun observeAll(): Flow<List<SampleEntity>>

    @Insert
    suspend fun insert(item: SampleEntity)
}
