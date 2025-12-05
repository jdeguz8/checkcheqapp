package com.jdeguzman.checkcheqapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdeguzman.checkcheqapp.data.remote.yelp.YelpBusiness
import com.jdeguzman.checkcheqapp.data.repository.YelpRepository
import com.jdeguzman.checkcheqapp.domain.PricePost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel wrapper around [YelpRepository] that exposes Yelp lookup state for a post.
 *
 * It drives UI for showing Yelp information alongside a [PricePost] by exposing:
 * - [YelpUiState.isLoading] while the network request is in flight
 * - [YelpUiState.business] when a matching business is found
 * - [YelpUiState.error] if the lookup fails or returns no usable result
 *
 * Typical usage:
 * - Call [loadForPost] once when the details screen is opened for a restaurant-like post.
 * - Observe [uiState] from the composable to render loading / error / success states.
 */

data class YelpUiState(
    val isLoading: Boolean = false,
    val business: YelpBusiness? = null,
    val error: String? = null
)

@HiltViewModel
class YelpViewModel @Inject constructor(
    private val yelpRepository: YelpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(YelpUiState())
    val uiState: StateFlow<YelpUiState> = _uiState

    /**
     * Load Yelp info for this post and update [uiState].
     */
    fun loadForPost(post: PricePost) {
        viewModelScope.launch {
            _uiState.value = YelpUiState(isLoading = true)

            try {
                val biz = yelpRepository.findBestMatchForPost(post)
                _uiState.value = YelpUiState(
                    isLoading = false,
                    business = biz,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = YelpUiState(
                    isLoading = false,
                    business = null,
                    error = e.localizedMessage ?: "Yelp lookup failed"
                )
            }
        }
    }
}
