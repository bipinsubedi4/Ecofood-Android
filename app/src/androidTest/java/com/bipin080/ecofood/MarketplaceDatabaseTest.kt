package com.bipin080.ecofood

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bipin080.ecofood.data.MarketplaceDatabase
import com.bipin080.ecofood.data.MarketplaceItem
import com.bipin080.ecofood.data.MarketplaceItemDao
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class MarketplaceDatabaseTest {

    private lateinit var db: MarketplaceDatabase
    private lateinit var dao: MarketplaceItemDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MarketplaceDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = db.marketplaceItemDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_and_get_all_items() = runTest {
        val item = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Banana",
            quantity = 5,
            unit = "pcs",
            price = 3.0,
            expiryDate = "12 Dec 2025",
            location = "Brisbane",
            description = "Fresh bananas",
            imageUrl = null,
            sellerUid = "user123",
            sellerName = "John",
            postedAt = Date()
        )

        dao.insert(item)

        dao.getAll().test {
            val list = awaitItem()
            assertThat(list.size).isEqualTo(1)
            assertThat(list[0].name).isEqualTo("Banana")
        }
    }

    @Test
    fun get_my_listings_filters_correctly() = runTest {
        val myItem = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Apples",
            quantity = 3,
            unit = "kg",
            price = 5.0,
            expiryDate = "10 Jan 2026",
            location = "Sydney",
            description = "Sweet apples",
            imageUrl = null,
            sellerUid = "local_user",
            sellerName = "Local User",
            postedAt = Date()
        )

        val otherItem = myItem.copy(
            id = UUID.randomUUID(),
            sellerUid = "someone_else",
            sellerName = "Other"
        )

        dao.insert(myItem)
        dao.insert(otherItem)

        dao.getMyListings("local_user").test {
            val list = awaitItem()
            assertThat(list.size).isEqualTo(1)
            assertThat(list[0].sellerUid).isEqualTo("local_user")
        }
    }

    @Test
    fun delete_item_removes_correct_entry() = runTest {
        val item = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Bread",
            quantity = 1,
            unit = "loaf",
            price = 2.5,
            expiryDate = "1 Jan 2026",
            location = "Melbourne",
            description = "Whole grain bread",
            imageUrl = null,
            sellerUid = "local_user",
            sellerName = "Local User",
            postedAt = Date()
        )

        dao.insert(item)

        // retrieve inserted version (Room stores exactly this)
        val itemFromDb = dao.getAll().first().first()

        dao.delete(itemFromDb)

        dao.getAll().test {
            val list = awaitItem()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun delete_all_clears_table() = runTest {
        val item1 = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Oranges",
            quantity = 4,
            unit = "kg",
            price = 6.0,
            expiryDate = "15 Jan 2026",
            location = "Brisbane",
            description = "Citrus fruits",
            imageUrl = null,
            sellerUid = "local_user",
            sellerName = "Local User",
            postedAt = Date()
        )

        val item2 = item1.copy(
            id = UUID.randomUUID(),
            name = "Grapes"
        )

        dao.insert(item1)
        dao.insert(item2)

        dao.deleteAll()

        dao.getAll().test {
            val list = awaitItem()
            assertThat(list).isEmpty()
        }
    }
}
