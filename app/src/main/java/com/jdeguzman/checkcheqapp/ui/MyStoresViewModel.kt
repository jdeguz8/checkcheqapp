package com.jdeguzman.checkcheqapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdeguzman.checkcheqapp.data.local.dao.StorePriceDao
import com.jdeguzman.checkcheqapp.data.local.entity.StorePriceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StorePricePin(
    val id: Long,
    val lat: Double,
    val lng: Double,
    val storeName: String,
    val itemName: String,
    val price: Double
)

data class DialogUiState(
    val showAddDialog: Boolean = false,
    val pendingLat: Double? = null,
    val pendingLng: Double? = null
)

@HiltViewModel
class MyStoresViewModel @Inject constructor(
    private val storePriceDao: StorePriceDao
) : ViewModel() {

    // pins coming from Room
    val pins: StateFlow<List<StorePricePin>> =
        storePriceDao.observeAll()
            .map { list ->
                list.map { it.toDomain() }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )

    // dialog / pending location state
    private val _dialogUi = MutableStateFlow(DialogUiState())
    val dialogUi: StateFlow<DialogUiState> = _dialogUi.asStateFlow()

    fun onMapLongClick(lat: Double, lng: Double) {
        _dialogUi.update {
            it.copy(
                showAddDialog = true,
                pendingLat = lat,
                pendingLng = lng
            )
        }
    }

    fun onDismissDialog() {
        _dialogUi.value = DialogUiState()
    }

    fun onAddPin(storeName: String, itemName: String, price: Double) {
        val current = _dialogUi.value
        val lat = current.pendingLat ?: return
        val lng = current.pendingLng ?: return

        viewModelScope.launch {
            val entity = StorePriceEntity(
                lat = lat,
                lng = lng,
                storeName = storeName,
                itemName = itemName,
                price = price
            )
            storePriceDao.insert(entity)
            _dialogUi.value = DialogUiState()
        }
    }
}

// mapping helper
private fun StorePriceEntity.toDomain(): StorePricePin =
    StorePricePin(
        id = id,
        lat = lat,
        lng = lng,
        storeName = storeName,
        itemName = itemName,
        price = price
    )
