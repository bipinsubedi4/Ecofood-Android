package com.bipin080.ecofood.data

fun fakeMarketplaceItems(): List<MarketplaceItem> {
    return listOf(
        MarketplaceItem(
            name = "Fresh Apples",
            description = "A bag of fresh apples from my backyard tree.",
            location = "San Francisco, CA",
            contact = "user1@example.com"
        ),
        MarketplaceItem(
            name = "Sourdough Starter",
            description = "A lively sourdough starter, ready to bake.",
            location = "Berkeley, CA",
            contact = "user2@example.com"
        ),
        MarketplaceItem(
            name = "Homemade Jam",
            description = "Delicious homemade strawberry jam.",
            location = "Oakland, CA",
            contact = "user3@example.com"
        )
    )
}
