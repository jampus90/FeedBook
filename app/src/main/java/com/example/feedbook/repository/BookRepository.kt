package com.example.feedbook.repository

import com.example.feedbook.data.Book
import com.example.feedbook.data.BookDao
import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {

    fun getAllBooksAlphabetically(userId: Int): Flow<List<Book>> = bookDao.getAllBooksAlphabetically(userId)

    fun getAllBooksByRating(userId: Int): Flow<List<Book>> = bookDao.getAllBooksByRating(userId)

    fun getBooksByReadStatus(userId: Int, isRead: Boolean): Flow<List<Book>> = bookDao.getBooksByReadStatus(userId, isRead)

    suspend fun getBookById(id: Int, userId: Int): Book? = bookDao.getBookById(id, userId)

    suspend fun insertBook(book: Book) = bookDao.insertBook(book)

    suspend fun updateBook(book: Book) = bookDao.updateBook(book)

    suspend fun deleteBook(book: Book) = bookDao.deleteBook(book)
}