package com.bookend.reflection.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromDayPart(part: DayPart): String = part.name

    @TypeConverter
    fun toDayPart(value: String): DayPart = DayPart.valueOf(value)
}
