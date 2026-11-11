package com.example.bazaarempire

import android.app.AlertDialog
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
                val minutes = secondsElapsed / 60
                val seconds = secondsElapsed % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
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

            val currentTapValue = if (isAdBoostActive) baseTapValue * 2 else baseTapValue
            coins += currentTapValue
            updateUI()
            
            btnTap.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50).withEndAction {
                btnTap.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
            }.start()

            // Spawn logic: Only allow one special coin on screen at a time
            if (btnGoldenDeal.visibility == View.GONE && btnBadCoin.visibility == View.GONE) {
                val randomChance = Random.nextInt(100)
                if (randomChance < 12) { // 12% chance for Good Coin
                    spawnSpecialCoin(btnGoldenDeal)
                } else if (randomChance in 13..20) { // 8% chance for Bad Coin
                    spawnSpecialCoin(btnBadCoin)
                }
            }
        }

        // Good Coin Click
        btnGoldenDeal.setOnClickListener {
            val bonus = baseTapValue * 10
            coins += bonus
            updateUI()
            Toast.makeText(this, "Golden Deal! +$bonus", Toast.LENGTH_SHORT).show()
            btnGoldenDeal.visibility = View.GONE
            resumeTimerIfNeeded()
        }

        // Bad Coin Click
        btnBadCoin.setOnClickListener {
            val penalty = baseTapValue * 15
            coins -= penalty
            if (coins < 0) coins = 0 // Prevent negative coins
            updateUI()
            Toast.makeText(this, "Cursed Coin! -$penalty", Toast.LENGTH_SHORT).show()
            btnBadCoin.visibility = View.GONE
            resumeTimerIfNeeded()
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

                    if (currentItemIndex == items.size - 1) {
                        winGame()
                    }
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
                handler.postDelayed({
                    isAdBoostActive = false
                    updateUI()
                }, 30000) 
            }
        }

        btnShop.setOnClickListener {
            coins += 500
            Toast.makeText(this, "TEST SHOP: Added 500 coins!", Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }

    private fun spawnSpecialCoin(coinButton: ImageButton) {
        coinButton.visibility = View.VISIBLE
        val randomX = Random.nextInt(50, 800).toFloat()
        val randomY = Random.nextInt(300, 1200).toFloat()
        coinButton.x = randomX
        coinButton.y = randomY

        coinButton.scaleX = 0f
        coinButton.scaleY = 0f
        coinButton.animate().scaleX(1f).scaleY(1f).setDuration(200).start()

        // Pause timer to build tension
        handler.removeCallbacks(timerRunnable)

        // Bad coin disappears faster (1.5 seconds) than Good coin (3 seconds)
        val displayTime = if (coinButton == btnBadCoin) 1500L else 3000L

        handler.postDelayed({
            if (coinButton.visibility == View.VISIBLE) {
                coinButton.visibility = View.GONE
                resumeTimerIfNeeded()
            }
        }, displayTime)
    }

    private fun resumeTimerIfNeeded() {
        if (isGameRunning) {
            handler.removeCallbacks(timerRunnable) // clear existing to avoid stacking
            handler.post(timerRunnable)
        }
    }

    private fun playSparkleAnimation() {
        ivSparkle.visibility = View.VISIBLE
        ivSparkle.scaleX = 0.5f
        ivSparkle.scaleY = 0.5f
        ivSparkle.alpha = 1f
        ivSparkle.animate().scaleX(2f).scaleY(2f).alpha(0f).setDuration(600).withEndAction { ivSparkle.visibility = View.GONE }.start()
    }

    private fun winGame() {
        isGameRunning = false
        hasWon = true
        handler.removeCallbacks(timerRunnable)

        val rank = when {
            secondsElapsed < 45 -> "🏆 Bazaar Legend (افسانه بازار)"
            secondsElapsed < 90 -> "🥇 Master Merchant (تاجر بزرگ)"
            secondsElapsed < 150 -> "🥈 Skilled Merchant (تاجر ماهر)"
            else -> "🥉 Novice (تازه‌کار)"
        }

        AlertDialog.Builder(this)
            .setTitle("Grand Bazaar Reached!")
            .setMessage("Time: ${tvTimer.text}\n\nRank: $rank\n\nYou have mastered the Bazaar!")
            .setCancelable(false)
            .setPositiveButton("Play Again") { _, _ -> resetGame() }
            .show()
    }

    private fun resetGame() {
        coins = 0L
        currentItemIndex = 0
        upgradeCost = 10L
        baseTapValue = 1L
        isAdBoostActive = false
        secondsElapsed = 0
        isGameRunning = false
        hasWon = false
        tvTimer.text = "00:00"
        itemImage.setImageResource(items[0])
        btnGoldenDeal.visibility = View.GONE
        btnBadCoin.visibility = View.GONE
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
        if (isAdBoostActive) {
            tvCoinCount.setTextColor(0xFFF4C430.toInt()) 
        } else {
            tvCoinCount.setTextColor(0xFFF5E6D3.toInt()) 
        }
    }
}
