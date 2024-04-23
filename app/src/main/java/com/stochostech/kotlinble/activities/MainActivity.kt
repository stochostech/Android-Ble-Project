package com.stochostech.kotlinble.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.stochostech.kotlinble.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.centralBTN.setOnClickListener {
            startActivity(Intent(this, Central::class.java))
        }

        binding.peripheralBTN.setOnClickListener {
            startActivity(Intent(this, Peripheral::class.java))
        }

    }
}