// PricePostRepository.kt
package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow

interface PricePostRepository {
    fun observePosts(): Flow<List<PricePost>>
    suspend fun add(post: PricePost)
    suspend fun clear()
}
