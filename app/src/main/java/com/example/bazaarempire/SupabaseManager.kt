package com.example.bazaarempire

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val player_id: String,
    val player_name: String,
    val contact_info: String,
    val score: Int,
    val user_id: String
)

object SupabaseManager {
    // ==========================================
    // PASTE YOUR SUPABASE URL AND KEY HERE:
    // ==========================================
    private const val SUPABASE_URL = "https://thowyglvcnplnothnrte.supabase.co/rest/v1/"
    private const val SUPABASE_KEY = "sb_publishable_4lCy9HUyRfMH1DmLhGxOrQ_zaP_IEKj"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) {
            autoLoadFromStorage = true
            alwaysAutoRefresh = true
        }
        install(Postgrest)
    }

    suspend fun ensureAnonymousLogin() {
        if (client.auth.currentUserOrNull() == null) {
            try {
                client.auth.signInAnonymously()
                Log.d("SupabaseDebug", "Anonymous login SUCCESS")
            } catch (e: Exception) {
                Log.e("SupabaseDebug", "Anonymous login FAILED: ${e.message}")
            }
        }
    }

    suspend fun submitScore(playerId: String, playerName: String, contactInfo: String, timeInSeconds: Int) {
        val currentUser = client.auth.currentUserOrNull()
        if (currentUser == null) {
            Log.e("SupabaseDebug", "Submit FAILED: User is null.")
            return
        }
        
        val entry = LeaderboardEntry(
            player_id = playerId,
            player_name = playerName,
            contact_info = contactInfo,
            score = timeInSeconds,
            user_id = currentUser.id
        )
        
        try {
            client.postgrest["leaderboard"].insert(entry)
            Log.d("SupabaseDebug", "Score SUBMITTED: $timeInSeconds")
        } catch (e: Exception) {
            Log.e("SupabaseDebug", "Submit FAILED: ${e.message}")
        }
    }
}
