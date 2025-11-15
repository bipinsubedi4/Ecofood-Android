package com.bipin080.ecofood.marketplace


data class MarketplaceItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val quantity: String = "",
    val expiryDate: String = "",
    val sellerUid: String = "",
    val sellerName: String = "",
    val contactEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

