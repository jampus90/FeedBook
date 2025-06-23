package com.example.feedbook.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(
    entities = [Book::class, User::class],
    version = 3,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `users` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `username` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add userId column to books table
                database.execSQL("ALTER TABLE books ADD COLUMN userId INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): BookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "book_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

