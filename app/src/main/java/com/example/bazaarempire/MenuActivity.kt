package com.example.bazaarempire

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        val bestTime = prefs.getInt("best_time", 9999)

        val tvBestTime = findViewById<TextView>(R.id.tvBestTime)
        if (bestTime != 9999) {
            tvBestTime.text = String.format("Best Time: %02d:%02d", bestTime / 60, bestTime % 60)
        }

        // Play Button
        findViewById<ImageButton>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Tutorial Button
        findViewById<ImageButton>(R.id.btnTutorial).setOnClickListener {
            showTutorial()
        }

        // Leaderboard Button
        findViewById<ImageButton>(R.id.btnLeaderboard).setOnClickListener {
            showLeaderboard(bestTime)
        }

        // Shop Button
        findViewById<ImageButton>(R.id.btnShop).setOnClickListener {
            showShop()
        }

        // Settings Button
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        
        // Inflate our custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val cbMusic = dialogView.findViewById<CheckBox>(R.id.cbMusic)
        val cbSound = dialogView.findViewById<CheckBox>(R.id.cbSound)

        // Load current settings (default to true)
        cbMusic.isChecked = prefs.getBoolean("music_enabled", true)
        cbSound.isChecked = prefs.getBoolean("sound_enabled", true)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Save to memory
                prefs.edit().putBoolean("music_enabled", cbMusic.isChecked).apply()
                prefs.edit().putBoolean("sound_enabled", cbSound.isChecked).apply()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTutorial() {
        AlertDialog.Builder(this)
            .setTitle("آموزش بازی (Tutorial)")
            .setMessage("به امپراتوری بازار خوش آمدید!\n\n" +
                    "۱. روی دکمه بزرگ ضربه بزنید تا سکه جمع کنید.\n" +
                    "۲. با سکه‌ها اجناس را ارتقا دهید تا به فرش برسید.\n" +
                    "۳. مراقب سکه‌های نفرین شده (بنفش) باشید! اگر روی آن‌ها بزنید سکه از دست می‌دهید.\n" +
                    "۴. سکه‌های طلایی را سریع بزنید تا جایزه بگیرید.\n" +
                    "۵. هرچه سریع‌تر به فرش برسید، رتبه شما بالاتر خواهد بود!")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showLeaderboard(bestTime: Int) {
        val mockScores = "1. Ali (علی) - 00:42\n2. Reza (رضا) - 00:51\n3. Sara (سارا) - 01:05"
        val userScore = if (bestTime != 9999) String.format("%02d:%02d", bestTime / 60, bestTime % 60) else "--:--"
        
        AlertDialog.Builder(this)
            .setTitle("نفرات برتر (Leaderboard)")
            .setMessage("Top Bazaar Merchants:\n\n$mockScores\n\nYour Best: $userScore")
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showShop() {
        AlertDialog.Builder(this)
            .setTitle("فروشگاه (Shop)")
            .setMessage("Avatars & Cosmetics coming soon!\n\n(In the future, you will buy these with your earned coins).")
            .setPositiveButton("Close", null)
            .show()
    }
}
