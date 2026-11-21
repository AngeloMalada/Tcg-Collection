package com.tcgvault.app.data.local.model

import androidx.room.Embedded
import com.tcgvault.app.data.local.entity.PreconDeckEntity
import com.tcgvault.app.data.local.entity.TcgCardEntity
import com.tcgvault.app.data.local.entity.TcgSetEntity

data class SetWithProgress(
    @Embedded val set: TcgSetEntity,
    val collectedUniqueCount: Int,
    val totalOwnedCopies: Int
) {
    val displayTotalCards: Int
        get() = maxOf(set.cardCount, collectedUniqueCount).coerceAtLeast(1)

    val progressFraction: Float
        get() = (collectedUniqueCount.toFloat() / displayTotalCards.toFloat()).coerceIn(0f, 1f)
}

data class CardWithCollection(
    @Embedded val card: TcgCardEntity,
    val ownedQuantity: Int,
    val foilQuantity: Int,
    val setName: String = "",
    val setCode: String = ""
) {
    val isCollected: Boolean get() = ownedQuantity > 0
}

data class PreconWithSummary(
    @Embedded val precon: PreconDeckEntity,
    val totalCardsInDeck: Int,
    val uniqueCardsInDeck: Int
)

data class PreconCardDetail(
    val cardId: String,
    val cardName: String,
    val quantityInDeck: Int,
    val ownedQuantity: Int,
    val cardType: String,
    val rarity: String,
    val imageUrl: String?
)

data class CollectionStats(
    val totalCopies: Int = 0,
    val uniqueCards: Int = 0,
    val yugiohCopies: Int = 0,
    val mtgCopies: Int = 0,
    val ownedPrecons: Int = 0,
    val setsStarted: Int = 0
)
