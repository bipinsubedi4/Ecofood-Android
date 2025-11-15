package com.bipin080.ecofood.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

@Database(entities = [MarketplaceItem::class], version = 3) // Incremented version
@TypeConverters(MarketplaceConverters::class)
abstract class MarketplaceDatabase : RoomDatabase() {

    abstract fun marketplaceItemDao(): MarketplaceItemDao

    companion object {
        @Volatile
        private var INSTANCE: MarketplaceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MarketplaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarketplaceDatabase::class.java,
                    "marketplace_database"
                )
                .fallbackToDestructiveMigration() // Handles schema changes
                .addCallback(MarketplaceDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class MarketplaceDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val dao = database.marketplaceItemDao()
                    // Clear old data and pre-populate with new data
                    dao.deleteAll()
                    fakeMarketplaceItems().forEach {
                        dao.insert(it)
                    }
                }
            }
        }
    }
}

class MarketplaceConverters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @androidx.room.TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @androidx.room.TypeConverter
    fun toUUID(uuid: String): UUID {
        return UUID.fromString(uuid)
    }
}
