package com.example.feedbook

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.feedbook.adapter.BookAdapter
import com.example.feedbook.viewmodel.AuthViewModel
import com.example.feedbook.viewmodel.BookViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var filterSpinner: Spinner
    private lateinit var sortSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check authentication
        if (!authViewModel.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        // Set up toolbar
        supportActionBar?.title = "Meu FeedBook"
        supportActionBar?.subtitle = "Bem vindo, ${authViewModel.getCurrentUser()}"

        setupViews()
        setupSpinners()
        setupRecyclerView()
        observeBooks()
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
        val fabAddBook: FloatingActionButton = findViewById(R.id.fab_add_book)
        fabAddBook.setOnClickListener {
            startActivity(Intent(this, AddEditBookActivity::class.java))
        }
    }

    private fun setupSpinners() {
        filterSpinner = findViewById(R.id.spinner_filter)
        sortSpinner = findViewById(R.id.spinner_sort)

        // Filter spinner
        val filterOptions = arrayOf("Todos", "Lidos", "Não lidos")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = filterAdapter

        // Sort spinner
        val sortOptions = arrayOf("Ordem alfabética", "Por nota")
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

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Tem certeza que quer sair?")
            .setPositiveButton("Sair") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
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