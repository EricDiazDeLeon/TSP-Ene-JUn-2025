package com.example.mirutadigital.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.mirutadigital.R
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.ui.view.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LocationSharingService : Service() {
    private lateinit var repository: AppRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var sharingJob: Job? = null
    private var truckId: String = ""
    private var routeId: String = ""

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                CoroutineScope(Dispatchers.IO).launch {
                    repository.shareLocation(
                        truckId = truckId,
                        routeId = routeId,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Inicializar repository aquí
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        truckId = intent?.getStringExtra(EXTRA_TRUCK_ID) ?: ""
        routeId = intent?.getStringExtra(EXTRA_ROUTE_ID) ?: ""

        showForegroundNotification()
        startLocationSharing()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationSharing()
    }

    private fun startLocationSharing() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 segundos
            fastestInterval = 5000 // 5 segundos
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Manejar error de permisos
        }
    }

    private fun stopLocationSharing() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun showForegroundNotification() {
        val channelId = "location_sharing_channel"
        val channelName = "Compartiendo Ubicación"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Compartiendo Ubicación")
            .setContentText("Tu ubicación se está compartiendo en tiempo real")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    companion object {
        const val EXTRA_TRUCK_ID = "truck_id"
        const val EXTRA_ROUTE_ID = "route_id"
    }
}
