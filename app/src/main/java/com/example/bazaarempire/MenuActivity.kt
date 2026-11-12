package com.example.bazaarempire

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    
    private var bgmPlayer: MediaPlayer? = null
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var tvPlayerName: TextView
    private lateinit var imgMenuAvatar: ImageView
    private lateinit var tvBestTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        
        prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        tvPlayerName = findViewById(R.id.tvPlayerName)
        imgMenuAvatar = findViewById(R.id.imgMenuAvatar)
        tvBestTime = findViewById(R.id.tvBestTime)

        checkUserName()
        updateProfileUI()

        findViewById<ImageButton>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnTutorial).setOnClickListener { showTutorialDialog() }
        findViewById<ImageButton>(R.id.btnLeaderboard).setOnClickListener { showLeaderboardDialog() }
        findViewById<ImageButton>(R.id.btnShop).setOnClickListener { showShopDialog() }
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { showSettingsDialog() }
    }

    override fun onResume() { 
        super.onResume()
        startMenuMusic()
        updateProfileUI()
        updateBestTimeUI() // Updates best time when returning from game
    }
    
    override fun onPause() { 
        super.onPause()
        bgmPlayer?.pause() 
    }
    
    override fun onDestroy() { 
        super.onDestroy()
        bgmPlayer?.release()
        bgmPlayer = null 
    }

    private fun updateBestTimeUI() {
        val bestTime = prefs.getInt("best_time", 9999)
        if (bestTime != 9999) {
            val mins = toPersianDigits(String.format("%02d", bestTime / 60))
            val secs = toPersianDigits(String.format("%02d", bestTime % 60))
            tvBestTime.text = "بهترین زمان: $mins:$secs"
        } else {
            tvBestTime.text = "بهترین زمان: --:--"
        }
    }

    private fun checkUserName() {
        val userName = prefs.getString("user_name", null)
        if (userName == null) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_username)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)

            val etName = dialog.findViewById<EditText>(R.id.etUsername)
            val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmName)

            btnConfirm.setOnClickListener {
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    prefs.edit().putString("user_name", name).apply()
                    updateProfileUI()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "نام نمی‌تواند خالی باشد", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }
    }

    private fun updateProfileUI() {
        val name = prefs.getString("user_name", "بازاری")
        tvPlayerName.text = "حاج $name"
        
        val avatarId = prefs.getInt("active_avatar", R.drawable.avatar_default)
        imgMenuAvatar.setImageResource(avatarId)
    }

    private fun startMenuMusic() {
        if (prefs.getBoolean("music_enabled", true)) {
            if (bgmPlayer == null) {
                bgmPlayer = MediaPlayer.create(this, R.raw.bgm)
                bgmPlayer?.isLooping = true
            }
            val volume = prefs.getInt("volume", 50) / 100f
            bgmPlayer?.setVolume(volume, volume)
            if (bgmPlayer?.isPlaying == false) bgmPlayer?.start()
        }
    }

    private fun showShopDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_shop)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<Button>(R.id.btnBuy1).setOnClickListener { equipAvatar(R.drawable.avatar_1) }
        dialog.findViewById<Button>(R.id.btnBuy2).setOnClickListener { equipAvatar(R.drawable.avatar_2) }
        dialog.findViewById<Button>(R.id.btnBuy3).setOnClickListener { equipAvatar(R.drawable.avatar_3) }
        dialog.findViewById<Button>(R.id.btnCloseShop).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun equipAvatar(avatarResId: Int) {
        prefs.edit().putInt("active_avatar", avatarResId).apply()
        updateProfileUI()
        Toast.makeText(this, "آواتار با موفقیت تغییر کرد!", Toast.LENGTH_SHORT).show()
    }

    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cbMusic = dialog.findViewById<CheckBox>(R.id.cbMusic)
        val cbSound = dialog.findViewById<CheckBox>(R.id.cbSound)
        val seekBar = dialog.findViewById<SeekBar>(R.id.seekBarVolume)
        val btnSave = dialog.findViewById<Button>(R.id.btnSaveSettings)

        cbMusic.isChecked = prefs.getBoolean("music_enabled", true)
        cbSound.isChecked = prefs.getBoolean("sound_enabled", true)
        seekBar.progress = prefs.getInt("volume", 50)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val vol = progress / 100f
                bgmPlayer?.setVolume(vol, vol)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        cbMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) { if (bgmPlayer == null) startMenuMusic() else bgmPlayer?.start() } 
            else { bgmPlayer?.pause() }
        }

        btnSave.setOnClickListener {
            prefs.edit().putBoolean("music_enabled", cbMusic.isChecked).apply()
            prefs.edit().putBoolean("sound_enabled", cbSound.isChecked).apply()
            prefs.edit().putInt("volume", seekBar.progress).apply()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showTutorialDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = "آموزش بازی"
        dialog.findViewById<TextView>(R.id.tvDialogMessage).text = "۱. روی دکمه بزرگ ضربه بزنید تا سکه جمع کنید.\n۲. اجناس را ارتقا دهید تا به زعفران برسید.\n۳. مراقب سکه‌های نفرین شده باشید!\n۴. سکه‌های طلایی را سریع بزنید تا جایزه بگیرید."
        
        val btnNeg = dialog.findViewById<Button>(R.id.btnDialogNegative)
        btnNeg.visibility = android.view.View.GONE
        
        val btnPos = dialog.findViewById<Button>(R.id.btnDialogPositive)
        btnPos.text = "متوجه شدم"
        btnPos.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showLeaderboardDialog() {
        val bestTime = prefs.getInt("best_time", 9999)
        val userScore = if (bestTime != 9999) "${toPersianDigits(String.format("%02d", bestTime / 60))}:${toPersianDigits(String.format("%02d", bestTime % 60))}" else "--:--"
        
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = "نفرات برتر"
        dialog.findViewById<TextView>(R.id.tvDialogMessage).text = "۱. علی - ۰۰:۴۲\n۲. رضا - ۰۰:۵۱\n۳. سارا - ۰۱:۰۵\n\nرکورد شما: $userScore"
        
        val btnNeg = dialog.findViewById<Button>(R.id.btnDialogNegative)
        btnNeg.visibility = android.view.View.GONE
        
        val btnPos = dialog.findViewById<Button>(R.id.btnDialogPositive)
        btnPos.text = "بستن"
        btnPos.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun toPersianDigits(number: String): String {
        var result = number
        val en = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        for (i in en.indices) { result = result.replace(en[i], fa[i]) }
        return result
    }
}
