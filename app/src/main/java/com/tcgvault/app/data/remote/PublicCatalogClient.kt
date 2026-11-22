package com.tcgvault.app.data.remote

import com.tcgvault.app.data.local.entity.PreconDeckEntity
import com.tcgvault.app.data.local.entity.TcgCardEntity
import com.tcgvault.app.data.local.entity.TcgSetEntity
import com.tcgvault.app.data.model.ReleaseDateFilter
import com.tcgvault.app.data.model.SetCategoryClassifier
import com.tcgvault.app.data.model.TcgGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Fetches read-only public card & set catalog metadata directly from:
 * - Scryfall API (Magic: The Gathering)
 * - YGOPRODeck API (Yu-Gi-Oh!)
 *
 * No custom backend or user account is used; all fetched catalog data and user
 * collection state are persisted strictly in local Room storage.
 */
class PublicCatalogClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchAllMtgSets(): List<TcgSetEntity> =
        withContext(Dispatchers.IO) {
            val body = get("https://api.scryfall.com/sets")
            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"]?.jsonArray ?: JsonArray(emptyList())

            val sets = mutableListOf<TcgSetEntity>()
            val todayIso = ReleaseDateFilter.todayIso()

            for (element in data) {
                val obj = element.jsonObject
                val digital = obj["digital"]?.jsonPrimitive?.booleanOrNull ?: false
                if (digital) continue
                val cardCount = obj["card_count"]?.jsonPrimitive?.intOrNull ?: 0
                if (cardCount <= 0) continue

                val code = obj["code"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: continue
                val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: continue
                val releasedAt = obj["released_at"]?.jsonPrimitive?.contentOrNull ?: continue
                if (!ReleaseDateFilter.isReleased(releasedAt, todayIso)) continue
                val setType = obj["set_type"]?.jsonPrimitive?.contentOrNull ?: "expansion"
                val iconUri = obj["icon_svg_uri"]?.jsonPrimitive?.contentOrNull

                sets.add(
                    TcgSetEntity(
                        id = "mtg:$code",
                        game = TcgGame.MTG,
                        code = code.uppercase(),
                        name = name,
                        releaseDate = releasedAt,
                        cardCount = cardCount,
                        setType = setType,
                        iconUrl = iconUri,
                        cardsLoaded = false
                    )
                )
            }
            sets
        }

    /**
     * Fetches actual individual Magic: The Gathering preconstructed decks (Commander precons
     * like "Avengers Assemble", Starter Kits, Challenger Decks, Duel Decks, etc.) from
     * MTGJSON's public DeckList.json catalog.
     */
    suspend fun fetchAllMtgPrecons(): List<PreconDeckEntity> =
        withContext(Dispatchers.IO) {
            val body = get("https://mtgjson.com/api/v5/DeckList.json")
            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"]?.jsonArray ?: JsonArray(emptyList())

            val precons = mutableListOf<PreconDeckEntity>()
            val todayIso = ReleaseDateFilter.todayIso()

            for (element in data) {
                val obj = element.jsonObject
                val fileName = obj["fileName"]?.jsonPrimitive?.contentOrNull ?: continue
                val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: continue
                val code = obj["code"]?.jsonPrimitive?.contentOrNull?.uppercase() ?: continue
                val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: "Precon Deck"
                val releaseDate = obj["releaseDate"]?.jsonPrimitive?.contentOrNull ?: continue
                if (!ReleaseDateFilter.isReleased(releaseDate, todayIso)) continue

                // Skip purely digital Arena/MTGO decks
                if (type.contains("Arena", ignoreCase = true) || type.contains("MTGO", ignoreCase = true)) {
                    continue
                }

                val year = releaseDate.take(4).toIntOrNull() ?: 2024
                precons.add(
                    PreconDeckEntity(
                        id = "precon:mtg:deck:$fileName",
                        game = TcgGame.MTG,
                        name = name,
                        category = type,
                        releaseYear = year,
                        setCode = code,
                        description = "Official Magic: The Gathering $type ($code • $releaseDate).",
                        deckFileName = fileName
                    )
                )
            }
            precons
        }

    /**
     * Downloads an individual MTG precon decklist from MTGJSON (commander + mainBoard)
     * with exact card counts and Scryfall artwork URLs.
     */
    suspend fun fetchMtgPreconDeckCards(precon: PreconDeckEntity): List<Pair<TcgCardEntity, Int>> =
        withContext(Dispatchers.IO) {
            val fileName = precon.deckFileName ?: return@withContext emptyList()
            val encodedFile = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
            val body = get("https://mtgjson.com/api/v5/decks/$encodedFile.json")
            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"]?.jsonObject ?: return@withContext emptyList()

            val combined = LinkedHashMap<String, Pair<TcgCardEntity, Int>>()
            val sections = listOf("commander", "mainBoard", "sideBoard")

            for (section in sections) {
                val arr = data[section]?.jsonArray ?: continue
                for (element in arr) {
                    val obj = element.jsonObject
                    val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: continue
                    val count = obj["count"]?.jsonPrimitive?.intOrNull?.coerceAtLeast(1) ?: 1
                    val cardSetCode = obj["setCode"]?.jsonPrimitive?.contentOrNull?.lowercase()
                        ?: precon.setCode.lowercase()
                    val number = obj["number"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val rarity = obj["rarity"]?.jsonPrimitive?.contentOrNull
                        ?.replaceFirstChar { it.uppercase() } ?: "Common"
                    val typeLine = obj["type"]?.jsonPrimitive?.contentOrNull ?: "Card"
                    val manaCost = obj["manaCost"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val power = obj["power"]?.jsonPrimitive?.contentOrNull
                    val toughness = obj["toughness"]?.jsonPrimitive?.contentOrNull
                    val stats = buildString {
                        if (manaCost.isNotBlank()) append(manaCost)
                        if (power != null && toughness != null) {
                            if (isNotEmpty()) append(" • ")
                            append("$power/$toughness")
                        }
                    }
                    val text = obj["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val scryfallId = obj["identifiers"]?.jsonObject
                        ?.get("scryfallId")?.jsonPrimitive?.contentOrNull
                    val uuid = obj["uuid"]?.jsonPrimitive?.contentOrNull ?: "${cardSetCode}_$number"

                    val cardId = if (!scryfallId.isNullOrBlank()) {
                        "mtg:$cardSetCode:$scryfallId"
                    } else {
                        "mtg:$cardSetCode:$uuid"
                    }

                    val imageUrl = if (!scryfallId.isNullOrBlank()) {
                        "https://api.scryfall.com/cards/$scryfallId?format=image&version=normal"
                    } else {
                        val encName = URLEncoder.encode(name, "UTF-8")
                        "https://api.scryfall.com/cards/named?exact=$encName&set=$cardSetCode&format=image&version=normal"
                    }

                    val entity = TcgCardEntity(
                        id = cardId,
                        setId = "mtg:$cardSetCode",
                        game = TcgGame.MTG,
                        name = name,
                        collectorNumber = number.padStart(3, '0'),
                        rarity = rarity,
                        cardType = typeLine,
                        manaOrStats = stats,
                        description = text,
                        imageUrl = imageUrl
                    )

                    val existing = combined[cardId]
                    if (existing != null) {
                        combined[cardId] = existing.first to (existing.second + count)
                    } else {
                        combined[cardId] = entity to count
                    }
                }
            }
            combined.values.toList()
        }

    suspend fun fetchAllYugiohSets(): Pair<List<TcgSetEntity>, List<PreconDeckEntity>> =
        withContext(Dispatchers.IO) {
            val body = get("https://db.ygoprodeck.com/api/v7/cardsets.php")
            val array = json.parseToJsonElement(body).jsonArray

            val sets = mutableListOf<TcgSetEntity>()
            val precons = mutableListOf<PreconDeckEntity>()
            val todayIso = ReleaseDateFilter.todayIso()

            for (element in array) {
                val obj = element.jsonObject
                val rawSetName = obj["set_name"]?.jsonPrimitive?.contentOrNull ?: continue
                val setName = decodeHtmlEntities(rawSetName)
                val setCode = obj["set_code"]?.jsonPrimitive?.contentOrNull?.uppercase() ?: continue
                val numOfCards = obj["num_of_cards"]?.jsonPrimitive?.intOrNull ?: 0
                if (numOfCards <= 0) continue
                val tcgDate = obj["tcg_date"]?.jsonPrimitive?.contentOrNull ?: continue
                if (!ReleaseDateFilter.isReleased(tcgDate, todayIso)) continue

                val preconType = SetCategoryClassifier.yugiohPreconType(setName)
                val setType = when {
                    preconType != null -> preconType
                    setName.contains("Speed Duel", ignoreCase = true) -> "Speed Duel"
                    setName.contains("Tin", ignoreCase = true) -> "Tin"
                    else -> "Booster Set"
                }

                sets.add(
                    TcgSetEntity(
                        id = "ygo:$setCode",
                        game = TcgGame.YUGIOH,
                        code = setCode,
                        name = setName,
                        releaseDate = tcgDate,
                        cardCount = numOfCards,
                        setType = setType,
                        iconUrl = null,
                        cardsLoaded = false
                    )
                )

                if (preconType != null) {
                    val year = tcgDate.take(4).toIntOrNull() ?: 2024
                    precons.add(
                        PreconDeckEntity(
                            id = "precon:ygo:set:$setCode",
                            game = TcgGame.YUGIOH,
                            name = setName,
                            category = preconType,
                            releaseYear = year,
                            setCode = setCode,
                            description = "Official Yu-Gi-Oh! $preconType ($numOfCards cards)."
                        )
                    )
                }
            }
            sets to precons
        }

    suspend fun fetchMtgCardsForSet(set: TcgSetEntity): List<TcgCardEntity> =
        fetchMtgCardsByQuery(
            setId = set.id,
            setCode = set.code,
            scryfallQuery = "set:${set.code.lowercase()}"
        )

    suspend fun fetchMtgCardsByQuery(
        setId: String,
        setCode: String,
        scryfallQuery: String
    ): List<TcgCardEntity> =
        withContext(Dispatchers.IO) {
            val cards = mutableListOf<TcgCardEntity>()
            val encodedQuery = URLEncoder.encode(scryfallQuery, "UTF-8")
            var nextUrl: String? =
                "https://api.scryfall.com/cards/search?q=$encodedQuery&unique=prints&order=set"

            var pages = 0
            while (nextUrl != null && pages < 10) {
                pages++
                val body = get(nextUrl)
                val root = json.parseToJsonElement(body).jsonObject
                val data = root["data"]?.jsonArray ?: break

                for (element in data) {
                    val obj = element.jsonObject
                    val scryfallId = obj["id"]?.jsonPrimitive?.contentOrNull ?: continue
                    val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: continue
                    val collectorNumber = obj["collector_number"]?.jsonPrimitive?.contentOrNull ?: ""
                    val rarity = obj["rarity"]?.jsonPrimitive?.contentOrNull
                        ?.replaceFirstChar { it.uppercase() } ?: "Common"
                    val typeLine = obj["type_line"]?.jsonPrimitive?.contentOrNull ?: "Card"
                    val manaCost = obj["mana_cost"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val power = obj["power"]?.jsonPrimitive?.contentOrNull
                    val toughness = obj["toughness"]?.jsonPrimitive?.contentOrNull
                    val stats = buildString {
                        if (manaCost.isNotBlank()) append(manaCost)
                        if (power != null && toughness != null) {
                            if (isNotEmpty()) append(" • ")
                            append("$power/$toughness")
                        }
                    }
                    val oracleText = obj["oracle_text"]?.jsonPrimitive?.contentOrNull
                        ?: extractFirstFaceField(obj, "oracle_text").orEmpty()
                    val imageUrl = extractMtgImageUrl(obj)

                    cards.add(
                        TcgCardEntity(
                            id = "mtg:${setCode.lowercase()}:$scryfallId",
                            setId = setId,
                            game = TcgGame.MTG,
                            name = name,
                            collectorNumber = collectorNumber.padStart(3, '0'),
                            rarity = rarity,
                            cardType = typeLine,
                            manaOrStats = stats,
                            description = oracleText,
                            imageUrl = imageUrl
                        )
                    )
                }

                val hasMore = root["has_more"]?.jsonPrimitive?.booleanOrNull ?: false
                nextUrl = if (hasMore) root["next_page"]?.jsonPrimitive?.contentOrNull else null
            }
            cards
        }

    suspend fun fetchYugiohCardsForSet(set: TcgSetEntity): List<TcgCardEntity> =
        withContext(Dispatchers.IO) {
            val encodedName = URLEncoder.encode(set.name, "UTF-8")
            val body = runCatching {
                get("https://db.ygoprodeck.com/api/v7/cardinfo.php?cardset=$encodedName")
            }.recoverCatching { err ->
                if (set.name.contains("'")) {
                    val htmlEncoded = URLEncoder.encode(set.name.replace("'", "&apos;"), "UTF-8")
                    get("https://db.ygoprodeck.com/api/v7/cardinfo.php?cardset=$htmlEncoded")
                } else {
                    throw err
                }
            }.getOrThrow()
            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"]?.jsonArray ?: return@withContext emptyList()

            val cards = mutableListOf<TcgCardEntity>()
            for (element in data) {
                val obj = element.jsonObject
                val cardIdNum = obj["id"]?.jsonPrimitive?.contentOrNull ?: continue
                val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: continue
                val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: "Card"
                val desc = obj["desc"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val atk = obj["atk"]?.jsonPrimitive?.intOrNull
                val def = obj["def"]?.jsonPrimitive?.intOrNull
                val level = obj["level"]?.jsonPrimitive?.intOrNull

                val stats = buildString {
                    if (atk != null) append("ATK $atk")
                    if (def != null) {
                        if (isNotEmpty()) append(" / ")
                        append("DEF $def")
                    }
                    if (level != null) {
                        if (isNotEmpty()) append(" • ")
                        append("LV $level")
                    }
                }

                val matchingSetInfo = obj["card_sets"]?.jsonArray?.firstOrNull {
                    val candidate = it.jsonObject["set_name"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    decodeHtmlEntities(candidate).equals(set.name, ignoreCase = true)
                }?.jsonObject

                val collectorNumber = matchingSetInfo?.get("set_code")?.jsonPrimitive?.contentOrNull
                    ?: "${set.code}-$cardIdNum"
                val rarity = matchingSetInfo?.get("set_rarity")?.jsonPrimitive?.contentOrNull ?: "Common"
                val imageUrl = obj["card_images"]?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("image_url_small")?.jsonPrimitive?.contentOrNull
                    ?: obj["card_images"]?.jsonArray?.firstOrNull()
                        ?.jsonObject?.get("image_url")?.jsonPrimitive?.contentOrNull

                cards.add(
                    TcgCardEntity(
                        id = "ygo:${set.code}:$cardIdNum",
                        setId = set.id,
                        game = TcgGame.YUGIOH,
                        name = name,
                        collectorNumber = collectorNumber,
                        rarity = rarity,
                        cardType = type,
                        manaOrStats = stats,
                        description = desc,
                        imageUrl = imageUrl
                    )
                )
            }
            cards
        }

    private fun extractMtgImageUrl(obj: JsonObject): String? {
        val direct = obj["image_uris"]?.jsonObject?.get("normal")?.jsonPrimitive?.contentOrNull
            ?: obj["image_uris"]?.jsonObject?.get("small")?.jsonPrimitive?.contentOrNull
        if (direct != null) return direct
        return obj["card_faces"]?.jsonArray?.firstOrNull()?.jsonObject
            ?.get("image_uris")?.jsonObject?.get("normal")?.jsonPrimitive?.contentOrNull
    }

    private fun extractFirstFaceField(obj: JsonObject, field: String): String? {
        return obj["card_faces"]?.jsonArray?.firstOrNull()?.jsonObject
            ?.get(field)?.jsonPrimitive?.contentOrNull
    }

    private fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "TcgVaultAndroid/1.0")
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Catalog request failed (${response.code}) for $url")
            }
            return response.body?.string().orEmpty()
        }
    }

    companion object {
        fun decodeHtmlEntities(raw: String): String = raw
            .replace("&apos;", "'")
            .replace("&#039;", "'")
            .replace("&#39;", "'")
            .replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
    }
}
