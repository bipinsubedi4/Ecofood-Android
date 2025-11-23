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
import org.robolectric.RuntimeEnvironment
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

    @Test
    fun insert_insertsItemCorrectly() = runTest {
        val item = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Tomato",
            quantity = 2,
            description = "Fresh",
            price = 12.0,
            sellerUid = "user123",
            sellerName = "Bipin",
            postedAt = Date(999)
        )

        dao.insert(item)

        val result = dao.getAll().first()

        assertEquals(1, result.size)
        assertEquals("Tomato", result[0].name)
    }

    @Test
    fun getMyListings_returnsOnlyUserItems() = runTest {
        val item1 = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Tomato",
            quantity = 2,
            description = "Fresh",

            price = 10.0,

            sellerUid = "user123",
            sellerName = "Bipin",
            postedAt = Date(111)
        )

        val item2 = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Onion",
            quantity = 1,
            description = "Red onion",

            price = 8.0,

            sellerUid = "user999",
            sellerName = "Alex",
            postedAt = Date(222)
        )

        dao.insert(item1)
        dao.insert(item2)

        val userItems = dao.getMyListings("user123").first()

        assertEquals(1, userItems.size)
        assertEquals("Tomato", userItems[0].name)
    }

    @Test
    fun deleteItem_removesOnlyThatItem() = runTest {
        val item1 = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Milk",
            quantity = 1,
            description = "Fresh",

            price = 4.5,

            sellerUid = "user123",
            sellerName = "Bipin",
            postedAt = Date(321)
        )

        val item2 = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Bread",
            quantity = 1,
            description = "Wholemeal",

            price = 3.0,

            sellerUid = "user123",
            sellerName = "Bipin",
            postedAt = Date(654)
        )

        dao.insert(item1)
        dao.insert(item2)

        dao.delete(item1)

        val result = dao.getAll().first()

        assertEquals(1, result.size)
        assertEquals("Bread", result[0].name)
    }

    @Test
    fun deleteAll_clearsTheTable() = runTest {
        val item = MarketplaceItem(
            id = UUID.randomUUID(),
            name = "Butter",
            quantity = 1,
            description = "Salted",

            price = 5.0,

            sellerUid = "user123",
            sellerName = "Bipin",
            postedAt = Date(1111)
        )

        dao.insert(item)

        dao.deleteAll()

        val result = dao.getAll().first()

        assertEquals(0, result.size)
    }
}
