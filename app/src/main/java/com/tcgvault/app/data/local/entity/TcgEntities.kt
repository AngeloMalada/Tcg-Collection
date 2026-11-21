package com.tcgvault.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tcgvault.app.data.model.TcgGame

@Entity(
    tableName = "tcg_sets",
    indices = [Index(value = ["game"]), Index(value = ["code"])]
)
data class TcgSetEntity(
    @PrimaryKey val id: String,
    val game: TcgGame,
    val code: String,
    val name: String,
    val releaseDate: String,
    val cardCount: Int,
    val setType: String,
    val iconUrl: String? = null,
    val cardsLoaded: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(
    tableName = "tcg_cards",
    indices = [Index(value = ["setId"]), Index(value = ["game"]), Index(value = ["name"])]
)
data class TcgCardEntity(
    @PrimaryKey val id: String,
    val setId: String,
    val game: TcgGame,
    val name: String,
    val collectorNumber: String,
    val rarity: String,
    val cardType: String,
    val manaOrStats: String,
    val description: String,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false
)

@Entity(tableName = "collection_entries")
data class CollectionEntryEntity(
    @PrimaryKey val cardId: String,
    val quantity: Int,
    val foilQuantity: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "precon_decks",
    indices = [Index(value = ["game"])]
)
data class PreconDeckEntity(
    @PrimaryKey val id: String,
    val game: TcgGame,
    val name: String,
    val category: String,
    val releaseYear: Int,
    val setCode: String,
    val description: String,
    val ownedCount: Int = 0,
    val isCustom: Boolean = false,
    val deckFileName: String? = null,
    val isFavorite: Boolean = false
)

@Entity(
    tableName = "precon_cards",
    indices = [Index(value = ["preconId"]), Index(value = ["cardId"])]
)
data class PreconCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val preconId: String,
    val cardId: String,
    val cardName: String,
    val quantityInDeck: Int
)
