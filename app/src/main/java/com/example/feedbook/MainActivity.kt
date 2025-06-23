package com.example.feedbook

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.feedbook.adapter.BookAdapter
import com.example.feedbook.viewmodel.BookViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import com.example.booklibrary.R

class MainActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModel.BookViewModelFactory(application)
    }

    private lateinit var bookAdapter: BookAdapter
    private lateinit var filterSpinner: Spinner
    private lateinit var sortSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupSpinners()
        setupRecyclerView()
        observeBooks()
    }

    private fun setupViews() {
        val fabAddBook: FloatingActionButton = findViewById(R.id.fab_add_book)
        fabAddBook.setOnClickListener {
            startActivity(Intent(this, AddEditBookActivity::class.java))
        }
    }

    private fun setupSpinners() {
        filterSpinner = findViewById(R.id.spinner_filter)
        sortSpinner = findViewById(R.id.spinner_sort)

        // Filter spinner
        val filterOptions = arrayOf("All Books", "Read", "Unread")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = filterAdapter

        // Sort spinner
        val sortOptions = arrayOf("Alphabetical", "By Rating")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        // Set listeners
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateBookList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateBookList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter { book ->
            val intent = Intent(this, AddEditBookActivity::class.java)
            intent.putExtra("BOOK_ID", book.id)
            startActivity(intent)
        }

        val recyclerView: RecyclerView = findViewById(R.id.rv_books)
        recyclerView.adapter = bookAdapter
    }

    private fun observeBooks() {
        updateBookList()
    }

    private fun updateBookList() {
        val filterPosition = filterSpinner.selectedItemPosition
        val sortPosition = sortSpinner.selectedItemPosition

        lifecycleScope.launch {
            val flow = when (filterPosition) {
                1 -> bookViewModel.getBooksByReadStatus(true) // Read
                2 -> bookViewModel.getBooksByReadStatus(false) // Unread
                else -> { // All books
                    if (sortPosition == 1) {
                        bookViewModel.getAllBooksByRating()
                    } else {
                        bookViewModel.getAllBooksAlphabetically()
                    }
                }
            }

            flow.collect { books ->
                val sortedBooks = if (filterPosition != 0 && sortPosition == 1) {
                    books.sortedByDescending { it.rating }
                } else if (filterPosition != 0 && sortPosition == 0) {
                    books.sortedBy { it.title }
                } else {
                    books
                }
                bookAdapter.submitList(sortedBooks)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateBookList()
    }
}