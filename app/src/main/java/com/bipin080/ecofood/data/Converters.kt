package com.bipin080.ecofood.data


import androidx.room.TypeConverter
import java.util.*

class Converters {

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(timestamp: Long?): Date? =
        timestamp?.let { Date(it) }
}
