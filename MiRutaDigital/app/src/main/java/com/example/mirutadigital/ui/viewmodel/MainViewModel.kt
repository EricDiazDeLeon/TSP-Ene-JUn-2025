package com.example.mirutadigital.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mirutadigital.data.model.User

class MainViewModel : ViewModel() {

    private val sampleUsers = listOf(
        User(1, "Ana García", "ana@email.com", "123-456-789"),
        User(2, "Carlos López", "carlos@email.com", "987-654-321"),
        User(3, "María Rodríguez", "maria@email.com", "555-123-456")
    )

    val users = MutableLiveData<List<User>>()
    val isLoading = MutableLiveData<Boolean>()

    fun loadUsers() {
        isLoading.value = true
        // Simular carga
        Thread {
            Thread.sleep(2000)
            users.postValue(sampleUsers)
            isLoading.postValue(false)
        }.start()
    }

    fun getUsersDirectly(): List<User> {
        return sampleUsers
    }
}