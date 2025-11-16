package com.bipin080.ecofood.data

import java.util.*
import java.util.concurrent.TimeUnit

fun fakeMarketplaceItems(): List<MarketplaceItem> {
    val calendar = Calendar.getInstance()
    
    return listOf(
        MarketplaceItem(
            name = "Organic Apples",
            quantity = "2",
            unit = "kg",
            price = 5.00,
            expiryDate = Date(calendar.timeInMillis + TimeUnit.DAYS.toMillis(5)),
            location = "Downtown",
            description = "Fresh organic apples, slightly overripe but perfect for baking.",
            sellerName = "Sarah M."
        ),
        MarketplaceItem(
            name = "Whole Grain Bread",
            quantity = "1",
            unit = "loaf",
            price = 3.50,
            expiryDate = Date(calendar.timeInMillis + TimeUnit.DAYS.toMillis(2)),
            location = "Suburbs",
            description = "A hearty loaf of whole grain bread, baked fresh this morning.",
            sellerName = "John D."
        )
    )
}
