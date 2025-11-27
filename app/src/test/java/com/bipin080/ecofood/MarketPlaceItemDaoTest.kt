package com.bipin080.ecofood

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bipin080.ecofood.data.MarketplaceDatabase
import com.bipin080.ecofood.data.MarketplaceItem
import com.bipin080.ecofood.data.MarketplaceItemDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [34], application = TestApp::class, manifest = Config.NONE)
class MarketplaceItemDaoTest {

    private lateinit var db: MarketplaceDatabase
    private lateinit var dao: MarketplaceItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        db = Room.inMemoryDatabaseBuilder(context, MarketplaceDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.marketplaceItemDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun testItem(
        name: String,
        quantity: Int,
        description: String,
        price: Double,
        sellerUid: String,
        sellerName: String,
        postedAt: Date
    ): MarketplaceItem {
        return MarketplaceItem(
            id = UUID.randomUUID().toString(),
            name = name,
            quantity = quantity,
            unit = "kg",
            price = price,
            expiryDate = "2026-01-01",
            location = "Brisbane",
            description = description,
            imageUrl = null,
            sellerUid = sellerUid,
            sellerName = sellerName,
            postedAt = postedAt
        )
    }

    @Test
    fun insert_insertsItemCorrectly() = runTest {
        val item = testItem("Tomato", 2, "Fresh", 12.0, "user123", "Bipin", Date(999))

        dao.insert(item)

        val result = dao.getAll().first()
        assertEquals(1, result.size)
        assertEquals("Tomato", result[0].name)
    }

    @Test
    fun getMyListings_returnsOnlyUserItems() = runTest {
        val item1 = testItem("Tomato", 2, "Fresh", 10.0, "user123", "Bipin", Date(111))
        val item2 = testItem("Onion", 1, "Red onion", 8.0, "user999", "Alex", Date(222))

        dao.insert(item1)
        dao.insert(item2)

        val userItems = dao.getMyListings("user123").first()

        assertEquals(1, userItems.size)
        assertEquals("Tomato", userItems[0].name)
    }

    @Test
    fun deleteItem_removesOnlyThatItem() = runTest {
        val item1 = testItem("Milk", 1, "Fresh", 4.5, "user123", "Bipin", Date(321))
        val item2 = testItem("Bread", 1, "Wholemeal", 3.0, "user123", "Bipin", Date(654))

        dao.insert(item1)
        dao.insert(item2)

        dao.delete(item1)

        val result = dao.getAll().first()

        assertEquals(1, result.size)
        assertEquals("Bread", result[0].name)
    }

    @Test
    fun deleteAll_clearsTheTable() = runTest {
        val item = testItem("Butter", 1, "Salted", 5.0, "user123", "Bipin", Date(1111))

        dao.insert(item)

        dao.deleteAll()

        val result = dao.getAll().first()
        assertEquals(0, result.size)
    }
}
