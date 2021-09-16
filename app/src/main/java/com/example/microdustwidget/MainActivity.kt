package com.example.microdustwidget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.microdustwidget.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}