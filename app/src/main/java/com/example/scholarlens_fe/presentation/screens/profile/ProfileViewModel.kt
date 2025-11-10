package com.example.scholarlens_fe.presentation.screens.profile

import com.example.scholarlens_fe.domain.model.User
import com.example.scholarlens_fe.domain.usecase.GetCurrentUserUseCase
import com.example.scholarlens_fe.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val user = getCurrentUserUseCase()
        _uiState.value = _uiState.value.copy(user = user)
    }

    fun signOut() {
        signOutUseCase()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }

    data class ProfileUiState(
        val user: User? = null,
        val isLoggedOut: Boolean = false
    )
}
