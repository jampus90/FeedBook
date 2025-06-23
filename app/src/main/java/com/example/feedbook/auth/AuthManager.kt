package com.example.feedbook.auth

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import android.util.Base64

class AuthManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_SESSION_TOKEN = "session_token"
    }

    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded

        val saltBase64 = Base64.encodeToString(salt, Base64.DEFAULT)
        val hashBase64 = Base64.encodeToString(hash, Base64.DEFAULT)

        return "$saltBase64:$hashBase64"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val salt = Base64.decode(parts[0], Base64.DEFAULT)
            val storedHashBytes = Base64.decode(parts[1], Base64.DEFAULT)

            val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val testHash = factory.generateSecret(spec).encoded

            MessageDigest.isEqual(storedHashBytes, testHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun login(username: String, userId: Int) {
        val sessionToken = generateSessionToken()
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_CURRENT_USER, username)
            .putInt(KEY_CURRENT_USER_ID, userId)
            .putString(KEY_SESSION_TOKEN, sessionToken)
            .apply()
    }

    fun logout() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_CURRENT_USER)
            .remove(KEY_CURRENT_USER_ID)
            .remove(KEY_SESSION_TOKEN)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUser(): String? {
        return sharedPreferences.getString(KEY_CURRENT_USER, null)
    }

    fun getCurrentUserId(): Int {
        return sharedPreferences.getInt(KEY_CURRENT_USER_ID, -1)
    }

    private fun generateSessionToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}