package com.jdeguzman.checkcheqapp.ui

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
 * UI wrapper around [YelpRepository] for a single PricePost.
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
