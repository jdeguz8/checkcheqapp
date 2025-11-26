package com.jdeguzman.checkcheqapp.data.parser


import javax.inject.Inject
import kotlin.random.Random

class FakeParser @Inject constructor() : StoreParser {
    override val storeId: String = "fake_store"

    override suspend fun fetchPrices(targets: List<String>): Map<String, ParsedPrice> {
        return targets.associateWith { key ->
            val price = Random.nextInt(250, 500) / 100.0  // $2.50–$5.00
            ParsedPrice(key = key, amount = price, unit = "ea")
        }
    }
}