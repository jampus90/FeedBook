package com.example.feedbook

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.feedbook.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import com.example.booklibrary.R

class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.AuthViewModelFactory(application)
    }

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (authViewModel.isLoggedIn()) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        tilPassword = findViewById(R.id.til_password)
        btnLogin = findViewById(R.id.btn_login)
        tvRegister = findViewById(R.id.tv_register)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        // Clear previous errors
        tilPassword.error = null

        if (!validateInput(username, password)) {
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            when (val result = authViewModel.login(username, password)) {
                is AuthViewModel.LoginResult.Success -> {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Login realizado!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthViewModel.LoginResult.InvalidCredentials -> {
                    showLoading(false)
                    tilPassword.error = "Usuário ou senha inválida"
                }
                is AuthViewModel.LoginResult.Error -> {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Falha no login: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            etUsername.error = "Insira o usuário"
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = "Insira a senha"
            isValid = false
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        etUsername.isEnabled = !show
        etPassword.isEnabled = !show
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}