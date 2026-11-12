package com.example.bazaarempire

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest

class BillingManager(private val context: Context) {
    private var payment: Payment? = null
    private var connection: Connection? = null

    fun connect(onReady: () -> Unit) {
        // REPLACE WITH YOUR ACTUAL RSA PUBLIC KEY FROM BAZAAR DEV PANEL
        val rsaPublicKey = "YOUR_RSA_PUBLIC_KEY_HERE" 
        
        val config = PaymentConfiguration(
            localSecurityCheck = SecurityCheck.Enable(rsaPublicKey = rsaPublicKey)
        )
        payment = Payment(context, config)
        
        connection = payment?.connect {
            connectionSucceed {
                Log.d("BazaarBilling", "Connected successfully")
                onReady()
            }
            connectionFailed { throwable ->
                Log.e("BazaarBilling", "Connection failed: ${throwable.message}")
            }
            disconnected {
                Log.d("BazaarBilling", "Disconnected")
            }
        }
    }

    fun purchaseItem(
        activity: Activity,
        productId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (connection?.getState() != ConnectionState.Connected) {
            onFailure("Not connected to Cafe Bazaar")
            return
        }
        
        val request = PurchaseRequest(productId = productId, payload = "avatar_purchase")
        
        // Poolakey requires the ActivityResultRegistry to launch the Bazaar payment screen
        // We cast the Activity to ComponentActivity to access this
        payment?.purchaseProduct(
            registry = (activity as ComponentActivity).activityResultRegistry,
            request = request
        ) {
            purchaseFlowBegan {
                Log.d("BazaarBilling", "Purchase flow began")
            }
            failedToBeginFlow { throwable ->
                onFailure(throwable.message ?: "Failed to begin flow")
            }
            purchaseSucceed { purchaseEntity ->
                Log.d("BazaarBilling", "Purchase successful: ${purchaseEntity.productId}")
                onSuccess()
            }
            purchaseCanceled {
                onFailure("Purchase canceled by user")
            }
            purchaseFailed { throwable ->
                onFailure(throwable.message ?: "Purchase failed")
            }
        }
    }

    fun disconnect() {
        connection?.disconnect()
    }
}
