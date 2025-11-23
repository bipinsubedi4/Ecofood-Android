package com.bipin080.ecofood

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import app.cash.turbine.test
import com.bipin080.ecofood.data.PantryDatabase
import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.data.PantryItemDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.UUID
import com.google.common.truth.Truth.assertThat

@RunWith(AndroidJUnit4::class)
class PantryDatabaseTest {

    private lateinit var db: PantryDatabase
    private lateinit var dao: PantryItemDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PantryDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = db.pantryItemDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_and_read_item() = runTest {
        val item = PantryItem(
            id = UUID.randomUUID(),
            name = "Rice",
            quantity = 2,
            unit = "kg",
            purchaseDate = Date(),
            expiryDate = Date(System.currentTimeMillis() + 3 * 86400000L) // +3 days
        )

        dao.insert(item)

        dao.getAll().test {
            val list = awaitItem()
            assertThat(list.size).isEqualTo(1)
            assertThat(list[0].name).isEqualTo("Rice")
        }
    }

    @Test
    fun delete_item() = runTest {
        val item = PantryItem(
            id = UUID.randomUUID(),
            name = "Milk",
            quantity = 1,
            unit = "L",
            purchaseDate = Date(),
            expiryDate = Date(System.currentTimeMillis() + 86400000L) // +1 day
        )

        dao.insert(item)
        dao.delete(item)

        dao.getAll().test {
            val list = awaitItem()
            assertThat(list).isEmpty()
        }
    }
}
