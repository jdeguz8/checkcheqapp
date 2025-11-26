package com.jdeguzman.checkcheqapp.data.parser;

data class ParsedPrice(
        val key: String,   // normalized item name
        val amount: Double,
        val unit: String   // "ea","L","kg"
)

interface StoreParser {
    val storeId: String
    suspend fun fetchPrices(targets: List<String>): Map<String, ParsedPrice>
}