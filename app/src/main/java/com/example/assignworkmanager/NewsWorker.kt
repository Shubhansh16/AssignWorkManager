package com.example.assignworkmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.assignworkmanager.retrofit.RetrofitInstance

class NewsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    companion object {
        const val CHANNEL_ID = "NewsChannel"
        const val NOTIFICATION_ID = 1
        private const val TAG ="NewsWorker"
    }

    private val apiKey = "YOUR_API_KEY" // Replace with your NewsAPI key

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started at ${System.currentTimeMillis()}")
        return try {
            val response = RetrofitInstance.api.getTopHeadlines("us", apiKey)
            Log.d(TAG, "API Response: status=${response.status}, articles=${response.articles.size}")
            if (response.status == "ok" && response.articles.isNotEmpty()) {
                val headline = response.articles[0].title ?: "No headline available"
                showNotification(headline)
                Log.d(TAG, "Notification triggered with headline: $headline")
                Result.success()
            } else {
                Log.d(TAG, "No valid articles found")
                Result.retry() // Retry if no articles
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching news: ${e.message}", e)
            Result.retry() // Retry on failure
        }
    }

    private fun showNotification(headline: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "News Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for news updates"
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Latest News Headline")
            .setContentText(headline)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Notification sent with ID $NOTIFICATION_ID")
    }
}
