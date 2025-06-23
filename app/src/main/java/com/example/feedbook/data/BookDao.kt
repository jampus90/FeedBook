package com.example.feedbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY title ASC")
    fun getAllBooksAlphabetically(userId: Int): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY rating DESC")
    fun getAllBooksByRating(userId: Int): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE userId = :userId AND isRead = :isRead ORDER BY title ASC")
    fun getBooksByReadStatus(userId: Int, isRead: Boolean): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id AND userId = :userId")
    suspend fun getBookById(id: Int, userId: Int): Book?

    @Insert
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)
}