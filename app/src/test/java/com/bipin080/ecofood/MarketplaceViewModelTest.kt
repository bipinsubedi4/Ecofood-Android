package com.bipin080.ecofood

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bipin080.ecofood.data.MarketplaceDatabase
import com.bipin080.ecofood.data.MarketplaceItem
import com.bipin080.ecofood.data.MarketplaceItemDao
import com.bipin080.ecofood.viewmodel.MarketplaceViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [34], application = TestApp::class, manifest = Config.NONE,shadows = [com.bipin080.ecofood.shadow.ShadowFirebaseMarketplaceSync::class])

class MarketplaceViewModelTest {

    @get:Rule val instantRule = InstantTaskExecutorRule()

    private lateinit var db: MarketplaceDatabase
    private lateinit var dao: MarketplaceItemDao
    private lateinit var viewModel: MarketplaceViewModel

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setup() {
        setFirebaseSyncDisabledForTests()
        val context = ApplicationProvider.getApplicationContext<Application>()

        // 1. Create in-memory Room DB
        db = Room.inMemoryDatabaseBuilder(
            context,
            MarketplaceDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.marketplaceItemDao()

        // 2. Create normal ViewModel (it will have wrong DAO initially)
        viewModel = MarketplaceViewModel(context)

        // 3. Inject our in-memory test DAO via reflection
        val daoField = MarketplaceViewModel::class.java.getDeclaredField("dao")
        daoField.isAccessible = true
        daoField.set(viewModel, dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** Helper to create sample item */
    private fun sampleItem(
        name: String = "Apple",
        price: Double = 2.5
    ): MarketplaceItem {
        return MarketplaceItem(
            id = UUID.randomUUID().toString(),
            name = name,
            quantity = 2,
            unit = "kg",
            price = price,
            expiryDate = "2026-01-01",
            location = "Brisbane",
            description = "Fresh fruit",
            imageUrl = null,
            sellerUid = "",
            sellerName = "",
            postedAt = null
        )
    }

    @Test
    fun addItem_setsSellerFields() = runTest(dispatcher) {
        val item = sampleItem()

        viewModel.addItem(item)
        scheduler.runCurrent()

        val result = dao.getAll().first()

        assertThat(result).hasSize(1)

        val saved = result.first()
        assertThat(saved.sellerUid).isEqualTo(MarketplaceViewModel.LOCAL_USER_ID)
        assertThat(saved.sellerName).isEqualTo(MarketplaceViewModel.LOCAL_USER_NAME)
        assertThat(saved.postedAt).isNotNull()
    }

    @Test
    fun deleteItem_removesItemCompletely() = runTest(dispatcher) {
        val item = sampleItem("Book", 0.0)

        // Add
        viewModel.addItem(item)
        scheduler.runCurrent()

        val before = dao.getAll().first()
        assertThat(before).hasSize(1)

        // Delete
        viewModel.deleteItem(before.first())
        scheduler.runCurrent()

        val after = dao.getAll().first()
        assertThat(after).isEmpty()
    }
}

private fun setFirebaseSyncDisabledForTests() {
    val clazz = Class.forName("com.bipin080.ecofood.sync.FirebaseMarketplaceSync")
    val field = clazz.getDeclaredField("ENABLED")
    field.isAccessible = true
    field.setBoolean(null, false)
}

