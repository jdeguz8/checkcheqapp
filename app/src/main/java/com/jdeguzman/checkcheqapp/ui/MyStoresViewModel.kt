package com.jdeguzman.checkcheqapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdeguzman.checkcheqapp.data.repository.PricePostRepository
import com.jdeguzman.checkcheqapp.domain.PricePost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyStoresViewModel @Inject constructor(
    private val repo: PricePostRepository
) : ViewModel() {

    // UI state for the "add pin" dialog
    data class DialogUi(
        val showAddDialog: Boolean = false,
        val lat: Double? = null,
        val lng: Double? = null
    )

    private val _dialogUi = MutableStateFlow(DialogUi())
    val dialogUi: StateFlow<DialogUi> = _dialogUi.asStateFlow()

    // Posts coming from Room via the repository
    val pins: StateFlow<List<PricePost>> =
        repo.observePosts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onMapLongClick(lat: Double, lng: Double) {
        _dialogUi.value = DialogUi(
            showAddDialog = true,
            lat = lat,
            lng = lng
        )
    }

    fun onDismissDialog() {
        _dialogUi.value = DialogUi()
    }

    fun onAddPin(
        storeName: String,
        itemName: String,
        price: Double,
        photoUri: String?
    ) {
        val lat = _dialogUi.value.lat ?: return
        val lng = _dialogUi.value.lng ?: return

        // NOTE: id = 0L → Room auto-generates the primary key
        val post = PricePost(
            id = 0L,
            storeName = storeName.trim(),
            itemName = itemName.trim(),
            price = price,
            lat = lat,
            lng = lng,
            photoUri = photoUri,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repo.add(post)
            _dialogUi.value = DialogUi()   // close dialog + clear temp coords
        }
    }

    fun clearAllPosts() {
        viewModelScope.launch {
            repo.clear()
        }
    }
}
