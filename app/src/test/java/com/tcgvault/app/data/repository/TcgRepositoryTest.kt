package com.tcgvault.app.data.repository

import com.tcgvault.app.data.local.seed.OfflineSeedCatalog
import com.tcgvault.app.data.model.ReleaseDateFilter
import com.tcgvault.app.data.model.SetCategoryClassifier
import com.tcgvault.app.data.model.TcgGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TcgRepositoryTest {

    @Test
    fun parseDecklistLines_parsesQuantitiesAndBareNames() {
        val raw = """
            3 Blue-Eyes White Dragon
            2x Sage with Eyes of Blue
            1X Ash Blossom & Joyous Spring
            Sol Ring
        """.trimIndent()

        val parsed = TcgRepository.parseDecklistLines(raw)

        assertEquals(4, parsed.size)
        assertEquals(3 to "Blue-Eyes White Dragon", parsed[0])
        assertEquals(2 to "Sage with Eyes of Blue", parsed[1])
        assertEquals(1 to "Ash Blossom & Joyous Spring", parsed[2])
        assertEquals(1 to "Sol Ring", parsed[3])
    }

    @Test
    fun offlineSeedCatalog_containsBothGamesAndPrecons() {
        val gamesInSets = OfflineSeedCatalog.initialSets.map { it.game }.toSet()
        val gamesInPrecons = OfflineSeedCatalog.initialPrecons.map { it.game }.toSet()

        assertTrue(gamesInSets.contains(TcgGame.YUGIOH))
        assertTrue(gamesInSets.contains(TcgGame.MTG))
        assertTrue(gamesInPrecons.contains(TcgGame.YUGIOH))
        assertTrue(gamesInPrecons.contains(TcgGame.MTG))
        assertTrue(OfflineSeedCatalog.initialCards.isNotEmpty())
        assertTrue(OfflineSeedCatalog.initialPreconCards.isNotEmpty())
    }

    @Test
    fun releaseDateFilter_hidesFutureAndInvalidDates() {
        val today = "2026-11-21"
        assertTrue(ReleaseDateFilter.isReleased("2024-11-15", today))
        assertTrue(ReleaseDateFilter.isReleased("2026-11-21", today))
        assertFalse(ReleaseDateFilter.isReleased("2026-11-22", today))
        assertFalse(ReleaseDateFilter.isReleased("2027-03-01", today))
        assertFalse(ReleaseDateFilter.isReleased("", today))
    }

    @Test
    fun setCategoryClassifier_groupsMtgAndYugiohSets() {
        assertEquals("Expansions", SetCategoryClassifier.categoryFor(TcgGame.MTG, "expansion", "Bloomburrow"))
        assertEquals("Commander", SetCategoryClassifier.categoryFor(TcgGame.MTG, "commander", "Bloomburrow Commander"))
        assertEquals("Masters & Horizons", SetCategoryClassifier.categoryFor(TcgGame.MTG, "draft_innovation", "Modern Horizons 3"))
        assertEquals("Structure & Starter Decks", SetCategoryClassifier.categoryFor(TcgGame.YUGIOH, "Structure Deck", "Structure Deck: Fire Kings"))
        assertEquals("Tins & Reprint Sets", SetCategoryClassifier.categoryFor(TcgGame.YUGIOH, "Reprint Set", "25th Anniversary Rarity Collection"))
        assertEquals("Booster Sets", SetCategoryClassifier.categoryFor(TcgGame.YUGIOH, "Booster Pack", "Legend of Blue Eyes White Dragon"))
    }
}
