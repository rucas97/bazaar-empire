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
    private const val SUPABASE_URL = "https://YOUR_PROJECT_ID.supabase.co"
    private const val SUPABASE_KEY = "YOUR_PUBLISHABLE_ANON_KEY"

    // Initialize safely
    var client: SupabaseClient? = null
    init {
        try {
            client = createSupabaseClient(supabaseUrl = SUPABASE_URL, supabaseKey = SUPABASE_KEY) {
                install(Auth) { autoLoadFromStorage = true; alwaysAutoRefresh = true }
                install(Postgrest)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun ensureAnonymousLogin() {
        try {
            if (client?.auth?.currentUserOrNull() == null) {
                client?.auth?.signInAnonymously()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun submitScore(playerId: String, playerName: String, contactInfo: String, timeInSeconds: Int) {
        try {
            val currentUser = client?.auth?.currentUserOrNull() ?: return
            val entry = LeaderboardEntry(playerId, playerName, contactInfo, timeInSeconds, currentUser.id)
            client?.postgrest?.get("leaderboard")?.insert(entry)
        } catch (e: Exception) { e.printStackTrace() }
    }
}
