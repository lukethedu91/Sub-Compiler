package com.bookend.reflection.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class EntryRepository(private val dao: EntryDao) {

    fun observeAll(): Flow<List<ReflectionEntry>> = dao.observeAll()

    suspend fun find(date: LocalDate, part: DayPart): ReflectionEntry? =
        dao.find(date.toEpochDay(), part)

    /** Saves the entry, or removes it once every answer has been cleared. */
    suspend fun save(entry: ReflectionEntry) {
        if (entry.isBlank) {
            dao.delete(entry)
        } else {
            dao.upsert(entry.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun delete(entry: ReflectionEntry) = dao.delete(entry)
}
