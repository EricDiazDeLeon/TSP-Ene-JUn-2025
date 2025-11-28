package com.example.mirutadigital.data.repository

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID
import androidx.core.content.edit

/**
 * regresa un ID unico y persistente para este usuario
 */
class UserIdProvider(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_id_prefs", Context.MODE_PRIVATE)

    private var currentUserId: String? = null

    companion object {
        private const val KEY_USER_ID = "key_user_id"
    }

    fun getUserId(): String {
        if (currentUserId != null) {
            return currentUserId!!
        }

        var userId = prefs.getString(KEY_USER_ID, null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_USER_ID, userId) }
        }
        currentUserId = userId
        return userId
    }
}