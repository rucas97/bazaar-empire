package com.example.bazaarempire

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var coins = 0L
    private var currentItemIndex = 0
    private var upgradeCost = 10L
    private var baseTapValue = 1L
    private var isAdBoostActive = false

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
        R.drawable.item_copper, R.drawable.item_saffron, R.drawable.item_carpet
    )

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isGameRunning) {
                secondsElapsed++
                tvTimer.text = String.format("%02d:%02d", secondsElapsed / 60, secondsElapsed % 60)
                handler.postDelayed(this, 1000)
            }
        }
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

        updateUI()

        btnTap.setOnClickListener {
            if (!isGameRunning && !hasWon) {
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
            Toast.makeText(this, "Golden Deal! +$bonus", Toast.LENGTH_SHORT).show()
            btnGoldenDeal.visibility = View.GONE
            resumeTimerIfNeeded()
        }

        btnBadCoin.setOnClickListener {
            val fullPenalty = baseTapValue * 15
            btnBadCoin.visibility = View.GONE
            resumeTimerIfNeeded()

            AlertDialog.Builder(this)
                .setTitle("Cursed Coin! (سکه نفرین شده)")
                .setMessage("You will lose $fullPenalty coins. Watch a mock ad to reduce damage by 50%?")
                .setPositiveButton("Watch Ad") { _, _ ->
                    val reducedPenalty = (fullPenalty * 0.5).toLong()
                    applyPenalty(reducedPenalty)
                    Toast.makeText(this, "TEST AD: Damage reduced!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Accept Damage") { _, _ -> applyPenalty(fullPenalty) }
                .setCancelable(false)
                .show()
        }

        btnUpgrade.setOnClickListener {
            if (coins >= upgradeCost) {
                coins -= upgradeCost
                upgradeCost = (upgradeCost * 1.5).toLong() 
                baseTapValue += 2 

                if (currentItemIndex < items.size - 1) {
                    currentItemIndex++
                    itemImage.setImageResource(items[currentItemIndex])
                    playSparkleAnimation()
                    if (currentItemIndex == items.size - 1) winGame()
                }
                updateUI()
            } else {
                Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show()
            }
        }

        btnAd.setOnClickListener {
            if (!isAdBoostActive) {
                isAdBoostActive = true
                Toast.makeText(this, "TEST AD: 2x Income for 30 seconds!", Toast.LENGTH_SHORT).show()
                updateUI()
                handler.postDelayed({ isAdBoostActive = false; updateUI() }, 30000) 
            }
        }

        // EXIT TO MENU BUTTON (Shop button acts as Menu return)
        btnShop.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
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

        coinButton.scaleX = 0f; coinButton.scaleY = 0f
        coinButton.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction {
            coinButton.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
        }.start()

        handler.removeCallbacks(timerRunnable)
        val displayTime = if (coinButton == btnBadCoin) 1200L else 2500L

        handler.postDelayed({
            if (coinButton.visibility == View.VISIBLE) {
                coinButton.visibility = View.GONE
                resumeTimerIfNeeded()
            }
        }, displayTime)
    }

    private fun resumeTimerIfNeeded() {
        if (isGameRunning) {
            handler.removeCallbacks(timerRunnable)
            handler.post(timerRunnable)
        }
    }

    private fun playSparkleAnimation() {
        ivSparkle.visibility = View.VISIBLE
        ivSparkle.scaleX = 0.5f; ivSparkle.scaleY = 0.5f; ivSparkle.alpha = 1f
        ivSparkle.animate().scaleX(2f).scaleY(2f).alpha(0f).setDuration(600).withEndAction { ivSparkle.visibility = View.GONE }.start()
    }

    private fun winGame() {
        isGameRunning = false
        hasWon = true
        handler.removeCallbacks(timerRunnable)

        val prefs = getSharedPreferences("BazaarPrefs", MODE_PRIVATE)
        val previousBest = prefs.getInt("best_time", 9999)
        var isNewRecord = false
        if (secondsElapsed < previousBest) {
            prefs.edit().putInt("best_time", secondsElapsed).apply()
            isNewRecord = true
        }

        val rank = when {
            secondsElapsed < 45 -> "🏆 Bazaar Legend"
            secondsElapsed < 90 -> "🥇 Master Merchant"
            secondsElapsed < 150 -> "🥈 Skilled Merchant"
            else -> "🥉 Novice"
        }
        val recordText = if (isNewRecord) "\n\n🌟 NEW RECORD! 🌟" else ""

        AlertDialog.Builder(this)
            .setTitle("Grand Bazaar Reached!")
            .setMessage("Time: ${tvTimer.text}\n\nRank: $rank$recordText")
            .setCancelable(false)
            .setPositiveButton("Back to Menu") { _, _ -> 
                // FIXED: Launch menu explicitly
                startActivity(Intent(this, MenuActivity::class.java))
                finish() 
            }
            .setNegativeButton("Play Again") { _, _ -> resetGame() }
            .show()
    }

    private fun resetGame() {
        coins = 0L; currentItemIndex = 0; upgradeCost = 10L; baseTapValue = 1L
        isAdBoostActive = false; secondsElapsed = 0; isGameRunning = false; hasWon = false
        tvTimer.text = "00:00"
        itemImage.setImageResource(items[0])
        btnGoldenDeal.visibility = View.GONE; btnBadCoin.visibility = View.GONE
        updateUI()
    }

    private fun formatNumber(number: Long): String {
        return when {
            number >= 1_000_000_000 -> String.format("%.1fB", number / 1_000_000_000.0)
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    private fun updateUI() {
        tvCoinCount.text = formatNumber(coins)
        tvUpgradeCost.text = "Upgrade: ${formatNumber(upgradeCost)}"
        tvCoinCount.setTextColor(if (isAdBoostActive) 0xFFF4C430.toInt() else 0xFFF5E6D3.toInt())
    }
}
