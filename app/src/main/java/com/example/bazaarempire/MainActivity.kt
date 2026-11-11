package com.example.bazaarempire

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var coins = 0
    private var currentItemIndex = 0
    private var upgradeCost = 10
    private var tapValue = 1

    private lateinit var tvCoinCount: TextView
    private lateinit var tvUpgradeCost: TextView
    private lateinit var btnTap: ImageButton
    private lateinit var btnUpgrade: ImageButton
    private lateinit var itemImage: ImageView

    private val items = listOf(
        R.drawable.item_tea,
        R.drawable.item_spices,
        R.drawable.item_pistachios,
        R.drawable.item_copper,
        R.drawable.item_saffron,
        R.drawable.item_carpet
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCoinCount = findViewById(R.id.tvCoinCount)
        tvUpgradeCost = findViewById(R.id.tvUpgradeCost)
        btnTap = findViewById(R.id.btnTap)
        btnUpgrade = findViewById(R.id.btnUpgrade)
        itemImage = findViewById(R.id.itemImage)

        updateUI()

        btnTap.setOnClickListener {
            coins += tapValue
            tvCoinCount.text = coins.toString()
            
            // Simple scale animation for juice
            btnTap.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50).withEndAction {
                btnTap.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
            }.start()
        }

        btnUpgrade.setOnClickListener {
            if (coins >= upgradeCost) {
                coins -= upgradeCost
                upgradeCost = (upgradeCost * 1.5).toInt()
                tapValue += 1

                if (currentItemIndex < items.size - 1) {
                    currentItemIndex++
                    itemImage.setImageResource(items[currentItemIndex])
                }

                updateUI()
            }
        }
    }

    private fun updateUI() {
        tvCoinCount.text = coins.toString()
        tvUpgradeCost.text = "Upgrade: $upgradeCost"
    }
}
