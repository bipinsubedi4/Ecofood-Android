package com.bipin080.ecofood

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bipin080.ecofood.data.MarketplaceDatabase
import com.bipin080.ecofood.data.MarketplaceItem
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
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [34], application = TestApp::class, manifest = Config.NONE)
class MarketplaceViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var db: MarketplaceDatabase
    private lateinit var viewModel: MarketplaceViewModel

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            MarketplaceDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        viewModel = MarketplaceViewModel(
            application = context,
            dao = db.marketplaceItemDao()
        )
    }



    @After
    fun tearDown() {
        db.close()
    }

    /** Helper function to generate a full valid item */
    private fun sampleItem(
        name: String = "Apple",
        price: Double = 2.5
    ): MarketplaceItem {
        return MarketplaceItem(
            id = UUID.randomUUID(),
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

    // -----------------------------
    // TEST: Add item
    // -----------------------------
    @Test
    fun addItem_setsSellerFields() = runTest(dispatcher) {

        val item = sampleItem()

        viewModel.addItem(item)
        scheduler.runCurrent()

        val result = db.marketplaceItemDao().getAll().first()

        assertThat(result).hasSize(1)

        val saved = result.first()
        assertThat(saved.sellerUid).isEqualTo(MarketplaceViewModel.LOCAL_USER_ID)
        assertThat(saved.sellerName).isEqualTo(MarketplaceViewModel.LOCAL_USER_NAME)
        assertThat(saved.postedAt).isNotNull()
    }

    // -----------------------------
    // TEST: Delete item
    // -----------------------------
    @Test
    fun deleteItem_removesItemCompletely() = runTest(dispatcher) {

        val item = sampleItem("Book", 0.0)

        viewModel.addItem(item)
        scheduler.runCurrent()

        val beforeDelete = db.marketplaceItemDao().getAll().first()
        assertThat(beforeDelete).hasSize(1)

        viewModel.deleteItem(beforeDelete.first())
        scheduler.runCurrent()

        val afterDelete = db.marketplaceItemDao().getAll().first()
        assertThat(afterDelete).isEmpty()
    }
}
