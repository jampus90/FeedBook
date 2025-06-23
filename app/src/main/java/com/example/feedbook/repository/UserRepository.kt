package com.example.feedbook.repository

import com.example.feedbook.data.User
import com.example.feedbook.data.UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)

    suspend fun insertUser(user: User): Long = userDao.insertUser(user)

    suspend fun isUsernameExists(username: String): Boolean = userDao.isUsernameExists(username) > 0
}
