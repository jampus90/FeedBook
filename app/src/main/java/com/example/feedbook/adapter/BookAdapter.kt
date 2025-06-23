package com.example.feedbook.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.booklibrary.R
import com.example.feedbook.data.Book
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class BookAdapter(
    private val onBookClick: (Book) -> Unit,
    private val onMenuClick: (Book) -> Unit = {}
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
        private val statusChip: Chip = itemView.findViewById(R.id.chip_status)
        private val menuButton: MaterialButton = itemView.findViewById(R.id.btn_menu)

        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = "por ${book.author}"

            // Set rating stars
            ratingTextView.text = "★".repeat(book.rating) + "☆".repeat(5 - book.rating)

            // Configure status chip
            setupStatusChip(book.isRead)

            // Set click listeners
            itemView.setOnClickListener { onBookClick(book) }
            menuButton.setOnClickListener { onMenuClick(book) }
        }

        private fun setupStatusChip(isRead: Boolean) {
            val context = itemView.context

            if (isRead) {
                statusChip.text = "Lido"
                statusChip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.success_green)
                )
                statusChip.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                statusChip.setChipIconResource(R.drawable.ic_check_circle)
                statusChip.chipIconTint = ColorStateList.valueOf(
                    ContextCompat.getColor(context, android.R.color.white)
                )
            } else {
                statusChip.text = "Não lido"
                statusChip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.warning_orange)
                )
                statusChip.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                statusChip.setChipIconResource(R.drawable.ic_schedule)
                statusChip.chipIconTint = ColorStateList.valueOf(
                    ContextCompat.getColor(context, android.R.color.white)
                )
            }
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
