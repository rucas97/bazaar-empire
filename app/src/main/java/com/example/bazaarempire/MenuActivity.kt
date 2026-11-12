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
            val uniqueId = "BX-${Random.nextInt(1000, 9999)}"
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
        tvPlayerName.text = "حاج ${prefs.getString("user_name", "بازاری") ?: "بازاری"}"
        tvPlayerId.text = prefs.getString("unique_id", "BX-0000") ?: "BX-0000"
        
        val editIcon = findViewById<ImageView>(R.id.btnEditName)
        if (prefs.getBoolean("name_changed", false)) {
            editIcon.alpha = 0.3f
        }

        imgMenuAvatar.setImageResource(prefs.getInt("active_avatar", R.drawable.avatar_default))
    }

    private fun showLeaderboardDialog() {
        val bestTime = prefs.getInt("best_time", 9999)
        val playerName = "حاج " + (prefs.getString("user_name", "بازاری") ?: "بازاری")
        val playerId = prefs.getString("unique_id", "BX-0000") ?: "BX-0000"
        
        val players = mutableListOf<Triple<String, String, Int>>()
        val mockNames = arrayOf("علی", "رضا", "سارا", "محمد", "حسین")
        for (i in 1..100) {
            players.add(Triple("BX-${Random.nextInt(1000, 9999)}", "${mockNames.random()} ${i}", Random.nextInt(20, 300)))
        }
        
        // FIXED: Nullable safety applied here
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

    private fun showDynamicShopDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_shop)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val container = dialog.findViewById<LinearLayout>(R.id.shopContainer)
        val inflater = LayoutInflater.from(this)
        
        val purchasedAvatars = prefs.getStringSet("purchased_avatars", mutableSetOf()) ?: mutableSetOf()
        val activeAvatarId = prefs.getInt("active_avatar", R.drawable.avatar_default)

        var i = 1
        while (true) {
            val resName = "avatar_$i"
            val resId = resources.getIdentifier(resName, "drawable", packageName)
            if (resId == 0) break 

            val rowView = inflater.inflate(R.layout.item_shop_avatar, container, false)
            val img = rowView.findViewById<ImageView>(R.id.imgAvatar)
            val tvName = rowView.findViewById<TextView>(R.id.tvAvatarName)
            val btnAction = rowView.findViewById<Button>(R.id.btnAction)

            img.setImageResource(resId)
            val avatarNames = arrayOf("تاجر جوان", "استاد بازار", "تاجر ثروتمند")
            tvName.text = if (i <= avatarNames.size) avatarNames[i-1] else "آواتار شماره $i"

            if (purchasedAvatars.contains(resName)) {
                if (activeAvatarId == resId) {
                    btnAction.text = "فعال"
                    btnAction.isEnabled = false
                    btnAction.alpha = 0.5f
                } else {
                    btnAction.text = "استفاده"
                    btnAction.setOnClickListener {
                        prefs.edit().putInt("active_avatar", resId).apply()
                        updateProfileUI()
                        dialog.dismiss()
                        showDynamicShopDialog()
                    }
                }
            } else {
                btnAction.text = "۱۰,۰۰۰ تومان"
                btnAction.setOnClickListener {
                    billingManager.purchaseItem(
                        activity = this@MenuActivity,
                        productId = "avatar_$i",
                        onSuccess = {
                            val newSet = purchasedAvatars.toMutableSet()
                            newSet.add(resName)
                            prefs.edit().putStringSet("purchased_avatars", newSet).apply()
                            prefs.edit().putInt("active_avatar", resId).apply()
                            
                            Toast.makeText(this, "آواتار خریداری شد!", Toast.LENGTH_SHORT).show()
                            updateProfileUI()
                            dialog.dismiss()
                            showDynamicShopDialog()
                        },
                        onFailure = { errorMsg ->
                            Toast.makeText(this, "خطا در خرید: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
            container.addView(rowView)
            i++ 
        }
        dialog.findViewById<Button>(R.id.btnCloseShop).setOnClickListener { dialog.dismiss() }
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
        val etContact = dialog.findViewById<EditText>(R.id.etContactInfo)
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
            prefs.edit().putString("contact_info", etContact.text.toString().trim()).apply()
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
        
        dialog.findViewById<Button>(R.id.btnDialogNegative).visibility = android.view.View.GONE
        dialog.findViewById<Button>(R.id.btnDialogPositive).apply { text = "متوجه شدم"; setOnClickListener { dialog.dismiss() } }
        dialog.show()
    }

    private fun startMenuMusic() {
        if (prefs.getBoolean("music_enabled", true)) {
            if (bgmPlayer == null) { bgmPlayer = MediaPlayer.create(this, R.raw.bgm); bgmPlayer?.isLooping = true }
            val volume = prefs.getInt("volume", 50) / 100f
            bgmPlayer?.setVolume(volume, volume)
            if (bgmPlayer?.isPlaying == false) bgmPlayer?.start()
        }
    }

    private fun toPersianDigits(number: String): String {
        var result = number
        val en = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        for (i in en.indices) { result = result.replace(en[i], fa[i]) }
        return result
    }
}
