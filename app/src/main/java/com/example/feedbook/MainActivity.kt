package com.example.feedbook

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.feedbook.adapter.BookAdapter
import com.example.feedbook.data.Book
import com.example.feedbook.viewmodel.AuthViewModel
import com.example.feedbook.viewmodel.BookViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import com.example.booklibrary.R

class MainActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModel.BookViewModelFactory(application)
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.AuthViewModelFactory(application)
    }

    private lateinit var bookAdapter: BookAdapter
    private lateinit var filterSpinner: MaterialAutoCompleteTextView
    private lateinit var sortSpinner: MaterialAutoCompleteTextView
    private lateinit var tvWelcome: TextView
    private lateinit var tvBookCount: TextView
    private lateinit var rvBooks: RecyclerView
    private lateinit var layoutEmptyState: View
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check authentication
        if (!authViewModel.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        setupToolbar()
        setupViews()
        setupSpinners()
        setupRecyclerView()
        observeBooks()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set welcome message
        tvWelcome = findViewById(R.id.tv_welcome)
        tvBookCount = findViewById(R.id.tv_book_count)

        val currentUser = authViewModel.getCurrentUser()
        tvWelcome.text = "Welcome back, $currentUser!"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViews() {
        rvBooks = findViewById(R.id.rv_books)
        layoutEmptyState = findViewById(R.id.layout_empty_state)

        val fabAddBook: ExtendedFloatingActionButton = findViewById(R.id.fab_add_book)
        val btnAddFirstBook: MaterialButton = findViewById(R.id.btn_add_first_book)

        fabAddBook.setOnClickListener {
            startActivity(Intent(this, AddEditBookActivity::class.java))
        }

        btnAddFirstBook.setOnClickListener {
            startActivity(Intent(this, AddEditBookActivity::class.java))
        }
    }

    private fun setupSpinners() {
        filterSpinner = findViewById(R.id.spinner_filter)
        sortSpinner = findViewById(R.id.spinner_sort)

        // Filter spinner
        val filterOptions = arrayOf("All Books", "Read", "Unread")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, filterOptions)
        filterSpinner.setAdapter(filterAdapter)
        filterSpinner.setText(filterOptions[0], false)

        // Sort spinner
        val sortOptions = arrayOf("Alphabetical", "By Rating")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sortOptions)
        sortSpinner.setAdapter(sortAdapter)
        sortSpinner.setText(sortOptions[0], false)

        // Set listeners
        filterSpinner.setOnItemClickListener { _, _, _, _ ->
            updateBookList()
        }

        sortSpinner.setOnItemClickListener { _, _, _, _ ->
            updateBookList()
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            onBookClick = { book ->
                val intent = Intent(this, AddEditBookActivity::class.java)
                intent.putExtra("BOOK_ID", book.id)
                startActivity(intent)
            },
            onMenuClick = { book ->
                showBookMenu(book)
            }
        )

        rvBooks.adapter = bookAdapter
    }

    private fun showBookMenu(book: Book) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle(book.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Edit
                        val intent = Intent(this, AddEditBookActivity::class.java)
                        intent.putExtra("BOOK_ID", book.id)
                        startActivity(intent)
                    }
                    1 -> {
                        // Delete
                        showDeleteConfirmation(book)
                    }
                }
            }
            .show()
    }

    private fun showDeleteConfirmation(book: Book) {
        AlertDialog.Builder(this)
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete \"${book.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                bookViewModel.deleteBook(book)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeBooks() {
        updateBookList()
    }

    private fun updateBookList() {
        val filterText = filterSpinner.text.toString()
        val sortText = sortSpinner.text.toString()

        val filterPosition = when (filterText) {
            "Read" -> 1
            "Unread" -> 2
            else -> 0
        }

        val sortPosition = when (sortText) {
            "By Rating" -> 1
            else -> 0
        }

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
                updateBookCount(sortedBooks.size)
                updateEmptyState(sortedBooks.isEmpty())
            }
        }
    }

    private fun updateBookCount(count: Int) {
        tvBookCount.text = when (count) {
            0 -> "No books in your library yet"
            1 -> "You have 1 book in your library"
            else -> "You have $count books in your library"
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            rvBooks.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvBooks.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        authViewModel.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Check authentication on resume
        if (!authViewModel.isLoggedIn()) {
            navigateToLogin()
            return
        }
        updateBookList()
    }
}
