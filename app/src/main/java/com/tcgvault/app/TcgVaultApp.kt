package com.tcgvault.app

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.tcgvault.app.data.local.TcgDatabase
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.data.repository.TcgRepository
import okhttp3.OkHttpClient

class TcgVaultApp : Application(), ImageLoaderFactory {
    val database: TcgDatabase by lazy { TcgDatabase.getInstance(this) }
    val repository: TcgRepository by lazy { TcgRepository(database.tcgDao()) }

    private val prefs by lazy { getSharedPreferences("tcg_vault_prefs", Context.MODE_PRIVATE) }

    fun getLastSelectedGame(): TcgGame {
        val saved = prefs.getString("selected_game", null)
        return saved?.let { runCatching { TcgGame.valueOf(it) }.getOrNull() } ?: TcgGame.YUGIOH
    }

    fun saveLastSelectedGame(game: TcgGame) {
        prefs.edit().putString("selected_game", game.name).apply()
    }

    fun isLightMode(): Boolean = prefs.getBoolean("light_mode", false)

    fun saveLightMode(enabled: Boolean) {
        prefs.edit().putBoolean("light_mode", enabled).apply()
    }

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "TcgVaultAndroid/1.0")
                    .header("Accept", "*/*")
                    .build()
                chain.proceed(request)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .build()
    }
}
