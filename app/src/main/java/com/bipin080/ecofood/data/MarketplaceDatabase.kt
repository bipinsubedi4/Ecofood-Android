package com.bipin080.ecofood.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.util.UUID

@Database(entities = [MarketplaceItem::class], version = 1)
@TypeConverters(Converters::class)
abstract class MarketplaceDatabase : RoomDatabase() {

    abstract fun marketplaceItemDao(): MarketplaceItemDao

    companion object {
        @Volatile
        private var INSTANCE: MarketplaceDatabase? = null

        fun getDatabase(context: Context): MarketplaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarketplaceDatabase::class.java,
                    "marketplace_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @androidx.room.TypeConverter
    fun toUUID(uuid: String): UUID {
        return UUID.fromString(uuid)
    }
}
