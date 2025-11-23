package com.bipin080.ecofood

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bipin080.ecofood.workers.NotificationWorker
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit

class EcoFoodApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

}
