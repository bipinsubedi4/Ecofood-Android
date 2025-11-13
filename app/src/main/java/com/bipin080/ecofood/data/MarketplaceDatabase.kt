package com.bipin080.ecofood.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [MarketplaceItem::class], version = 1)
@TypeConverters(Converters::class)
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
                    fakeMarketplaceItems().forEach { 
                        dao.insert(it)
                    }
                }
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
