package com.bipin080.ecofood.shadow

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(className = "com.bipin080.ecofood.sync.FirebaseMarketplaceSync")
class ShadowFirebaseMarketplaceSync {

    @Implementation
    fun start(dao: Any?, scope: Any?) {
        // Do nothing – fully disable Firebase sync for tests
    }
}
