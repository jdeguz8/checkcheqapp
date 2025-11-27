package com.jdeguzman.checkcheqapp.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// UI model for a pin on the map
data class StorePricePin(
    val id: Long,
    val lat: Double,
    val lng: Double,
    val storeName: String,
    val itemName: String,
    val price: Double,
    val photoUri: String? = null
)

// UI state for the "Add price" dialog
data class AddDialogUi(
    val showAddDialog: Boolean = false,
    val lat: Double? = null,
    val lng: Double? = null
)

@HiltViewModel
class MyStoresViewModel @Inject constructor() : ViewModel() {

    // All map pins to render
    private val _pins = MutableStateFlow<List<StorePricePin>>(emptyList())
    val pins: StateFlow<List<StorePricePin>> = _pins.asStateFlow()

    // Dialog state
    private val _dialogUi = MutableStateFlow(AddDialogUi())
    val dialogUi: StateFlow<AddDialogUi> = _dialogUi.asStateFlow()

    /**
     * Called from GoogleMap.onMapLongClick().
     */
    fun onMapLongClick(lat: Double, lng: Double) {
        _dialogUi.value = AddDialogUi(
            showAddDialog = true,
            lat = lat,
            lng = lng
        )
    }

    fun onDismissDialog() {
        _dialogUi.value = AddDialogUi()
    }

    /**
     * Called by AddPriceDialog when the user taps "Save".
     */
    fun onAddPin(
        storeName: String,
        itemName: String,
        price: Double,
        imageUri: Uri?
    ) {
        val dialogSnapshot = _dialogUi.value
        val lat = dialogSnapshot.lat
        val lng = dialogSnapshot.lng

        // Safety check – should not really happen but avoids crashes
        if (!dialogSnapshot.showAddDialog || lat == null || lng == null) {
            _dialogUi.value = AddDialogUi()
            return
        }

        val newPin = StorePricePin(
            id = System.currentTimeMillis(), // simple unique-ish ID
            lat = lat,
            lng = lng,
            storeName = storeName,
            itemName = itemName,
            price = price,
            photoUri = imageUri?.toString()
        )

        _pins.value = _pins.value + newPin
        _dialogUi.value = AddDialogUi()
    }
}
