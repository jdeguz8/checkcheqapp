package com.jdeguzman.checkcheqapp.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Shared VM for map + feed.
 * Holds a list of price posts (pins) entirely in memory for now.
 */
@HiltViewModel
class MyStoresViewModel @Inject constructor() : ViewModel() {

    // One crowd-sourced post
    data class PricePost(
        val id: String,
        val storeName: String,
        val itemName: String,
        val price: Double,
        val currency: String = "CAD",
        val lat: Double,
        val lng: Double,
        val photoUrl: String? = null,
        val createdAt: Long = System.currentTimeMillis()
    )

    // UI state for the “add price” dialog
    data class DialogUi(
        val showAddDialog: Boolean = false,
        val pendingLat: Double? = null,
        val pendingLng: Double? = null
    )

    // All posts/pins currently in the app
    private val _pins = MutableStateFlow<List<PricePost>>(seedFakePosts())
    val pins: StateFlow<List<PricePost>> = _pins.asStateFlow()

    // Dialog state
    private val _dialogUi = MutableStateFlow(DialogUi())
    val dialogUi: StateFlow<DialogUi> = _dialogUi.asStateFlow()

    /** User long-presses map → remember location and show dialog */
    fun onMapLongClick(lat: Double, lng: Double) {
        _dialogUi.value = DialogUi(
            showAddDialog = true,
            pendingLat = lat,
            pendingLng = lng
        )
    }

    /** User hits “Save” in dialog → create a new post pinned to that location */
    fun onAddPin(storeName: String, itemName: String, price: Double) {
        val dialog = _dialogUi.value
        val lat = dialog.pendingLat ?: return
        val lng = dialog.pendingLng ?: return

        val newPost = PricePost(
            id = System.currentTimeMillis().toString(),
            storeName = storeName.ifBlank { "Unknown store" },
            itemName = itemName.ifBlank { "Unknown item" },
            price = price,
            lat = lat,
            lng = lng
        )

        _pins.value = _pins.value + newPost
        _dialogUi.value = DialogUi() // reset dialog
    }

    fun onDismissDialog() {
        _dialogUi.value = DialogUi()
    }

    // Some starter posts so the feed/map don’t look empty
    private fun seedFakePosts(): List<PricePost> = listOf(
        PricePost(
            id = "1",
            storeName = "Superstore Kenaston",
            itemName = "2L Milk",
            price = 4.49,
            lat = 49.84,
            lng = -97.20
        ),
        PricePost(
            id = "2",
            storeName = "Walmart Polo Park",
            itemName = "Dozen Eggs",
            price = 3.99,
            lat = 49.88,
            lng = -97.19
        )
    )
}
