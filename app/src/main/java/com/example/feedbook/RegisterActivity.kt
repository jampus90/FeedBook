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

class RegisterActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.AuthViewModelFactory(application)
    }

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        tilUsername = findViewById(R.id.til_username)
        tilPassword = findViewById(R.id.til_password)
        tilConfirmPassword = findViewById(R.id.til_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        tvLogin = findViewById(R.id.tv_login)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            performRegistration()
        }

        tvLogin.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun performRegistration() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Clear previous errors
        clearErrors()

        if (!validateInput(username, password, confirmPassword)) {
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            when (val result = authViewModel.register(username, password)) {
                is AuthViewModel.RegisterResult.Success -> {
                    showLoading(false)
                    Toast.makeText(this@RegisterActivity, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Go back to login
                }
                is AuthViewModel.RegisterResult.UsernameExists -> {
                    showLoading(false)
                    tilUsername.error = "Usuário já cadastrado"
                }
                is AuthViewModel.RegisterResult.Error -> {
                    showLoading(false)
                    Toast.makeText(this@RegisterActivity, "Falha no cadastro: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(username: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            tilUsername.error = "Insira o usuário"
            isValid = false
        } else if (username.length < 3) {
            tilUsername.error = "Nome de usuário deve conter no mínimo 3 letras"
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = "Insira a senha"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Sua senha deve ter no mínimo 6 caracteres"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "Por favor confirme sua senha"
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPassword.error = "Senhas não conferem"
            isValid = false
        }

        return isValid
    }

    private fun clearErrors() {
        tilUsername.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !show
        etUsername.isEnabled = !show
        etPassword.isEnabled = !show
        etConfirmPassword.isEnabled = !show
    }
}