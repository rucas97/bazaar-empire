package com.example.bazaarempire

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
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
            showCustomDialog(
                title = "آموزش بازی",
                message = "۱. روی دکمه بزرگ ضربه بزنید تا سکه جمع کنید.\n" +
                          "۲. اجناس را ارتقا دهید تا به فرش برسید.\n" +
                          "۳. مراقب سکه‌های نفرین شده باشید!\n" +
                          "۴. سکه‌های طلایی را سریع بزنید تا جایزه بگیرید.",
                posText = "متوجه شدم"
            )
        }

        findViewById<ImageButton>(R.id.btnLeaderboard).setOnClickListener {
            val userScore = if (bestTime != 9999) String.format("%02d:%02d", bestTime / 60, bestTime % 60) else "--:--"
            showCustomDialog(
                title = "نفرات برتر",
                message = "1. علی - 00:42\n2. رضا - 00:51\n3. سارا - 01:05\n\nرکورد شما: $userScore",
                posText = "بستن"
            )
        }

        findViewById<ImageButton>(R.id.btnShop).setOnClickListener {
            showCustomDialog(
                title = "فروشگاه",
                message = "آواتارها و ظاهر جدید به زودی اضافه می‌شوند!",
                posText = "بستن"
            )
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun showCustomDialog(title: String, message: String, posText: String, negText: String? = null, onPositive: (() -> Unit)? = null, onNegative: (() -> Unit)? = null) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = title
        dialog.findViewById<TextView>(R.id.tvDialogMessage).text = message
        val btnPos = dialog.findViewById<Button>(R.id.btnDialogPositive)
        val btnNeg = dialog.findViewById<Button>(R.id.btnDialogNegative)

        btnPos.text = posText
        btnPos.setOnClickListener {
            onPositive?.invoke()
            dialog.dismiss()
        }

        if (negText != null) {
            btnNeg.visibility = android.view.View.VISIBLE
            btnNeg.text = negText
            btnNeg.setOnClickListener {
                onNegative?.invoke()
                dialog.dismiss()
            }
        } else {
            btnNeg.visibility = android.view.View.GONE
        }

        dialog.show()
    }

    private fun showSettingsDialog() {
        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = "تنظیمات"
        dialog.findViewById<TextView>(R.id.tvDialogMessage).text = "تنظیمات صدا و موسیقی:"
        
        // Hiding default buttons
        dialog.findViewById<Button>(R.id.btnDialogPositive).visibility = android.view.View.GONE
        dialog.findViewById<Button>(R.id.btnDialogNegative).visibility = android.view.View.GONE

        // We will add checkboxes dynamically for now (Simplified)
        val cbMusic = CheckBox(this)
        cbMusic.text = "موسیقی"
        cbMusic.isChecked = prefs.getBoolean("music_enabled", true)
        cbMusic.setTextColor(android.graphics.Color.WHITE)
        
        val cbSound = CheckBox(this)
        cbSound.text = "افکت صوتی"
        cbSound.isChecked = prefs.getBoolean("sound_enabled", true)
        cbSound.setTextColor(android.graphics.Color.WHITE)

        // This is a quick hack to add checkboxes to the custom layout
        val layout = dialog.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.tvDialogMessage.parent as Int)
        // Just close the dialog on save for now.
        
        dialog.show()
    }
}
