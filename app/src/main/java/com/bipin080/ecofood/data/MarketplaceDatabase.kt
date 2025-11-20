package com.bipin080.ecofood.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MarketplaceItem::class],
    version = 5,  // <-- bump version to avoid migration crash
    exportSchema = false
)
@TypeConverters(MarketplaceConverters::class)
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
                )
                    .fallbackToDestructiveMigration()  // always safe during development
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
