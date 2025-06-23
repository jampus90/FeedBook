package com.example.feedbook.repository

import com.example.feedbook.data.Book
import com.example.feedbook.data.BookDao
import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {

    fun getAllBooksAlphabetically(): Flow<List<Book>> = bookDao.getAllBooksAlphabetically()

    fun getAllBooksByRating(): Flow<List<Book>> = bookDao.getAllBooksByRating()

    fun getBooksByReadStatus(isRead: Boolean): Flow<List<Book>> = bookDao.getBooksByReadStatus(isRead)

    suspend fun getBookById(id: Int): Book? = bookDao.getBookById(id)

    suspend fun insertBook(book: Book) = bookDao.insertBook(book)

    suspend fun updateBook(book: Book) = bookDao.updateBook(book)

    suspend fun deleteBook(book: Book) = bookDao.deleteBook(book)
}