package com.rucas.hojrehturbo

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
        try {
            val config = PaymentConfiguration(localSecurityCheck = SecurityCheck.Disable)
            payment = Payment(context, config)
            connection = payment?.connect {
                connectionSucceed { onReady() }
                connectionFailed { throwable -> Log.e("Bazaar", "Connection failed: ${throwable.message}") }
                disconnected { }
            }
        } catch (e: Exception) {
            Log.e("Bazaar", "Bazaar app not installed or error: ${e.message}")
        }
    }

    fun purchaseItem(activity: Activity, productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        try {
            if (connection?.getState() != ConnectionState.Connected) {
                onFailure("Not connected to Cafe Bazaar")
                return
            }
            val request = PurchaseRequest(productId = productId, payload = "avatar_purchase")
            payment?.purchaseProduct(registry = (activity as ComponentActivity).activityResultRegistry, request = request) {
                purchaseFlowBegan { }
                failedToBeginFlow { throwable: Throwable -> onFailure(throwable.message ?: "Failed") }
                purchaseSucceed { _ -> onSuccess() }
                purchaseCanceled { onFailure("Canceled") }
                purchaseFailed { throwable: Throwable -> onFailure(throwable.message ?: "Failed") }
            }
        } catch (e: Exception) {
            onFailure(e.message ?: "Unknown Error")
        }
    }
    fun disconnect() { try { connection?.disconnect() } catch (e: Exception) {} }
}
