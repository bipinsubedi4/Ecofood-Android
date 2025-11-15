package com.bipin080.ecofood.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bipin080.ecofood.R
import com.bipin080.ecofood.data.PantryDatabase
import kotlinx.coroutines.flow.first

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val pantryDao = PantryDatabase.getDatabase(applicationContext).pantryItemDao()
        val expiringSoonItems = pantryDao.getAll().first()
            .filter { it.expiryDate.time > System.currentTimeMillis() && (it.expiryDate.time - System.currentTimeMillis()) < 3 * 24 * 60 * 60 * 1000 } // 3 days

        if (expiringSoonItems.isNotEmpty()) {
            sendNotification(expiringSoonItems.size)
        }

        return Result.success()
    }

    private fun sendNotification(itemCount: Int) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pantry_notifications",
                "Pantry Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "pantry_notifications")
            .setContentTitle("You have expiring items!")
            .setContentText("$itemCount items in your pantry are expiring soon.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a proper icon
            .build()

        notificationManager.notify(1, notification)
    }
}
