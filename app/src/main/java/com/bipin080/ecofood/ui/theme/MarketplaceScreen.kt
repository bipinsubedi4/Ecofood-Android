package com.bipin080.ecofood.ui.theme

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bipin080.ecofood.R
import com.bipin080.ecofood.data.MarketplaceItem
import com.bipin080.ecofood.viewmodel.MarketplaceViewModel
import java.util.*
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    marketplaceViewModel: MarketplaceViewModel,
    onAddItem: () -> Unit,
    onViewMyListings: () -> Unit
) {
    val items by marketplaceViewModel.marketplaceItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "EcoFood Logo",
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Community Marketplace", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItem,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Share Food")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Share your surplus food with the community and discover what others are offering.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Button(onClick = onViewMyListings, modifier = Modifier.fillMaxWidth()) {
                Text("View My Listings")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items) { item ->
                    MarketplaceListItem(item = item)
                }
            }
        }
    }
}

@Composable
fun MarketplaceListItem(item: MarketplaceItem) {

    val expiryDays = remember(item.expiryDate) {
        try {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val expiryDate = item.expiryDate?.let { formatter.parse(it) }
            if (expiryDate != null) {
                TimeUnit.MILLISECONDS.toDays(expiryDate.time - System.currentTimeMillis())
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    val expiryColor =
        if (expiryDays < 3) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {

            // Image banner
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {

                // Title
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Price
                Text(
                    text = "$${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Expiry Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Expiry",
                        tint = expiryColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = "Expires in $expiryDays days",
                        style = MaterialTheme.typography.bodyMedium.copy(color = expiryColor)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Posted info
                val relativePostTime = getRelativeTime(item.postedAt)
                Text(
                    text = "Posted by ${item.sellerName} • $relativePostTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contact button
                Button(
                    onClick = { /* contact seller */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Contact Seller", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


fun getRelativeTime(date: Date?): String {
    if (date == null) return ""
    return DateUtils.getRelativeTimeSpanString(
        date.time,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
