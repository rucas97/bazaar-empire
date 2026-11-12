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
            val mins = toPersianDigits(String.format("%02d", bestTime / 60))
            val secs = toPersianDigits(String.format("%02d", bestTime % 60))
            tvBestTime.text = "بهترین زمان: $mins:$secs"
        } else {
            tvBestTime.text = "بهترین زمان: --:--"
        }

        findViewById<ImageButton>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.btnTutorial).setOnClickListener {
            showCustomDialog(
                title = "آموزش بازی",
                message = "۱. روی دکمه بزرگ ضربه بزنید تا سکه جمع کنید.\n" +
                          "۲. اجناس را ارتقا دهید تا به زعفران برسید.\n" +
                          "۳. مراقب سکه‌های نفرین شده باشید!\n" +
                          "۴. سکه‌های طلایی را سریع بزنید تا جایزه بگیرید.",
                posText = "متوجه شدم"
            )
        }

        findViewById<ImageButton>(R.id.btnLeaderboard).setOnClickListener {
            val userScore = if (bestTime != 9999) {
                val mins = toPersianDigits(String.format("%02d", bestTime / 60))
                val secs = toPersianDigits(String.format("%02d", bestTime % 60))
                "$mins:$secs"
            } else "--:--"
            showCustomDialog(
                title = "نفرات برتر",
                message = "۱. علی - ۰۰:۴۲\n۲. رضا - ۰۰:۵۱\n۳. سارا - ۰۱:۰۵\n\nرکورد شما: $userScore",
                posText = "بستن"
            )
        }

        findViewById<ImageButton>(R.id.btnShop).setOnClickListener {
            showShopDialog()
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun toPersianDigits(number: String): String {
        var result = number
        val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        for (i in englishDigits.indices) {
            result = result.replace(englishDigits[i], persianDigits[i])
        }
        return result
    }

    private fun showCustomDialog(title: String, message: String, posText: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = title
        dialog.findViewById<TextView>(R.id.tvDialogMessage).text = message
        val btnPos = dialog.findViewById<Button>(R.id.btnDialogPositive)
        dialog.findViewById<Button>(R.id.btnDialogNegative).visibility = android.view.View.GONE

        btnPos.text = posText
        btnPos.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showShopDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_shop)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<Button>(R.id.btnCloseShop).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showSettingsDialog() {
        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cbMusic = dialog.findViewById<CheckBox>(R.id.cbMusic)
        val cbSound = dialog.findViewById<CheckBox>(R.id.cbSound)
        val btnSave = dialog.findViewById<Button>(R.id.btnSaveSettings)

        cbMusic.isChecked = prefs.getBoolean("music_enabled", true)
        cbSound.isChecked = prefs.getBoolean("sound_enabled", true)

        btnSave.setOnClickListener {
            prefs.edit().putBoolean("music_enabled", cbMusic.isChecked).apply()
            prefs.edit().putBoolean("sound_enabled", cbSound.isChecked).apply()
            dialog.dismiss()
        }

        dialog.show()
    }
}
