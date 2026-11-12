package com.example.bazaarempire

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var coins = 0L
    private var currentItemIndex = 0
    private var upgradeCost = 30L
    private var baseTapValue = 1L
    private var isAdBoostActive = false
    private var badCoinClickCount = 0

    private var secondsElapsed = 0
    private var isGameRunning = false
    private var hasWon = false

    private lateinit var tvCoinCount: TextView
    private lateinit var tvUpgradeCost: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnTap: ImageButton
    private lateinit var btnUpgrade: ImageButton
    private lateinit var itemImage: ImageView
    private lateinit var ivSparkle: ImageView
    private lateinit var btnAd: ImageButton
    private lateinit var btnShop: ImageButton
    private lateinit var btnGoldenDeal: ImageButton
    private lateinit var btnBadCoin: ImageButton

    private val handler = Handler(Looper.getMainLooper())
    private val items = listOf(
        R.drawable.item_tea, R.drawable.item_spices, R.drawable.item_pistachios,
        R.drawable.item_copper, R.drawable.item_carpet, R.drawable.item_saffron
    )

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isGameRunning) {
                secondsElapsed++
                val mins = toPersianDigits(String.format("%02d", secondsElapsed / 60))
                val secs = toPersianDigits(String.format("%02d", secondsElapsed % 60))
                tvTimer.text = "$mins:$secs"
                handler.postDelayed(this, 1000)
            }
        }
    }

    // Helper to convert English numbers to Farsi
    private fun toPersianDigits(number: String): String {
        var result = number
        val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        for (i in englishDigits.indices) {
            result = result.replace(englishDigits[i], persianDigits[i])
        }
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCoinCount = findViewById(R.id.tvCoinCount)
        tvUpgradeCost = findViewById(R.id.tvUpgradeCost)
        tvTimer = findViewById(R.id.tvTimer)
        btnTap = findViewById(R.id.btnTap)
        btnUpgrade = findViewById(R.id.btnUpgrade)
        itemImage = findViewById(R.id.itemImage)
        ivSparkle = findViewById(R.id.ivSparkle)
        btnAd = findViewById(R.id.btnAd)
        btnShop = findViewById(R.id.btnShop)
        btnGoldenDeal = findViewById(R.id.btnGoldenDeal)
        btnBadCoin = findViewById(R.id.btnBadCoin)

        loadGameData()
        updateUI()

        btnTap.setOnClickListener {
            if (hasWon) return@setOnClickListener

            if (!isGameRunning) {
                isGameRunning = true
                handler.post(timerRunnable)
            }

            coins += if (isAdBoostActive) baseTapValue * 2 else baseTapValue
            updateUI()
            
            btnTap.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50).withEndAction {
                btnTap.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
            }.start()

            if (btnGoldenDeal.visibility == View.GONE && btnBadCoin.visibility == View.GONE) {
                val chance = Random.nextInt(100)
                if (chance < 10) spawnSpecialCoin(btnGoldenDeal)
                else if (chance in 11..35) spawnSpecialCoin(btnBadCoin)
            }
        }

        btnGoldenDeal.setOnClickListener {
            val bonus = baseTapValue * 10
            coins += bonus
            updateUI()
            Toast.makeText(this, "جایزه طلایی! +${toPersianDigits(bonus.toString())}", Toast.LENGTH_SHORT).show()
            btnGoldenDeal.visibility = View.GONE
            resumeTimerIfNeeded()
        }

        btnBadCoin.setOnClickListener {
            val fullPenalty = baseTapValue * 15
            btnBadCoin.visibility = View.GONE
            pauseTimer()
            
            badCoinClickCount++
            if (badCoinClickCount == 1 || badCoinClickCount % 3 == 0) {
                showBadCoinDialog(fullPenalty)
            } else {
                applyPenalty(fullPenalty)
                Toast.makeText(this, "سکه نفرین شده! -${toPersianDigits(fullPenalty.toString())}", Toast.LENGTH_SHORT).show()
                resumeTimerIfNeeded()
            }
        }

        btnUpgrade.setOnClickListener {
            if (hasWon) return@setOnClickListener

            if (coins >= upgradeCost) {
                coins -= upgradeCost
                upgradeCost = (upgradeCost * 1.5).toLong() 
                baseTapValue += 3

                if (currentItemIndex < items.size - 1) {
                    currentItemIndex++
                    itemImage.setImageResource(items[currentItemIndex])
                    playSparkleAnimation()

                    if (currentItemIndex == items.size - 1) {
                        winGame()
                    }
                }
                updateUI()
            } else {
                Toast.makeText(this, "سکه کافی ندارید!", Toast.LENGTH_SHORT).show()
            }
        }

        btnAd.setOnClickListener {
            if (!isAdBoostActive) {
                isAdBoostActive = true
                Toast.makeText(this, "تبلیغات تستی: درآمد ۲ برابر برای ۳۰ ثانیه!", Toast.LENGTH_SHORT).show()
                updateUI()
                handler.postDelayed({
                    isAdBoostActive = false
                    updateUI()
                }, 30000) 
            }
        }

        btnShop.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            saveGameData()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        saveGameData()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun pauseTimer() { handler.removeCallbacks(timerRunnable) }
    
    private fun resumeTimerIfNeeded() {
        if (isGameRunning && !hasWon) {
            handler.removeCallbacks(timerRunnable)
            handler.post(timerRunnable)
        }
    }

    private fun saveGameData() {
        if (hasWon) return 
        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putLong("saved_coins", coins)
            putInt("saved_item_index", currentItemIndex)
            putLong("saved_upgrade_cost", upgradeCost)
            putLong("saved_tap_value", baseTapValue)
            putInt("saved_time", secondsElapsed)
            putBoolean("saved_game_running", isGameRunning)
            apply()
        }
    }

    private fun loadGameData() {
        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        coins = prefs.getLong("saved_coins", 0L)
        currentItemIndex = prefs.getInt("saved_item_index", 0)
        upgradeCost = prefs.getLong("saved_upgrade_cost", 30L)
        baseTapValue = prefs.getLong("saved_tap_value", 1L)
        secondsElapsed = prefs.getInt("saved_time", 0)
        isGameRunning = prefs.getBoolean("saved_game_running", false)
        
        val mins = toPersianDigits(String.format("%02d", secondsElapsed / 60))
        val secs = toPersianDigits(String.format("%02d", secondsElapsed % 60))
        tvTimer.text = "$mins:$secs"
        itemImage.setImageResource(items[currentItemIndex])
        
        if (isGameRunning) handler.post(timerRunnable)
    }

    private fun clearSavedData() {
        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        prefs.edit().remove("saved_coins").remove("saved_item_index").remove("saved_upgrade_cost")
            .remove("saved_tap_value").remove("saved_time").remove("saved_game_running").apply()
    }

    private fun showBadCoinDialog(penalty: Long) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = "سکه نفرین شده!"
        val penaltyFa = toPersianDigits(penalty.toString())
        dialog.findViewById<TextView>(R.id.tvDialogMessage).text = "شما $penaltyFa سکه از دست می‌دهید.\nآیا می‌خواهید با تماشای تبلیغات خسارت را نصف کنید؟"
        
        val btnPos = dialog.findViewById<Button>(R.id.btnDialogPositive)
        val btnNeg = dialog.findViewById<Button>(R.id.btnDialogNegative)

        btnPos.text = "تبلیغات"
        btnNeg.text = "قبول خسارت"
        btnNeg.visibility = View.VISIBLE

        btnPos.setOnClickListener {
            applyPenalty((penalty * 0.5).toLong())
            Toast.makeText(this, "تبلیغات تستی: خسارت نصف شد!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            resumeTimerIfNeeded()
        }

        btnNeg.setOnClickListener {
            applyPenalty(penalty)
            dialog.dismiss()
            resumeTimerIfNeeded()
        }

        dialog.show()
    }

    private fun applyPenalty(penalty: Long) {
        coins -= penalty
        if (coins < 0) coins = 0
        updateUI()
    }

    private fun spawnSpecialCoin(coinButton: ImageButton) {
        coinButton.visibility = View.VISIBLE
        coinButton.x = Random.nextInt(50, 800).toFloat()
        coinButton.y = Random.nextInt(300, 1200).toFloat()

        coinButton.scaleX = 0f
        coinButton.scaleY = 0f
        coinButton.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction {
            coinButton.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
        }.start()

        pauseTimer()

        val displayTime = if (coinButton == btnBadCoin) 1200L else 2500L

        handler.postDelayed({
            if (coinButton.visibility == View.VISIBLE) {
                coinButton.visibility = View.GONE
                resumeTimerIfNeeded()
            }
        }, displayTime)
    }

    private fun playSparkleAnimation() {
        ivSparkle.visibility = View.VISIBLE
        ivSparkle.scaleX = 0.5f; ivSparkle.scaleY = 0.5f; ivSparkle.alpha = 1f
        ivSparkle.animate().scaleX(2f).scaleY(2f).alpha(0f).setDuration(600).withEndAction { ivSparkle.visibility = View.GONE }.start()
    }

    private fun winGame() {
        isGameRunning = false
        hasWon = true
        pauseTimer()
        clearSavedData()

        btnUpgrade.isEnabled = false
        btnUpgrade.alpha = 0.5f
        tvUpgradeCost.text = "پایان یافت!"

        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        val previousBest = prefs.getInt("best_time", 9999)
        var isNewRecord = false

        if (secondsElapsed < previousBest) {
            prefs.edit().putInt("best_time", secondsElapsed).apply()
            isNewRecord = true
        }

        val rank = when {
            secondsElapsed < 45 -> "افسانه بازار"
            secondsElapsed < 90 -> "تاجر بزرگ"
            secondsElapsed < 150 -> "تاجر ماهر"
            else -> "تازه‌کار"
        }

        val recordText = if (isNewRecord) "\n\n🌟 رکورد جدید! 🌟" else ""
        val message = "زمان: ${tvTimer.text}\nرتبه: $rank$recordText"

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = "پیروزی!"
        dialog.findViewById<TextView>(R.id.tvDialogMessage).text = message
        
        val btnPos = dialog.findViewById<Button>(R.id.btnDialogPositive)
        val btnNeg = dialog.findViewById<Button>(R.id.btnDialogNegative)

        btnPos.text = "بازی مجدد"
        btnNeg.text = "منو"
        btnNeg.visibility = View.VISIBLE

        // CRASH FIX: Safely remove all callbacks BEFORE dismissing or resetting
        btnPos.setOnClickListener { 
            handler.removeCallbacksAndMessages(null)
            dialog.dismiss()
            resetGame() 
        }
        btnNeg.setOnClickListener { 
            handler.removeCallbacksAndMessages(null)
            dialog.dismiss()
            finish() 
        }

        dialog.show()
    }

    private fun resetGame() {
        handler.removeCallbacksAndMessages(null)
        coins = 0L; currentItemIndex = 0; upgradeCost = 30L; baseTapValue = 1L
        isAdBoostActive = false; secondsElapsed = 0; isGameRunning = false; hasWon = false
        tvTimer.text = "۰۰:۰۰"
        itemImage.setImageResource(items[0])
        btnGoldenDeal.visibility = View.GONE; btnBadCoin.visibility = View.GONE
        
        btnUpgrade.isEnabled = true
        btnUpgrade.alpha = 1f
        
        updateUI()
    }

    private fun formatNumber(number: Long): String {
        val formatted = when {
            number >= 1_000_000_000 -> String.format("%.1fB", number / 1_000_000_000.0)
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
        return toPersianDigits(formatted)
    }

    private fun updateUI() {
        tvCoinCount.text = formatNumber(coins)
        if (!hasWon) {
            tvUpgradeCost.text = "ارتقا: ${formatNumber(upgradeCost)}"
        }
        tvCoinCount.setTextColor(if (isAdBoostActive) 0xFFF4C430.toInt() else 0xFFF5E6D3.toInt())
    }
}
