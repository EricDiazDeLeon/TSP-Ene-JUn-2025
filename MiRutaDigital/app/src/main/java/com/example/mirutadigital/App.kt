package com.example.mirutadigital

import android.app.Application
import com.example.mirutadigital.data.local.AppDatabase
import com.example.mirutadigital.data.remote.FirestoreService
import com.example.mirutadigital.data.repository.AppRepository

class App : Application() {
    
    lateinit var repository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        val database = AppDatabase.getDatabase(this)
        val firestoreService = FirestoreService()
        repository = AppRepository(database.appDao(), firestoreService)
    }
}
