package com.example.bazaarempire

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val player_id: String? = null,
    val player_name: String? = null,
    val contact_info: String? = null,
    val score: Int = 0,
    val user_id: String? = null
)

object SupabaseManager {
    // Correct base URL (no /rest/v1/)
    private const val SUPABASE_URL = "https://thowyglvcnplnothnrte.supabase.co"
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
            } catch (e: Exception) {
                Log.e("SupabaseAuth", "Login failed: ${e.message}")
            }
        }
    }

    suspend fun submitScore(playerId: String, playerName: String, contactInfo: String, timeInSeconds: Int) {
        val currentUser = client.auth.currentUserOrNull() ?: return
        
        try {
            val existingEntries = client.postgrest["leaderboard"].select {
                filter { eq("user_id", currentUser.id) }
            }.decodeList<LeaderboardEntry>()

            if (existingEntries.isEmpty()) {
                val entry = LeaderboardEntry(
                    player_id = playerId,
                    player_name = playerName,
                    contact_info = contactInfo,
                    score = timeInSeconds,
                    user_id = currentUser.id
                )
                client.postgrest["leaderboard"].insert(entry)
            } else {
                val existingScore = existingEntries.first().score
                
                if (timeInSeconds < existingScore) {
                    client.postgrest["leaderboard"].update({
                        set("score", timeInSeconds)
                        set("player_name", playerName)
                        set("player_id", playerId)
                        set("contact_info", contactInfo)
                    }) {
                        filter { eq("user_id", currentUser.id) }
                    }
                } else {
                    client.postgrest["leaderboard"].update({
                        set("player_name", playerName) 
                        set("player_id", playerId)
                        set("contact_info", contactInfo)
                    }) {
                        filter { eq("user_id", currentUser.id) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseSubmit", "Failed: ${e.message}")
        }
    }

    suspend fun fetchLeaderboard(): List<LeaderboardEntry>? {
        return try {
            client.postgrest["leaderboard"].select {
                order("score", Order.ASCENDING)
            }.decodeList<LeaderboardEntry>()
        } catch (e: Exception) {
            Log.e("SupabaseFetch", "Failed: ${e.message}")
            null 
        }
    }
}
