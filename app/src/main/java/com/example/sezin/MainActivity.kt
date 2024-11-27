package com.example.sezin

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sezin.user.login.LoginActivity
import com.example.sezin.user.AuthUtils
import com.example.sezin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.navigation_profile) {
                if (!AuthUtils.isLoggedIn(this)) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    return@setOnItemSelectedListener false
                }
            }
            navController.navigate(item.itemId)
            true
        }
    }
}
