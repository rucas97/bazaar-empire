package com.example.bazaarempire

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show the background image as the splash screen
        setContentView(R.layout.activity_splash)

        // Wait 2 seconds, then go to the Main Menu
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MenuActivity::class.java))
            finish() // Close splash so user can't go back to it
        }, 2000) // 2000ms = 2 seconds
    }
}
