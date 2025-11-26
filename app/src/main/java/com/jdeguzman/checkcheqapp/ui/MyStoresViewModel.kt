package com.jdeguzman.checkcheqapp.ui

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// A single pin on the map
data class StorePricePin(
    val id: Long,
    val itemName: String,
    val price: Double,
    val storeName: String,
    val position: LatLng,
    val photoUri: String? = null,          // reserved for later when we wire photos
    val createdAt: Long = System.currentTimeMillis()
)

@HiltViewModel
class MyStoresViewModel @Inject constructor(
    // later: inject repo / DAO / Firebase here
) : ViewModel() {

    private val _pins = MutableStateFlow<List<StorePricePin>>(emptyList())
    val pins: StateFlow<List<StorePricePin>> = _pins.asStateFlow()

    // null = no dialog; non-null = show "add pin" dialog for this location
    private val _pendingLatLng = MutableStateFlow<LatLng?>(null)
    val pendingLatLng: StateFlow<LatLng?> = _pendingLatLng.asStateFlow()

    fun onMapLongClick(latLng: LatLng) {
        _pendingLatLng.value = latLng
    }

    fun addPin(itemName: String, price: Double, storeName: String) {
        val pos = _pendingLatLng.value ?: return

        val cleanItem = itemName.trim()
        val cleanStore = storeName.trim()
        val p = price

        if (cleanItem.isEmpty() || cleanStore.isEmpty()) return

        val pin = StorePricePin(
            id = System.currentTimeMillis(),
            itemName = cleanItem,
            price = p,
            storeName = cleanStore,
            position = pos
        )

        _pins.update { it + pin }
        _pendingLatLng.value = null
    }

    fun cancelAddPin() {
        _pendingLatLng.value = null
    }

    // later: add "prune pins older than 7 days" here for weekly refresh logic
}
