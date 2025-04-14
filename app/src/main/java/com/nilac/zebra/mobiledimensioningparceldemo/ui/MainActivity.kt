package com.nilac.zebra.mobiledimensioningparceldemo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nilac.zebra.mobiledimensioningparceldemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
    }
}