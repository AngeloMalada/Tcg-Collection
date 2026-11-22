package com.tcgvault.app.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TcgGame(val displayName: String, val shortName: String) {
    YUGIOH("Yu-Gi-Oh!", "YGO"),
    MTG("Magic: The Gathering", "MTG")
}

object ReleaseDateFilter {
    fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun isReleased(releaseDate: String, todayIso: String = todayIso()): Boolean {
        val trimmed = releaseDate.trim()
        if (trimmed.length < 10) return false
        return trimmed.take(10) <= todayIso
    }
}

object SetCategoryClassifier {
    const val PRECONS_CATEGORY = "Precons"
    const val UNIVERSES_BEYOND_CATEGORY = "Universes Beyond"

    private val UB_SET_CODES = setOf(
        "LTR", "LTC", "WHO", "PIP", "ACR", "40K", "BOT", "REX",
        "FIN", "FIC", "FCA", "SPM", "SPE", "MAR", "MTC", "MSH",
        "TLA", "TLE", "TMT", "TMC", "SLX"
    )

    private val UB_KEYWORDS = listOf(
        "lord of the rings", "middle-earth", "fallout", "doctor who",
        "assassin's creed", "warhammer", "final fantasy", "marvel",
        "spider-man", "avengers", "avatar: the last airbender",
        "jurassic", "transformers", "teenage mutant ninja turtles",
        "star trek", "walking dead", "stranger things", "street fighter",
        "fortnite", "princess bride", "monty python", "hatsune miku",
        "tomb raider", "ghostbusters", "chucky", "spongebob", "sonic",
        "universes beyond"
    )

    fun defaultCategory(game: TcgGame): String = when (game) {
        TcgGame.YUGIOH -> "Booster Sets"
        TcgGame.MTG -> "Expansions"
    }

    fun isUniversesBeyond(game: TcgGame, code: String, name: String): Boolean {
        if (game != TcgGame.MTG) return false
        val upperCode = code.trim().uppercase(Locale.US)
        if (upperCode in UB_SET_CODES) return true
        val lowerName = name.trim().lowercase(Locale.US)
        return UB_KEYWORDS.any { lowerName.contains(it) }
    }

    fun yugiohPreconType(setName: String): String? {
        val name = setName.trim().lowercase(Locale.US)
        if (name.contains("deck build pack")) return null
        return when {
            name.contains("structure deck") -> "Structure Deck"
            name.contains("starter deck") || name.contains("starter set") -> "Starter Deck"
            name.contains("legendary") && name.contains("deck") -> "Legendary Decks"
            name.contains("egyptian god deck") ||
                name.contains("tactical-try deck") ||
                name.contains("chronicle deck") -> "Preconstructed Deck"
            else -> null
        }
    }

    fun categoryFor(game: TcgGame, setType: String, setName: String): String {
        val type = setType.trim().lowercase(Locale.US)
        val name = setName.trim().lowercase(Locale.US)
        return when (game) {
            TcgGame.MTG -> when (type) {
                "expansion" -> "Expansions"
                "core" -> "Core Sets"
                "commander" -> "Commander"
                "masters", "draft_innovation" -> "Masters & Horizons"
                "starter", "duel_deck", "box", "arsenal", "planechase", "archenemy" -> "Starter & Precons"
                else -> "Special & Promos"
            }

            TcgGame.YUGIOH -> when {
                yugiohPreconType(setName) != null ||
                    type.contains("structure") ||
                    type.contains("starter") ||
                    type.contains("legendary deck") ||
                    type.contains("preconstructed") -> "Structure & Starter Decks"
                type.contains("speed duel") || name.contains("speed duel") -> "Speed Duel"
                type.contains("tin") || type.contains("reprint") ||
                    name.contains("tin") || name.contains("rarity collection") ||
                    name.contains("battles of legend") || name.contains("gold") ||
                    name.contains("mega pack") -> "Tins & Reprint Sets"
                name.contains("ots tournament") || name.contains("astral pack") ||
                    name.contains("turbo pack") || name.contains("promo") ||
                    name.contains("sneak peek") -> "Tournament & Promos"
                else -> "Booster Sets"
            }
        }
    }

    fun categoryOrder(game: TcgGame): List<String> = when (game) {
        TcgGame.MTG -> listOf(
            "Expansions",
            "Commander",
            "Core Sets",
            "Masters & Horizons",
            "Starter & Precons",
            "Special & Promos"
        )

        TcgGame.YUGIOH -> listOf(
            "Booster Sets",
            "Structure & Starter Decks",
            "Tins & Reprint Sets",
            "Speed Duel",
            "Tournament & Promos"
        )
    }
}
