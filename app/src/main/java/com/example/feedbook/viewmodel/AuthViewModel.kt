package com.example.feedbook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.feedbook.auth.AuthManager
import com.example.feedbook.data.BookDatabase
import com.example.feedbook.data.User
import com.example.feedbook.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository
    private val authManager: AuthManager = AuthManager(application)

    init {
        val userDao = BookDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    suspend fun login(username: String, password: String): LoginResult {
        return try {
            val user = userRepository.getUserByUsername(username)
            if (user != null && authManager.verifyPassword(password, user.passwordHash)) {
                authManager.login(username, user.id)
                LoginResult.Success
            } else {
                LoginResult.InvalidCredentials
            }
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Login falhou")
        }
    }

    suspend fun register(username: String, password: String): RegisterResult {
        return try {
            if (userRepository.isUsernameExists(username)) {
                RegisterResult.UsernameExists
            } else {
                val hashedPassword = authManager.hashPassword(password)
                val user = User(username = username, passwordHash = hashedPassword)
                userRepository.insertUser(user)
                RegisterResult.Success
            }
        } catch (e: Exception) {
            RegisterResult.Error(e.message ?: "Cadastro falhou")
        }
    }

    fun logout() {
        authManager.logout()
    }

    fun isLoggedIn(): Boolean = authManager.isLoggedIn()

    fun getCurrentUser(): String? = authManager.getCurrentUser()

    fun getCurrentUserId(): Int = authManager.getCurrentUserId()

    sealed class LoginResult {
        object Success : LoginResult()
        object InvalidCredentials : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    sealed class RegisterResult {
        object Success : RegisterResult()
        object UsernameExists : RegisterResult()
        data class Error(val message: String) : RegisterResult()
    }

    class AuthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}