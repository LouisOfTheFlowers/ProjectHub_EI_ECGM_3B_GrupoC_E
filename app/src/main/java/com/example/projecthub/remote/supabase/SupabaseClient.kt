package com.example.projecthub.remote.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = "https://ypttthmsfcbexoimpjii.supabase.co",
        supabaseKey = "sb_publishable_5LkO7kRnkX83F8a-qi5UpQ_0zxGV4Lt"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}