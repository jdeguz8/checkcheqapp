package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.data.local.dao.PriceDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for price-related operations.
 *
 * Right now this is just a stub – we’re not calling any API or Firebase yet.
 * Later we can add those here and still keep the rest of the app the same.
 */
@Singleton
class PriceRepository @Inject constructor(
    private val priceDao: PriceDao
) {
    /**
     * Called from BasketViewModel.refreshPrices().
     * Currently a no-op so the app builds and runs.
     */
    suspend fun refreshForBasket(names: List<String>) {
        // TODO: in the future, fetch prices from network/Firebase and
        // write them into Room using priceDao.
    }
}
