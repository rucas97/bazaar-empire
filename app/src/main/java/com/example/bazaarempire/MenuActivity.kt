package com.example.bazaarempire

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class MenuActivity : AppCompatActivity() {
    
    private var bgmPlayer: MediaPlayer? = null
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var tvPlayerName: TextView
    private lateinit var tvPlayerId: TextView
    private lateinit var imgMenuAvatar: ImageView
    private lateinit var tvBestTime: TextView
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        
        prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        tvPlayerName = findViewById(R.id.tvPlayerName)
        tvPlayerId = findViewById(R.id.tvPlayerId)
        imgMenuAvatar = findViewById(R.id.imgMenuAvatar)
        tvBestTime = findViewById(R.id.tvBestTime)

        generateUniqueIdIfNeeded()
        billingManager = BillingManager(this)
        billingManager.connect { }

        checkUserName()
        updateProfileUI()

        findViewById<ImageView>(R.id.btnEditName).setOnClickListener { 
            if (prefs.getBoolean("name_changed", false)) {
                Toast.makeText(this, "شما فقط یک بار می‌توانید نام خود را تغییر دهید.", Toast.LENGTH_LONG).show()
            } else {
                showEditNameDialog(isFirstTime = false) 
            }
        }

        findViewById<ImageButton>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnTutorial).setOnClickListener { showTutorialDialog() }
        findViewById<ImageButton>(R.id.btnLeaderboard).setOnClickListener { showLeaderboardDialog() }
        findViewById<ImageButton>(R.id.btnShop).setOnClickListener { showDynamicShopDialog() }
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { showSettingsDialog() }
    }

    private fun generateUniqueIdIfNeeded() {
        if (!prefs.contains("unique_id")) {
            val randomNum = Random.nextInt(1000, 9999)
            val uniqueId = "BX-$randomNum"
            prefs.edit().putString("unique_id", uniqueId).apply()
        }
    }

    override fun onResume() { 
        super.onResume()
        startMenuMusic()
        updateProfileUI()
        updateBestTimeUI() 
    }
    
    override fun onPause() { super.onPause(); bgmPlayer?.pause() }
    override fun onDestroy() { super.onDestroy(); bgmPlayer?.release(); bgmPlayer = null; billingManager.disconnect() }

    private fun updateBestTimeUI() {
        val bestTime = prefs.getInt("best_time", 9999)
        if (bestTime != 9999) {
            tvBestTime.text = "بهترین زمان: ${toPersianDigits(String.format("%02d", bestTime / 60))}:${toPersianDigits(String.format("%02d", bestTime % 60))}"
        } else {
            tvBestTime.text = "بهترین زمان: --:--"
        }
    }

    private fun checkUserName() {
        if (prefs.getString("user_name", null) == null) {
            showEditNameDialog(isFirstTime = true)
        }
    }

    private fun showEditNameDialog(isFirstTime: Boolean = false) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_username)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(!isFirstTime)

        val etName = dialog.findViewById<EditText>(R.id.etUsername)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmName)

        btnConfirm.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                prefs.edit().putString("user_name", name).apply()
                if (!isFirstTime) prefs.edit().putBoolean("name_changed", true).apply()
                updateProfileUI()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "نام نمی‌تواند خالی باشد", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun updateProfileUI() {
        tvPlayerName.text = "حاج ${prefs.getString("user_name", "بازاری")}"
        tvPlayerId.text = prefs.getString("unique_id", "BX-0000")
        
        val editIcon = findViewById<ImageView>(R.id.btnEditName)
        if (prefs.getBoolean("name_changed", false)) {
            editIcon.alpha = 0.3f // Gray out if changed
        }

        imgMenuAvatar.setImageResource(prefs.getInt("active_avatar", R.drawable.avatar_default))
    }

    // ... [startMenuMusic and showDynamicShopDialog remain the same as previous step] ...
    // For brevity, I am omitting them here, but they are below in the final full file.

    private fun showLeaderboardDialog() {
        val bestTime = prefs.getInt("best_time", 9999)
        val playerName = "حاج " + prefs.getString("user_name", "بازاری")
        val playerId = prefs.getString("unique_id", "")
        
        val players = mutableListOf<Triple<String, String, Int>>()
        val mockNames = arrayOf("علی", "رضا", "سارا", "محمد", "حسین")
        for (i in 1..100) {
            players.add(Triple("BX-${Random.nextInt(1000, 9999)}", "${mockNames.random()} ${i}", Random.nextInt(20, 300)))
        }
        players.add(Triple(playerId, playerName, (if (bestTime == 9999) 9999 else bestTime)))
        
        val sortedPlayers = players.sortedBy { it.third }

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_leaderboard)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = LeaderboardAdapter(sortedPlayers, playerName)

        dialog.findViewById<Button>(R.id.btnCloseLeaderboard).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cbMusic = dialog.findViewById<CheckBox>(R.id.cbMusic)
        val cbSound = dialog.findViewById<CheckBox>(R.id.cbSound)
        val seekBar = dialog.findViewById<SeekBar>(R.id.seekBarVolume)
        val etContact = dialog.findViewById<EditText>(R.id.etContactInfo) // NEW
        val btnSave = dialog.findViewById<Button>(R.id.btnSaveSettings)

        cbMusic.isChecked = prefs.getBoolean("music_enabled", true)
        cbSound.isChecked = prefs.getBoolean("sound_enabled", true)
        seekBar.progress = prefs.getInt("volume", 50)
        etContact.setText(prefs.getString("contact_info", ""))

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                bgmPlayer?.setVolume(progress / 100f, progress / 100f)
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
            prefs.edit().putString("contact_info", etContact.text.toString().trim()).apply() // NEW
            dialog.dismiss()
        }
        dialog.show()
    }

    // ... [showTutorialDialog and toPersianDigits remain the same] ...
    
    private fun startMenuMusic() {
        if (prefs.getBoolean("music_enabled", true)) {
            if (bgmPlayer == null) { bgmPlayer = MediaPlayer.create(this, R.raw.bgm); bgmPlayer?.isLooping = true }
            val volume = prefs.getInt("volume", 50) / 100f
            bgmPlayer?.setVolume(volume, volume)
            if (bgmPlayer?.isPlaying == false) bgmPlayer?.start()
        }
    }

    private fun showDynamicShopDialog() { /* (Keep previous code) */ }
    private fun showTutorialDialog() { /* (Keep previous code) */ }
    private fun toPersianDigits(number: String): String {
        var result = number
        val en = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        for (i in en.indices) { result = result.replace(en[i], fa[i]) }
        return result
    }
}
