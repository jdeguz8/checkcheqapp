package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.jdeguzman.checkcheqapp.data.local.entity.ItemEntity


@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ItemEntity?

    @Query("SELECT * FROM items WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByName(name: String): ItemEntity?

    @Query("SELECT * FROM items WHERE name LIKE '%' || :q || '%' ORDER BY name")
    fun search(q: String): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: ItemEntity): List<Long>

    @Update suspend fun update(item: ItemEntity)
    @Delete suspend fun delete(item: ItemEntity)
//    @Query("DELETE FROM items") suspend fun clear()
//    fun insert(item: Item)
}