package com.bookend.reflection.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM entries ORDER BY dateEpochDay DESC, part ASC")
    fun observeAll(): Flow<List<ReflectionEntry>>

    @Query("SELECT * FROM entries WHERE dateEpochDay = :dateEpochDay")
    fun observeDay(dateEpochDay: Long): Flow<List<ReflectionEntry>>

    @Query("SELECT * FROM entries WHERE dateEpochDay = :dateEpochDay AND part = :part LIMIT 1")
    suspend fun find(dateEpochDay: Long, part: DayPart): ReflectionEntry?

    @Upsert
    suspend fun upsert(entry: ReflectionEntry)

    @Delete
    suspend fun delete(entry: ReflectionEntry)
}
