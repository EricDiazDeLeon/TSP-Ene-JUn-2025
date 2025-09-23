package com.example.mirutadigital.data.repository

import com.example.mirutadigital.data.model.User
import kotlinx.coroutines.delay

class UserRepository {
    // Datos de ejemplo (simulamos una base de datos/API) <---Aqui se tiene que cambiar para conectar con al API
    private val users = listOf(
        User(1, "Ana García", "ana@email.com", "123-456-789"),
        User(2, "Carlos López", "carlos@email.com", "987-654-321"),
        User(3, "María Rodríguez", "maria@email.com", "555-123-456")
    )

    // Simular obtención de datos (como si fuera una API)
    suspend fun getAllUsers(): List<User> {
        delay(1000) // Simular delay de red
        return users
    }

    suspend fun getUserById(id: Int): User? {
        delay(500) // Simular delay de red
        return users.find { it.id == id }
    }

    suspend fun searchUsers(query: String): List<User> {
        delay(300) // Simular delay de red
        return users.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        }
    }
}