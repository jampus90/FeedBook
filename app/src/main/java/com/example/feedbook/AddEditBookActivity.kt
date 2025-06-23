package com.example.feedbook

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.feedbook.data.Book
import com.example.feedbook.viewmodel.BookViewModel
import com.example.feedbook.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import android.content.Intent
import com.example.booklibrary.R

class AddEditBookActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModel.BookViewModelFactory(application)
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.AuthViewModelFactory(application)
    }

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var cbIsRead: CheckBox
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private var bookId: Int = -1
    private var currentBook: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check authentication
        if (!authViewModel.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_add_edit_book)

        bookId = intent.getIntExtra("BOOK_ID", -1)

        setupViews()
        setupClickListeners()

        if (bookId != -1) {
            loadBook()
            btnDelete.visibility = Button.VISIBLE
            title = "Editar livro"
        } else {
            title = "Adicionar livro"
        }
    }

    private fun setupViews() {
        etTitle = findViewById(R.id.et_title)
        etAuthor = findViewById(R.id.et_author)
        etDescription = findViewById(R.id.et_description)
        ratingBar = findViewById(R.id.rating_bar)
        cbIsRead = findViewById(R.id.cb_is_read)
        btnSave = findViewById(R.id.btn_save)
        btnDelete = findViewById(R.id.btn_delete)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveBook()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadBook() {
        lifecycleScope.launch {
            currentBook = bookViewModel.getBookById(bookId)
            currentBook?.let { book ->
                etTitle.setText(book.title)
                etAuthor.setText(book.author)
                etDescription.setText(book.description)
                ratingBar.rating = book.rating.toFloat()
                cbIsRead.isChecked = book.isRead
            }
        }
    }

    private fun saveBook() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val rating = ratingBar.rating.toInt()
        val isRead = cbIsRead.isChecked

        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "Por favor insira o título e autor do livro", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (bookId == -1) {
                // Add new book - userId will be set automatically in BookViewModel.insertBook()
                val newBook = Book(
                    title = title,
                    author = author,
                    description = description,
                    rating = rating,
                    isRead = isRead,
                    userId = authViewModel.getCurrentUserId() // Set current user's ID
                )
                bookViewModel.insertBook(newBook)
                Toast.makeText(this@AddEditBookActivity, "Livro adicionado com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                // Update existing book - preserve the original userId
                currentBook?.let { book ->
                    val updatedBook = book.copy(
                        title = title,
                        author = author,
                        description = description,
                        rating = rating,
                        isRead = isRead
                        // userId remains the same
                    )
                    bookViewModel.updateBook(updatedBook)
                    Toast.makeText(this@AddEditBookActivity, "Livro atualizado com sucesso", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Deletar livro")
            .setMessage("Tem certeza que deseja deletar este livro?")
            .setPositiveButton("Deletar") { _, _ ->
                deleteBook()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteBook() {
        currentBook?.let { book ->
            lifecycleScope.launch {
                bookViewModel.deleteBook(book)
                Toast.makeText(this@AddEditBookActivity, "Livro deletado com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}