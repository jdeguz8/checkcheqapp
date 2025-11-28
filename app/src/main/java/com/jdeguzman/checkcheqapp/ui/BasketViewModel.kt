//package com.jdeguzman.checkcheqapp.ui
//
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.jdeguzman.checkcheqapp.data.local.BasketItem
//import com.jdeguzman.checkcheqapp.data.local.toDomain
//import com.jdeguzman.checkcheqapp.data.local.toEntity
//import com.jdeguzman.checkcheqapp.data.local.dao.ItemDao
//import com.jdeguzman.checkcheqapp.data.local.dao.BasketDao
//import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity   // <-- make sure this is here
//import com.jdeguzman.checkcheqapp.data.local.entity.ItemEntity
//import com.jdeguzman.checkcheqapp.data.repository.PriceRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class BasketViewModel @Inject constructor(
//    private val itemDao: ItemDao,
//    private val basketDao: BasketDao,
//    private val repo: PriceRepository
//) : ViewModel() {
//
//    // Basket as domain models
//    val basket: StateFlow<List<BasketItem>> = basketDao.observeAll()
//        .map { entities: List<BasketItemEntity> ->
//            entities.map { it.toDomain() }
//        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000L),
//            initialValue = emptyList()
//        )
//
//    /**
//     * Add an item to the basket by name.
//     * If it already exists in the basket, bump its qty.
//     * Also make sure it exists in the Item table.
//     */
//    fun addItem(rawName: String) = viewModelScope.launch {
//        val name = rawName.trim()
//        if (name.isBlank()) return@launch
//
//        // 1) Ensure there is an ItemEntity
//        val existingItem = itemDao.getByName(name)
//        if (existingItem == null) {
//            // Create a simple item row
//            val newItem = ItemEntity(name = name)
//            itemDao.upsert(newItem)
//        }
//
//        // 2) Update basket: if already present, increment qty; else add new row with qty=1
//        val current = basket.value.firstOrNull { it.name.equals(name, ignoreCase = true) }
//        val newQty = (current?.qty ?: 0) + 1
//
//        val updated = BasketItem(
//            id = current?.id ?: 0L,
//            name = name,
//            qty = newQty,
//            price = current?.price ?: 0.0
//        )
//
//        basketDao.upsert(updated.toEntity())
//    }
//
//    /** Clear the whole basket. */
//    fun clearBasket() = viewModelScope.launch {
//        basketDao.clear()
//    }
//
//    /**
//     * Ask the repository to refresh prices for all items in the basket.
//     * (Even if you haven’t fully implemented the network side yet,
//     * this is a great talking point in your demo.)
//     */
//    fun refreshPrices() = viewModelScope.launch {
//        val targetNames = basket.value.map { it.name }.distinct()
//        if (targetNames.isNotEmpty()) {
//            repo.refreshForBasket(targetNames)
//        }
//    }
//}
