package com.example.mirutadigital.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mirutadigital.R
import com.example.mirutadigital.ui.map.MapFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapFragment())
                .commit()
        }
    }
}