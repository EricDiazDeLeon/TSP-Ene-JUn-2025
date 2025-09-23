package com.example.mirutadigital.ui.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mirutadigital.R
import com.example.mirutadigital.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var textUserList: TextView
    private lateinit var buttonRefresh: Button

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textUserList = findViewById(R.id.textUserList)
        buttonRefresh = findViewById(R.id.buttonRefresh)

        viewModel = MainViewModel()

        buttonRefresh.setOnClickListener {
            viewModel.loadUsers()
            showUsers()
        }

        showUsers()
    }

    private fun showUsers() {
        val users = viewModel.getUsersDirectly()
        val userText = StringBuilder()
        userText.append("Usuarios: ${users.size}\n\n")
        users.forEach { user ->
            userText.append("• ${user.name} - ${user.email}\n")
        }
        textUserList.text = userText.toString()
    }
}