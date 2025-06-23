package com.example.feedbook.data

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameExists(username: String): Int
}