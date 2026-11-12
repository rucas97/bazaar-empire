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
    val player_name: String,
    val score: Int,
    val user_id: String
)

object SupabaseManager {
    private const val SUPABASE_URL = "https://YOUR_PROJECT_ID.supabase.co"
    private const val SUPABASE_KEY = "YOUR_PUBLISHABLE_ANON_KEY"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) {
            autoLoadFromStorage = true
            alwaysAutoRefresh = true // Fixed property name
        }
        install(Postgrest)
    }

    suspend fun ensureAnonymousLogin() {
        if (client.auth.currentUserOrNull() == null) {
            try {
                client.auth.signInAnonymously()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun submitScore(playerName: String, timeInSeconds: Int) {
        val currentUser = client.auth.currentUserOrNull() ?: return
        val entry = LeaderboardEntry(
            player_name = playerName,
            score = timeInSeconds,
            user_id = currentUser.id
        )
        try {
            client.postgrest["leaderboard"].insert(entry)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
