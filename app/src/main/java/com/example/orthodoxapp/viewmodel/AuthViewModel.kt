package com.example.orthodoxapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orthodoxapp.repository.AuthRepository
import com.example.orthodoxapp.data.network.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<Result<Any>?>(null)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, passwordRaw: String) {
        viewModelScope.launch {
            val result = repository.login(email, passwordRaw)
            _loginState.value = result
        }
    }
}
