package com.example.bazaarempire

import android.app.Activity
import android.content.Context
import android.util.Log
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
        // REPLACE THIS with your actual RSA Public Key from the Cafe Bazaar Developer Panel
        val rsaPublicKey = "YOUR_RSA_PUBLIC_KEY_HERE" 
        
        val config = PaymentConfiguration(
            localSecurityCheck = SecurityCheck.Enable(rsaPublicKey = rsaPublicKey)
        )
        payment = Payment(context, config)
        connection = payment?.connect {
            if (it == ConnectionState.Connected) {
                Log.d("BazaarBilling", "Connected successfully")
                onReady()
            } else {
                Log.e("BazaarBilling", "Connection failed: ${it.name}")
            }
        }
    }

    fun purchaseItem(activity: Activity, productId: String, onSuccess: () -> Unit) {
        if (connection == null) {
            Log.e("BazaarBilling", "Not connected to Bazaar")
            return
        }
        
        val request = PurchaseRequest(productId = productId, payload = "avatar_purchase")
        payment?.purchaseProduct(connection!!, request) {
            if (it.isSuccess) {
                Log.d("BazaarBilling", "Purchase successful: ${it.data?.productId}")
                onSuccess()
            } else {
                Log.e("BazaarBilling", "Purchase failed: ${it.errorMessage}")
            }
        }
    }

    fun disconnect() {
        connection?.disconnect()
    }
}
