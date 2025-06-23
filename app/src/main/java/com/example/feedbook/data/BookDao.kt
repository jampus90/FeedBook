package com.example.feedbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooksAlphabetically(): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY rating DESC")
    fun getAllBooksByRating(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isRead = :isRead ORDER BY title ASC")
    fun getBooksByReadStatus(isRead: Boolean): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Int): Book?

    @Insert
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)
}