// In Planner.kt

package com.jdeguzman.checkcheqapp.domain

// Note: You will need to adapt your BasketItem to use sku: String instead of itemId: Long
// For now, I'll assume you have a way to get the SKU for a BasketItem.
import com.jdeguzman.checkcheqapp.data.local.BasketItem
import com.jdeguzman.checkcheqapp.data.local.dao.PriceDao
import javax.inject.Inject

data class Plan(
    val stores: List<String>,
    val total: Double,
    val breakdown: Map<String, String> // sku -> storeId
)

class Planner @Inject constructor(
    private val priceDao: PriceDao
) {
    // I've renamed the function to be clearer about what it does.
    // The basket logic will need to be adapted to provide a SKU.
    suspend fun findBestSingleStorePlan(basket: Map<String, Double>, storeIds: List<String>): Plan? {
        var bestPlan: Plan? = null

        for (storeId in storeIds) {
            var currentTotal = 0.0
            val breakdownForStore = mutableMapOf<String, String>()

            for ((sku, quantity) in basket) {
                // Find the price for this SKU specifically in the current store
                val priceEntity = priceDao.allFor(sku).firstOrNull { it.store == storeId }
                    ?: break // If any item is missing, this store is invalid. Stop checking it.

                // --- FIX 1: Use 'currentPrice' ---
                currentTotal += priceEntity.currentPrice * quantity
                breakdownForStore[sku] = storeId
            }

            // A plan is valid only if we found a price for every single item in the basket
            if (breakdownForStore.size == basket.size) {
                // If this is the first valid plan, or if it's cheaper than the previous best, save it.
                if (bestPlan == null || currentTotal < bestPlan.total) {
                    bestPlan = Plan(
                        stores = listOf(storeId),
                        total = currentTotal,
                        breakdown = breakdownForStore
                    )
                }
            }
        }
        return bestPlan
    }
}