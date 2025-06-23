package com.example.feedbook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.feedbook.data.Book
import com.example.feedbook.data.BookDatabase
import com.example.feedbook.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookRepository

    init {
        val bookDao = BookDatabase.getDatabase(application).bookDao()
        repository = BookRepository(bookDao)
    }

    fun getAllBooksAlphabetically(): Flow<List<Book>> = repository.getAllBooksAlphabetically()

    fun getAllBooksByRating(): Flow<List<Book>> = repository.getAllBooksByRating()

    fun getBooksByReadStatus(isRead: Boolean): Flow<List<Book>> = repository.getBooksByReadStatus(isRead)

    suspend fun getBookById(id: Int): Book? = repository.getBookById(id)

    fun insertBook(book: Book) = viewModelScope.launch {
        repository.insertBook(book)
    }

    fun updateBook(book: Book) = viewModelScope.launch {
        repository.updateBook(book)
    }

    fun deleteBook(book: Book) = viewModelScope.launch {
        repository.deleteBook(book)
    }

    class BookViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BookViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}