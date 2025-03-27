package com.example.assignworkmanager

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scheduleNewsWorker()
        } else {
            findViewById<TextView>(R.id.statusTextView).text = "Notification permission denied"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scheduleButton = findViewById<Button>(R.id.scheduleButton)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)

        scheduleButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    scheduleNewsWorker()
                    statusTextView.text = "News updates scheduled! Check notifications soon."
                } else {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                scheduleNewsWorker()
                statusTextView.text = "News updates scheduled! Check notifications soon."
            }
        }
    }

    private fun scheduleNewsWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // One-time test request
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NewsWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)

        // Periodic request (15 minutes)
        val periodicWorkRequest = PeriodicWorkRequestBuilder<NewsWorker>(
            repeatInterval = 15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "newsWork",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
    }
}