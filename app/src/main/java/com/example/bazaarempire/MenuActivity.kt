package com.example.bazaarempire

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
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

        findViewById<ImageButton>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.btnTutorial).setOnClickListener {
            showCustomDialog("آموزش (Tutorial)", 
                "۱. روی دکمه ضربه بزنید تا سکه بگیرید.\n" +
                "۲. مراقب سکه‌های بنفش باشید!\n" +
                "۳. سریع به فرش برسید!", "Got it", null, false)
        }

        findViewById<ImageButton>(R.id.btnLeaderboard).setOnClickListener {
            val mockScores = "1. Ali - 00:42\n2. Reza - 00:51\n3. Sara - 01:05"
            val userScore = if (bestTime != 9999) String.format("%02d:%02d", bestTime / 60, bestTime % 60) else "--:--"
            showCustomDialog("نفرات برتر (Ranks)", "Top Merchants:\n\n$mockScores\n\nYour Best: $userScore", "Close", null, false)
        }

        findViewById<ImageButton>(R.id.btnShop).setOnClickListener {
            showCustomDialog("فروشگاه (Shop)", "Avatars & Cosmetics coming soon!", "Close", null, false)
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_message, null)
        
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val layoutSettings = dialogView.findViewById<android.widget.LinearLayout>(R.id.layoutSettings)
        val cbMusic = dialogView.findViewById<CheckBox>(R.id.cbMusic)
        val cbSound = dialogView.findViewById<CheckBox>(R.id.cbSound)
        val btnPositive = dialogView.findViewById<Button>(R.id.btnPositive)
        val btnNegative = dialogView.findViewById<Button>(R.id.btnNegative)

        tvTitle.text = "تنظیمات (Settings)"
        tvMessage.visibility = android.view.View.GONE
        layoutSettings.visibility = android.view.View.VISIBLE
        
        cbMusic.isChecked = prefs.getBoolean("music_enabled", true)
        cbSound.isChecked = prefs.getBoolean("sound_enabled", true)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Make dialog background transparent so our custom panel shows
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnPositive.text = "Save"
        btnPositive.setOnClickListener {
            prefs.edit().putBoolean("music_enabled", cbMusic.isChecked).apply()
            prefs.edit().putBoolean("sound_enabled", cbSound.isChecked).apply()
            dialog.dismiss()
        }

        btnNegative.text = "Cancel"
        btnNegative.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showCustomDialog(title: String, message: String, posText: String, negText: String?, isAd: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_message, null)
        
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val btnPositive = dialogView.findViewById<Button>(R.id.btnPositive)
        val btnNegative = dialogView.findViewById<Button>(R.id.btnNegative)

        tvTitle.text = title
        tvMessage.text = message
        btnPositive.text = posText

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnPositive.setOnClickListener { dialog.dismiss() }

        if (negText != null) {
            btnNegative.visibility = android.view.View.VISIBLE
            btnNegative.text = negText
            btnNegative.setOnClickListener { dialog.dismiss() }
        } else {
            btnNegative.visibility = android.view.View.GONE
        }

        dialog.show()
    }
}
