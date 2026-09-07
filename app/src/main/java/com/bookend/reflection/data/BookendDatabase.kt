package com.bookend.reflection.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ReflectionEntry::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class BookendDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var instance: BookendDatabase? = null

        fun get(context: Context): BookendDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                BookendDatabase::class.java,
                "bookend.db",
            ).build().also { instance = it }
        }
    }
}
