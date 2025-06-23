package com.example.feedbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.booklibrary.R
import com.example.feedbook.data.Book

class BookAdapter(
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.tv_author)
        private val ratingTextView: TextView = itemView.findViewById(R.id.tv_rating)
        private val statusTextView: TextView = itemView.findViewById(R.id.tv_status)

        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = "by ${book.author}"
            ratingTextView.text = "★".repeat(book.rating) + "☆".repeat(5 - book.rating)
            statusTextView.text = if (book.isRead) "Read" else "Unread"
            statusTextView.setTextColor(
                if (book.isRead)
                    itemView.context.getColor(android.R.color.holo_green_dark)
                else
                    itemView.context.getColor(android.R.color.holo_orange_dark)
            )

            itemView.setOnClickListener { onBookClick(book) }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}