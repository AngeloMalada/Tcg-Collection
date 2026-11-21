package com.tcgvault.app.data.repository

import com.tcgvault.app.data.local.dao.TcgDao
import com.tcgvault.app.data.local.entity.PreconCardEntity
import com.tcgvault.app.data.local.entity.PreconDeckEntity
import com.tcgvault.app.data.local.entity.TcgCardEntity
import com.tcgvault.app.data.local.entity.TcgSetEntity
import com.tcgvault.app.data.local.model.CardWithCollection
import com.tcgvault.app.data.local.model.CollectionStats
import com.tcgvault.app.data.local.model.PreconCardDetail
import com.tcgvault.app.data.local.model.PreconWithSummary
import com.tcgvault.app.data.local.model.SetWithProgress
import com.tcgvault.app.data.local.seed.OfflineSeedCatalog
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.data.remote.PublicCatalogClient
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TcgRepository(
    private val dao: TcgDao,
    private val catalogClient: PublicCatalogClient = PublicCatalogClient()
) {
    suspend fun ensureSeeded() {
        if (dao.getSetCount() == 0) {
            dao.insertSetsIgnore(OfflineSeedCatalog.initialSets)
            dao.insertCardsIgnore(OfflineSeedCatalog.initialCards)
        } else {
            // Backfill artwork URLs for previously seeded cards without touching user collection data
            for (card in OfflineSeedCatalog.initialCards) {
                val url = card.imageUrl
                if (!url.isNullOrBlank()) {
                    dao.updateCardImageIfMissing(card.id, url)
                }
            }
        }
        if (dao.getPreconCount() == 0) {
            dao.insertPreconsIgnore(OfflineSeedCatalog.initialPrecons)
            dao.insertPreconCards(OfflineSeedCatalog.initialPreconCards)
        } else {
            // Ensure newly added official precons / boxed collections appear without touching user data
            dao.insertPreconsIgnore(OfflineSeedCatalog.initialPrecons)
            // Upgrade Foundations Starter Collection to the full MTGJSON StarterCollection_FDN decklist
            val upgraded = dao.setPreconDeckFileNameIfMissing(
                "precon:mtg:fdn-starter-collection",
                "StarterCollection_FDN"
            )
            if (upgraded > 0) {
                dao.clearPreconCards("precon:mtg:fdn-starter-collection")
            }
        }
        dao.pruneReplacedMtgSeedStubs()
    }

    fun observeSets(game: TcgGame?, query: String): Flow<List<SetWithProgress>> =
        dao.observeSetsWithProgress(game, query.trim())

    fun observeSet(setId: String): Flow<SetWithProgress?> =
        dao.observeSetWithProgress(setId)

    fun observeCardsInSet(setId: String, query: String): Flow<List<CardWithCollection>> =
        dao.observeCardsInSet(setId, query.trim())

    fun observeOwnedCards(game: TcgGame?, query: String): Flow<List<CardWithCollection>> =
        dao.observeOwnedCards(game, query.trim())

    fun observeCollectionStats(): Flow<CollectionStats> =
        dao.observeCollectionStats()

    fun observePrecons(game: TcgGame?, query: String): Flow<List<PreconWithSummary>> =
        dao.observePrecons(game, query.trim())

    fun observePreconCards(preconId: String): Flow<List<PreconCardDetail>> =
        dao.observePreconCardDetails(preconId)

    suspend fun toggleOrAddCard(cardId: String) {
        val current = dao.getCollectionEntry(cardId)?.quantity ?: 0
        if (current == 0) {
            dao.setCardQuantity(cardId, 1)
        } else {
            dao.adjustCardQuantity(cardId, 1)
        }
    }

    suspend fun adjustCardQuantity(cardId: String, delta: Int) {
        dao.adjustCardQuantity(cardId, delta)
    }

    suspend fun setCardQuantity(cardId: String, quantity: Int) {
        dao.setCardQuantity(cardId, quantity)
    }

    suspend fun toggleSetFavorite(setId: String) {
        dao.toggleSetFavorite(setId)
    }

    suspend fun togglePreconFavorite(preconId: String) {
        dao.togglePreconFavorite(preconId)
    }

    suspend fun toggleCardFavorite(cardId: String) {
        dao.toggleCardFavorite(cardId)
    }

    /**
     * Syncs all official Yu-Gi-Oh! (YGOPRODeck) and Magic: The Gathering (Scryfall + MTGJSON
     * individual precon decks) directly into local Room storage.
     */
    suspend fun syncAllCatalogSets(): Result<Int> = runCatching {
        var totalInserted = 0
        val mtgSets = catalogClient.fetchAllMtgSets()
        dao.insertSetsIgnore(mtgSets)
        for (set in mtgSets) {
            dao.refreshSetMetadata(
                setId = set.id,
                name = set.name,
                releaseDate = set.releaseDate,
                officialCardCount = set.cardCount,
                setType = set.setType,
                iconUrl = set.iconUrl
            )
        }
        totalInserted += mtgSets.size

        // Replace legacy whole-set MTG precons with actual individual precon decks from MTGJSON
        dao.deleteUnownedLegacyMtgSetPrecons()
        val mtgPrecons = runCatching { catalogClient.fetchAllMtgPrecons() }.getOrDefault(emptyList())
        if (mtgPrecons.isNotEmpty()) {
            dao.insertPreconsIgnore(mtgPrecons)
        }

        val (ygoSets, ygoPrecons) = catalogClient.fetchAllYugiohSets()
        dao.insertSetsIgnore(ygoSets)
        for (set in ygoSets) {
            dao.refreshSetMetadata(
                setId = set.id,
                name = set.name,
                releaseDate = set.releaseDate,
                officialCardCount = set.cardCount,
                setType = set.setType,
                iconUrl = set.iconUrl
            )
        }
        dao.insertPreconsIgnore(ygoPrecons)
        totalInserted += ygoSets.size

        totalInserted
    }

    /**
     * Ensures cards for a specific set are stored locally in Room.
     * If not yet cached locally, downloads them once from Scryfall / YGOPRODeck.
     */
    suspend fun ensureSetCardsLoaded(setId: String): Result<Int> = runCatching {
        val set = dao.getSetById(setId) ?: return@runCatching 0
        val existingCount = dao.getCardCountForSet(setId)
        if (set.cardsLoaded && existingCount > 0) {
            return@runCatching existingCount
        }
        val fetched = when (set.game) {
            TcgGame.MTG -> catalogClient.fetchMtgCardsForSet(set)
            TcgGame.YUGIOH -> catalogClient.fetchYugiohCardsForSet(set)
        }
        if (fetched.isNotEmpty()) {
            dao.upsertCards(fetched)
            dao.pruneReplacedMtgSeedStubs()
            dao.markSetCardsLoaded(setId, fetched.size)
        }
        fetched.size
    }

    /**
     * Ensures a precon's decklist cards are fetched and cached locally in Room.
     */
    suspend fun ensurePreconCardsLoaded(preconId: String): Result<Unit> = runCatching {
        val precon = dao.getPreconById(preconId) ?: return@runCatching
        val existingDeckCards = dao.getPreconCards(preconId)
        if (existingDeckCards.isNotEmpty() || precon.isCustom) return@runCatching

        val matchingSetId = when (precon.game) {
            TcgGame.MTG -> "mtg:${precon.setCode.lowercase()}"
            TcgGame.YUGIOH -> "ygo:${precon.setCode.uppercase()}"
        }
        val specialQuery = SPECIAL_MTG_PRODUCT_QUERIES[preconId]

        when {
            !precon.deckFileName.isNullOrBlank() -> {
                val deckCards = catalogClient.fetchMtgPreconDeckCards(precon)
                if (deckCards.isNotEmpty()) {
                    // Ensure parent sets exist for every card so CollectionScreen joins succeed
                    val missingSetIds = deckCards.map { it.first.setId }.distinct()
                    val placeholderSets = missingSetIds.map { sid ->
                        val code = sid.substringAfter("mtg:").uppercase()
                        TcgSetEntity(
                            id = sid,
                            game = TcgGame.MTG,
                            code = code,
                            name = precon.name,
                            releaseDate = "${precon.releaseYear}-01-01",
                            cardCount = deckCards.size,
                            setType = "commander",
                            cardsLoaded = false
                        )
                    }
                    dao.insertSetsIgnore(placeholderSets)
                    dao.upsertCards(deckCards.map { it.first })
                    dao.pruneReplacedMtgSeedStubs()
                    dao.insertPreconCards(
                        deckCards.map { (card, qty) ->
                            PreconCardEntity(
                                preconId = preconId,
                                cardId = card.id,
                                cardName = card.name,
                                quantityInDeck = qty
                            )
                        }
                    )
                }
            }

            specialQuery != null -> {
                val fetchedCards = catalogClient.fetchMtgCardsByQuery(
                    setId = matchingSetId,
                    setCode = precon.setCode,
                    scryfallQuery = specialQuery
                )
                if (fetchedCards.isNotEmpty()) {
                    dao.upsertCards(fetchedCards)
                    dao.ensureSetCardCountAtLeast(matchingSetId, dao.getCardCountForSet(matchingSetId))
                    dao.insertPreconCards(
                        fetchedCards.map {
                            PreconCardEntity(
                                preconId = preconId,
                                cardId = it.id,
                                cardName = it.name,
                                quantityInDeck = 1
                            )
                        }
                    )
                }
            }

            else -> {
                val set = dao.getSetById(matchingSetId)
                val fetchedCards = if (set != null) {
                    when (set.game) {
                        TcgGame.MTG -> catalogClient.fetchMtgCardsForSet(set)
                        TcgGame.YUGIOH -> catalogClient.fetchYugiohCardsForSet(set)
                    }
                } else {
                    emptyList()
                }
                if (fetchedCards.isNotEmpty()) {
                    dao.upsertCards(fetchedCards)
                    dao.markSetCardsLoaded(matchingSetId, fetchedCards.size)
                    dao.insertPreconCards(
                        fetchedCards.map {
                            PreconCardEntity(
                                preconId = preconId,
                                cardId = it.id,
                                cardName = it.name,
                                quantityInDeck = 1
                            )
                        }
                    )
                }
            }
        }
    }

    /**
     * Adds a precon deck to the user's collection.
     * Loads its real decklist cards first if not cached locally, then increments every card's
     * quantity in the local collection.
     */
    suspend fun addPreconToCollection(preconId: String): Result<Unit> = runCatching {
        ensurePreconCardsLoaded(preconId).getOrThrow()
        dao.addPreconToCollection(preconId)
    }

    suspend fun removePreconFromCollection(preconId: String) {
        dao.removePreconFromCollection(preconId)
    }

    /**
     * Allows the user to create and immediately add a custom precon deck (with optional
     * newline-separated card list like "3 Blue-Eyes White Dragon" or "1 Sol Ring")
     * stored 100% locally in Room.
     */
    suspend fun createAndAddCustomPrecon(
        name: String,
        game: TcgGame,
        category: String,
        releaseYear: Int,
        cardLines: String,
        addImmediatelyToCollection: Boolean
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return

        val shortId = UUID.randomUUID().toString().take(8)
        val setId = "custom:${game.shortName.lowercase()}:$shortId"
        val preconId = "precon:custom:$shortId"
        val parsedLines = parseDecklistLines(cardLines)

        val customSet = TcgSetEntity(
            id = setId,
            game = game,
            code = "PRE-$shortId".uppercase().take(8),
            name = cleanName,
            releaseDate = "$releaseYear-01-01",
            cardCount = parsedLines.size.coerceAtLeast(1),
            setType = category.ifBlank { "Custom Precon" },
            cardsLoaded = true
        )
        dao.upsertSet(customSet)

        val cardEntities = parsedLines.mapIndexed { index, (qty, cardName) ->
            val cardId = "$setId:${index + 1}"
            TcgCardEntity(
                id = cardId,
                setId = setId,
                game = game,
                name = cardName,
                collectorNumber = (index + 1).toString().padStart(3, '0'),
                rarity = "Precon",
                cardType = "${game.displayName} Precon Card",
                manaOrStats = "x$qty in deck",
                description = "Included in $cleanName ($category)."
            )
        }
        if (cardEntities.isNotEmpty()) {
            dao.upsertCards(cardEntities)
        }

        val precon = PreconDeckEntity(
            id = preconId,
            game = game,
            name = cleanName,
            category = category.ifBlank { "Custom Precon" },
            releaseYear = releaseYear,
            setCode = customSet.code,
            description = "Custom preconstructed deck added locally (${parsedLines.sumOf { it.first }} cards).",
            ownedCount = 0,
            isCustom = true
        )
        dao.upsertPrecon(precon)

        if (cardEntities.isNotEmpty()) {
            dao.insertPreconCards(
                cardEntities.mapIndexed { idx, card ->
                    PreconCardEntity(
                        preconId = preconId,
                        cardId = card.id,
                        cardName = card.name,
                        quantityInDeck = parsedLines[idx].first
                    )
                }
            )
        }

        if (addImmediatelyToCollection) {
            dao.addPreconToCollection(preconId)
        }
    }

    companion object {
        private val SPECIAL_MTG_PRODUCT_QUERIES = mapOf(
            "precon:mtg:fdn-beginner-box" to "set:fdn cn>=488 cn<=564"
        )

        fun parseDecklistLines(raw: String): List<Pair<Int, String>> {
            return raw.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val match = Regex("^(\\d+)\\s*[xX]?\\s+(.+)$").find(line)
                    if (match != null) {
                        val qty = match.groupValues[1].toIntOrNull()?.coerceIn(1, 99) ?: 1
                        val name = match.groupValues[2].trim()
                        if (name.isNotBlank()) qty to name else null
                    } else {
                        1 to line
                    }
                }
        }
    }
}
