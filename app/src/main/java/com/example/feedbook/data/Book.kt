package com.example.feedbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val author: String,
    val description: String,
    val rating: Int, // 1-5 stars
    val isRead: Boolean = false,
    val userId: Int // Link to the user who added this book
)