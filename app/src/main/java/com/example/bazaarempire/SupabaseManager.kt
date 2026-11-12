package com.example.bazaarempire

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
    // HARDCODED WITH YOUR URL (Base URL only, no /rest/v1/)
    private const val SUPABASE_URL = "https://thowyglvcnplnothnrte.supabase.co" 
    private const val SUPABASE_KEY = "sb_publishable_4lCy9HUyRfMH1DmLhGxOrQ_zaP_IEKj"

    var client: SupabaseClient? = null
    
    init {
        try {
            client = createSupabaseClient(supabaseUrl = SUPABASE_URL, supabaseKey = SUPABASE_KEY) {
                install(Auth) { 
                    autoLoadFromStorage = true
                    alwaysAutoRefresh = true 
                }
                install(Postgrest)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun ensureAnonymousLogin(): String {
        return try {
            if (client?.auth?.currentUserOrNull() == null) {
                client?.auth?.signInAnonymously()
            }
            "Success: Logged in anonymously"
        } catch (e: Exception) { 
            "Login Error: ${e.message}" 
        }
    }

    suspend fun submitScore(playerId: String, playerName: String, contactInfo: String, timeInSeconds: Int): String {
        return try {
            val currentUser = client?.auth?.currentUserOrNull() ?: return "Error: Still not logged in"
            val entry = LeaderboardEntry(playerId, playerName, contactInfo, timeInSeconds, currentUser.id)
            client?.postgrest?.get("leaderboard")?.insert(entry)
            "Success: Score Sent!"
        } catch (e: Exception) { 
            "Supabase Error: ${e.message}" 
        }
    }
}
