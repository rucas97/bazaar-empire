package com.example.bazaarempire

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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

        findViewById<ImageButton>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.btnTutorial).setOnClickListener { showTutorial() }
        findViewById<ImageButton>(R.id.btnLeaderboard).setOnClickListener { showLeaderboard(bestTime) }
        findViewById<ImageButton>(R.id.btnShop).setOnClickListener { showShop() }
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { showSettings() }
    }

    private fun showTutorial() {
        AlertDialog.Builder(this).setTitle("آموزش بازی (Tutorial)")
            .setMessage("۱. روی دکمه بزرگ ضربه بزنید تا سکه جمع کنید.\n۲. با سکه‌ها اجناس را ارتقا دهید تا به فرش برسید.\n۳. مراقب سکه‌های نفرین شده (بنفش) باشید!\n۴. سکه‌های طلایی را سریع بزنید تا جایزه بگیرید.\n۵. هرچه سریع‌تر به فرش برسید، رتبه شما بالاتر می‌شود!")
            .setPositiveButton("Got it", null).show()
    }

    private fun showLeaderboard(bestTime: Int) {
        val mockScores = "1. Ali - 00:42\n2. Reza - 00:51\n3. Sara - 01:05"
        val userScore = if (bestTime != 9999) String.format("%02d:%02d", bestTime / 60, bestTime % 60) else "--:--"
        AlertDialog.Builder(this).setTitle("نفرات برتر (Leaderboard)")
            .setMessage("Top Bazaar Merchants:\n\n$mockScores\n\nYour Best: $userScore")
            .setPositiveButton("Close", null).show()
    }

    private fun showShop() {
        AlertDialog.Builder(this).setTitle("فروشگاه (Shop)")
            .setMessage("Avatars & Cosmetics coming soon!\n\n(You will buy these with your earned coins).")
            .setPositiveButton("Close", null).show()
    }

    private fun showSettings() {
        AlertDialog.Builder(this).setTitle("Settings (تنظیمات)")
            .setMessage("Sound: ON\nMusic: ON\nLanguage: English\n\n(Coming Soon)")
            .setPositiveButton("Close", null).show()
    }
}
