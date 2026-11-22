package com.tcgvault.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.tcgvault.app.data.local.entity.CollectionEntryEntity
import com.tcgvault.app.data.local.entity.PreconCardEntity
import com.tcgvault.app.data.local.entity.PreconDeckEntity
import com.tcgvault.app.data.local.entity.TcgCardEntity
import com.tcgvault.app.data.local.entity.TcgSetEntity
import com.tcgvault.app.data.local.model.CardWithCollection
import com.tcgvault.app.data.local.model.CollectionStats
import com.tcgvault.app.data.local.model.PreconCardDetail
import com.tcgvault.app.data.local.model.PreconWithSummary
import com.tcgvault.app.data.local.model.SetWithProgress
import com.tcgvault.app.data.model.TcgGame
import kotlinx.coroutines.flow.Flow

@Dao
interface TcgDao {

    // --- Sets ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSetsIgnore(sets: List<TcgSetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSet(set: TcgSetEntity)

    @Query("SELECT COUNT(*) FROM tcg_sets")
    suspend fun getSetCount(): Int

    @Query("SELECT * FROM tcg_sets WHERE id = :setId")
    suspend fun getSetById(setId: String): TcgSetEntity?

    @Query("UPDATE tcg_sets SET cardsLoaded = 1, cardCount = CASE WHEN :actualCount > 0 THEN :actualCount ELSE cardCount END WHERE id = :setId")
    suspend fun markSetCardsLoaded(setId: String, actualCount: Int)

    @Query(
        """
        UPDATE tcg_sets
        SET name = :name,
            releaseDate = :releaseDate,
            cardCount = CASE WHEN :officialCardCount > cardCount THEN :officialCardCount ELSE cardCount END,
            setType = :setType,
            iconUrl = COALESCE(:iconUrl, iconUrl),
            cardsLoaded = CASE WHEN :officialCardCount > cardCount THEN 0 ELSE cardsLoaded END
        WHERE id = :setId
        """
    )
    suspend fun refreshSetMetadata(
        setId: String,
        name: String,
        releaseDate: String,
        officialCardCount: Int,
        setType: String,
        iconUrl: String?
    )

    @Query("UPDATE tcg_sets SET cardCount = CASE WHEN :minCount > cardCount THEN :minCount ELSE cardCount END WHERE id = :setId")
    suspend fun ensureSetCardCountAtLeast(setId: String, minCount: Int)

    @Query("UPDATE tcg_sets SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :setId")
    suspend fun toggleSetFavorite(setId: String)

    @Query("UPDATE precon_decks SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :preconId")
    suspend fun togglePreconFavorite(preconId: String)

    @Query("UPDATE tcg_cards SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :cardId")
    suspend fun toggleCardFavorite(cardId: String)

    @Query(
        """
        SELECT s.*,
               COUNT(DISTINCT CASE WHEN ce.quantity > 0 THEN c.id END) AS collectedUniqueCount,
               COALESCE(SUM(CASE WHEN ce.quantity > 0 THEN ce.quantity ELSE 0 END), 0) AS totalOwnedCopies
        FROM tcg_sets s
        LEFT JOIN tcg_cards c ON c.setId = s.id
        LEFT JOIN collection_entries ce ON ce.cardId = c.id
        WHERE (:game IS NULL OR s.game = :game)
          AND (:query = '' OR s.name LIKE '%' || :query || '%' OR s.code LIKE '%' || :query || '%')
        GROUP BY s.id
        ORDER BY s.releaseDate DESC, s.name ASC
        """
    )
    fun observeSetsWithProgress(game: TcgGame?, query: String): Flow<List<SetWithProgress>>

    @Query(
        """
        SELECT s.*,
               COUNT(DISTINCT CASE WHEN ce.quantity > 0 THEN c.id END) AS collectedUniqueCount,
               COALESCE(SUM(CASE WHEN ce.quantity > 0 THEN ce.quantity ELSE 0 END), 0) AS totalOwnedCopies
        FROM tcg_sets s
        LEFT JOIN tcg_cards c ON c.setId = s.id
        LEFT JOIN collection_entries ce ON ce.cardId = c.id
        WHERE s.id = :setId
        GROUP BY s.id
        """
    )
    fun observeSetWithProgress(setId: String): Flow<SetWithProgress?>

    // --- Cards ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCards(cards: List<TcgCardEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardsIgnore(cards: List<TcgCardEntity>)

    @Query("UPDATE tcg_cards SET imageUrl = :imageUrl WHERE id = :cardId AND (imageUrl IS NULL OR imageUrl = '')")
    suspend fun updateCardImageIfMissing(cardId: String, imageUrl: String)

    @Query(
        """
        DELETE FROM tcg_cards
        WHERE game = 'MTG'
          AND LENGTH(id) < 20
          AND id NOT IN (SELECT cardId FROM collection_entries WHERE quantity > 0)
          AND setId IN (SELECT DISTINCT setId FROM tcg_cards WHERE game = 'MTG' AND LENGTH(id) >= 20)
        """
    )
    suspend fun pruneReplacedMtgSeedStubs()

    @Query("SELECT COUNT(*) FROM tcg_cards WHERE setId = :setId")
    suspend fun getCardCountForSet(setId: String): Int

    @Query(
        """
        SELECT c.*,
               COALESCE(ce.quantity, 0) AS ownedQuantity,
               COALESCE(ce.foilQuantity, 0) AS foilQuantity,
               s.name AS setName,
               s.code AS setCode
        FROM tcg_cards c
        INNER JOIN tcg_sets s ON s.id = c.setId
        LEFT JOIN collection_entries ce ON ce.cardId = c.id
        WHERE c.setId = :setId
          AND (:query = '' OR c.name LIKE '%' || :query || '%' OR c.collectorNumber LIKE '%' || :query || '%' OR c.cardType LIKE '%' || :query || '%')
        ORDER BY c.isFavorite DESC, c.collectorNumber ASC, c.name ASC
        """
    )
    fun observeCardsInSet(setId: String, query: String): Flow<List<CardWithCollection>>

    // --- Collection ---
    @Query("SELECT * FROM collection_entries WHERE cardId = :cardId")
    suspend fun getCollectionEntry(cardId: String): CollectionEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCollectionEntry(entry: CollectionEntryEntity)

    @Query("DELETE FROM collection_entries WHERE cardId = :cardId")
    suspend fun deleteCollectionEntry(cardId: String)

    @Transaction
    suspend fun adjustCardQuantity(cardId: String, delta: Int) {
        val existing = getCollectionEntry(cardId)
        val newQty = (existing?.quantity ?: 0) + delta
        if (newQty <= 0) {
            deleteCollectionEntry(cardId)
        } else {
            upsertCollectionEntry(
                CollectionEntryEntity(
                    cardId = cardId,
                    quantity = newQty,
                    foilQuantity = existing?.foilQuantity ?: 0,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    @Transaction
    suspend fun setCardQuantity(cardId: String, quantity: Int) {
        if (quantity <= 0) {
            deleteCollectionEntry(cardId)
        } else {
            val existing = getCollectionEntry(cardId)
            upsertCollectionEntry(
                CollectionEntryEntity(
                    cardId = cardId,
                    quantity = quantity,
                    foilQuantity = existing?.foilQuantity ?: 0,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    @Query(
        """
        SELECT c.*,
               ce.quantity AS ownedQuantity,
               ce.foilQuantity AS foilQuantity,
               s.name AS setName,
               s.code AS setCode
        FROM collection_entries ce
        INNER JOIN tcg_cards c ON c.id = ce.cardId
        INNER JOIN tcg_sets s ON s.id = c.setId
        WHERE ce.quantity > 0
          AND (:game IS NULL OR c.game = :game)
          AND (:query = '' OR c.name LIKE '%' || :query || '%' OR s.name LIKE '%' || :query || '%' OR s.code LIKE '%' || :query || '%')
        ORDER BY c.isFavorite DESC, ce.updatedAt DESC, c.name ASC
        """
    )
    fun observeOwnedCards(game: TcgGame?, query: String): Flow<List<CardWithCollection>>

    @Query(
        """
        SELECT
            COALESCE((SELECT SUM(quantity) FROM collection_entries WHERE quantity > 0), 0) AS totalCopies,
            COALESCE((SELECT COUNT(*) FROM collection_entries WHERE quantity > 0), 0) AS uniqueCards,
            COALESCE((SELECT SUM(ce.quantity) FROM collection_entries ce INNER JOIN tcg_cards c ON c.id = ce.cardId WHERE ce.quantity > 0 AND c.game = 'YUGIOH'), 0) AS yugiohCopies,
            COALESCE((SELECT SUM(ce.quantity) FROM collection_entries ce INNER JOIN tcg_cards c ON c.id = ce.cardId WHERE ce.quantity > 0 AND c.game = 'MTG'), 0) AS mtgCopies,
            COALESCE((SELECT SUM(ownedCount) FROM precon_decks WHERE ownedCount > 0), 0) AS ownedPrecons,
            COALESCE((SELECT COUNT(DISTINCT c.setId) FROM collection_entries ce INNER JOIN tcg_cards c ON c.id = ce.cardId WHERE ce.quantity > 0), 0) AS setsStarted
        """
    )
    fun observeCollectionStats(): Flow<CollectionStats>

    // --- Precons ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPreconsIgnore(precons: List<PreconDeckEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrecon(precon: PreconDeckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreconCards(cards: List<PreconCardEntity>)

    @Query("SELECT COUNT(*) FROM precon_decks")
    suspend fun getPreconCount(): Int

    @Query("DELETE FROM precon_decks WHERE id LIKE 'precon:mtg:set:%' AND ownedCount = 0")
    suspend fun deleteUnownedLegacyMtgSetPrecons()

    @Query(
        """
        DELETE FROM precon_decks
        WHERE id IN ('precon:ygo:sdwd', 'precon:ygo:sr14', 'precon:mtg:lcc-veloci', 'precon:mtg:blc-squirrel')
          AND ownedCount = 0
          AND EXISTS (
            SELECT 1 FROM precon_decks p2
            WHERE p2.game = precon_decks.game
              AND p2.id != precon_decks.id
              AND (
                LOWER(p2.setCode) = LOWER(precon_decks.setCode)
                OR LOWER(p2.name) = LOWER(precon_decks.name)
              )
          )
        """
    )
    suspend fun pruneReplacedSeedPrecons()

    @Query("DELETE FROM precon_cards WHERE preconId NOT IN (SELECT id FROM precon_decks)")
    suspend fun pruneOrphanedPreconCards()

    @Query(
        """
        UPDATE tcg_sets
        SET name = REPLACE(REPLACE(REPLACE(REPLACE(name, '&apos;', ''''), '&#039;', ''''), '&quot;', '"'), '&amp;', '&')
        WHERE name LIKE '%&apos;%' OR name LIKE '%&#039;%' OR name LIKE '%&quot;%' OR name LIKE '%&amp;%'
        """
    )
    suspend fun decodeHtmlEntitiesInSets()

    @Query(
        """
        UPDATE precon_decks
        SET name = REPLACE(REPLACE(REPLACE(REPLACE(name, '&apos;', ''''), '&#039;', ''''), '&quot;', '"'), '&amp;', '&'),
            description = REPLACE(REPLACE(REPLACE(REPLACE(description, '&apos;', ''''), '&#039;', ''''), '&quot;', '"'), '&amp;', '&')
        WHERE name LIKE '%&apos;%' OR name LIKE '%&#039;%' OR name LIKE '%&quot;%' OR name LIKE '%&amp;%'
           OR description LIKE '%&apos;%' OR description LIKE '%&#039;%' OR description LIKE '%&quot;%' OR description LIKE '%&amp;%'
        """
    )
    suspend fun decodeHtmlEntitiesInPrecons()

    @Query("UPDATE precon_decks SET deckFileName = :deckFileName WHERE id = :preconId AND (deckFileName IS NULL OR deckFileName = '')")
    suspend fun setPreconDeckFileNameIfMissing(preconId: String, deckFileName: String): Int

    @Query("DELETE FROM precon_cards WHERE preconId = :preconId")
    suspend fun clearPreconCards(preconId: String)

    @Query("SELECT * FROM precon_decks WHERE id = :preconId")
    suspend fun getPreconById(preconId: String): PreconDeckEntity?

    @Query("SELECT * FROM precon_cards WHERE preconId = :preconId")
    suspend fun getPreconCards(preconId: String): List<PreconCardEntity>

    @Query("UPDATE precon_decks SET ownedCount = :ownedCount WHERE id = :preconId")
    suspend fun updatePreconOwnedCount(preconId: String, ownedCount: Int)

    @Query(
        """
        SELECT p.*,
               COALESCE(SUM(pc.quantityInDeck), 0) AS totalCardsInDeck,
               COUNT(pc.id) AS uniqueCardsInDeck
        FROM precon_decks p
        LEFT JOIN precon_cards pc ON pc.preconId = p.id
        WHERE (:game IS NULL OR p.game = :game)
          AND (:query = '' OR p.name LIKE '%' || :query || '%' OR p.category LIKE '%' || :query || '%' OR p.setCode LIKE '%' || :query || '%')
        GROUP BY p.id
        ORDER BY p.isFavorite DESC, p.releaseYear DESC, p.name ASC
        """
    )
    fun observePrecons(game: TcgGame?, query: String): Flow<List<PreconWithSummary>>

    @Query(
        """
        SELECT pc.cardId AS cardId,
               pc.cardName AS cardName,
               pc.quantityInDeck AS quantityInDeck,
               COALESCE(ce.quantity, 0) AS ownedQuantity,
               COALESCE(c.cardType, 'Card') AS cardType,
               COALESCE(c.rarity, 'Common') AS rarity,
               c.imageUrl AS imageUrl
        FROM precon_cards pc
        LEFT JOIN tcg_cards c ON c.id = pc.cardId
        LEFT JOIN collection_entries ce ON ce.cardId = pc.cardId
        WHERE pc.preconId = :preconId
        ORDER BY pc.cardName ASC
        """
    )
    fun observePreconCardDetails(preconId: String): Flow<List<PreconCardDetail>>

    @Transaction
    suspend fun addPreconToCollection(preconId: String) {
        val precon = getPreconById(preconId) ?: return
        updatePreconOwnedCount(preconId, precon.ownedCount + 1)
        val deckCards = getPreconCards(preconId)
        for (deckCard in deckCards) {
            adjustCardQuantity(deckCard.cardId, deckCard.quantityInDeck)
        }
    }

    @Transaction
    suspend fun removePreconFromCollection(preconId: String) {
        val precon = getPreconById(preconId) ?: return
        if (precon.ownedCount <= 0) return
        updatePreconOwnedCount(preconId, (precon.ownedCount - 1).coerceAtLeast(0))
        val deckCards = getPreconCards(preconId)
        for (deckCard in deckCards) {
            adjustCardQuantity(deckCard.cardId, -deckCard.quantityInDeck)
        }
    }
}
