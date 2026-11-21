package com.tcgvault.app.data.local.seed

import com.tcgvault.app.data.local.entity.PreconCardEntity
import com.tcgvault.app.data.local.entity.PreconDeckEntity
import com.tcgvault.app.data.local.entity.TcgCardEntity
import com.tcgvault.app.data.local.entity.TcgSetEntity
import com.tcgvault.app.data.model.TcgGame
import java.net.URLEncoder

/**
 * Bundled offline starter catalog so the app works immediately with zero network
 * and no backend. Users can also sync the complete MTG (Scryfall) & Yu-Gi-Oh!
 * (YGOPRODeck) set catalogs directly into local Room storage anytime.
 */
object OfflineSeedCatalog {

    private fun scryfallImage(cardName: String, setCode: String): String {
        val encoded = URLEncoder.encode(cardName, "UTF-8")
        return "https://api.scryfall.com/cards/named?exact=$encoded&set=${setCode.lowercase()}&format=image&version=normal"
    }

    val initialSets: List<TcgSetEntity> = listOf(
        // Yu-Gi-Oh! Iconic Sets
        TcgSetEntity(
            id = "ygo:LOB",
            game = TcgGame.YUGIOH,
            code = "LOB",
            name = "Legend of Blue Eyes White Dragon",
            releaseDate = "2002-03-08",
            cardCount = 10,
            setType = "Booster Pack",
            cardsLoaded = true
        ),
        TcgSetEntity(
            id = "ygo:MRD",
            game = TcgGame.YUGIOH,
            code = "MRD",
            name = "Metal Raiders",
            releaseDate = "2002-06-26",
            cardCount = 6,
            setType = "Booster Pack",
            cardsLoaded = true
        ),
        TcgSetEntity(
            id = "ygo:RA01",
            game = TcgGame.YUGIOH,
            code = "RA01",
            name = "25th Anniversary Rarity Collection",
            releaseDate = "2023-11-03",
            cardCount = 6,
            setType = "Reprint Set",
            cardsLoaded = true
        ),
        TcgSetEntity(
            id = "ygo:SDWD",
            game = TcgGame.YUGIOH,
            code = "SDWD",
            name = "Structure Deck: Blue-Eyes White Destiny",
            releaseDate = "2025-02-14",
            cardCount = 6,
            setType = "Structure Deck",
            cardsLoaded = true
        ),
        TcgSetEntity(
            id = "ygo:SR14",
            game = TcgGame.YUGIOH,
            code = "SR14",
            name = "Structure Deck: Fire Kings",
            releaseDate = "2023-12-08",
            cardCount = 6,
            setType = "Structure Deck",
            cardsLoaded = true
        ),
        // Magic: The Gathering Iconic Sets
        TcgSetEntity(
            id = "mtg:fdn",
            game = TcgGame.MTG,
            code = "FDN",
            name = "Foundations",
            releaseDate = "2024-11-15",
            cardCount = 8,
            setType = "core",
            cardsLoaded = true
        ),
        TcgSetEntity(
            id = "mtg:blb",
            game = TcgGame.MTG,
            code = "BLB",
            name = "Bloomburrow",
            releaseDate = "2024-08-02",
            cardCount = 6,
            setType = "expansion",
            cardsLoaded = true
        ),
        TcgSetEntity(
            id = "mtg:mh3",
            game = TcgGame.MTG,
            code = "MH3",
            name = "Modern Horizons 3",
            releaseDate = "2024-06-14",
            cardCount = 6,
            setType = "draft_innovation",
            cardsLoaded = true
        ),
        TcgSetEntity(
            id = "mtg:lcc",
            game = TcgGame.MTG,
            code = "LCC",
            name = "The Lost Caverns of Ixalan Commander",
            releaseDate = "2023-11-17",
            cardCount = 6,
            setType = "commander",
            cardsLoaded = true
        ),
        TcgSetEntity(
            id = "mtg:blc",
            game = TcgGame.MTG,
            code = "BLC",
            name = "Bloomburrow Commander",
            releaseDate = "2024-08-02",
            cardCount = 6,
            setType = "commander",
            cardsLoaded = true
        )
    )

    val initialCards: List<TcgCardEntity> = listOf(
        // LOB - Legend of Blue Eyes White Dragon
        TcgCardEntity("ygo:LOB:89631139", "ygo:LOB", TcgGame.YUGIOH, "Blue-Eyes White Dragon", "LOB-001", "Ultra Rare", "Dragon / Normal Monster", "ATK 3000 / DEF 2500 • LV 8", "This legendary dragon is a powerful engine of destruction. Virtually invincible, very few have faced this awesome creature and lived to tell the tale.", "https://images.ygoprodeck.com/images/cards/89631139.jpg"),
        TcgCardEntity("ygo:LOB:46986414", "ygo:LOB", TcgGame.YUGIOH, "Dark Magician", "LOB-005", "Ultra Rare", "Spellcaster / Normal Monster", "ATK 2500 / DEF 2100 • LV 7", "The ultimate wizard in terms of attack and defense.", "https://images.ygoprodeck.com/images/cards/46986414.jpg"),
        TcgCardEntity("ygo:LOB:74677422", "ygo:LOB", TcgGame.YUGIOH, "Red-Eyes Black Dragon", "LOB-070", "Ultra Rare", "Dragon / Normal Monster", "ATK 2400 / DEF 2000 • LV 7", "A ferocious dragon with a deadly attack.", "https://images.ygoprodeck.com/images/cards/74677422.jpg"),
        TcgCardEntity("ygo:LOB:33396948", "ygo:LOB", TcgGame.YUGIOH, "Exodia the Forbidden One", "LOB-124", "Ultra Rare", "Spellcaster / Effect Monster", "ATK 1000 / DEF 1000 • LV 3", "If you have Right Leg, Left Leg, Right Arm and Left Arm of the Forbidden One in addition to this card in your hand, you win the Duel.", "https://images.ygoprodeck.com/images/cards/33396948.jpg"),
        TcgCardEntity("ygo:LOB:53129443", "ygo:LOB", TcgGame.YUGIOH, "Dark Hole", "LOB-052", "Super Rare", "Normal Spell", "Spell", "Destroy all monsters on the field.", "https://images.ygoprodeck.com/images/cards/53129443.jpg"),
        TcgCardEntity("ygo:LOB:12580477", "ygo:LOB", TcgGame.YUGIOH, "Raigeki", "LOB-053", "Super Rare", "Normal Spell", "Spell", "Destroy all monsters your opponent controls.", "https://images.ygoprodeck.com/images/cards/12580477.jpg"),
        TcgCardEntity("ygo:LOB:83764718", "ygo:LOB", TcgGame.YUGIOH, "Monster Reborn", "LOB-118", "Ultra Rare", "Normal Spell", "Spell", "Target 1 monster in either GY; Special Summon it.", "https://images.ygoprodeck.com/images/cards/83764718.jpg"),
        TcgCardEntity("ygo:LOB:55144522", "ygo:LOB", TcgGame.YUGIOH, "Pot of Greed", "LOB-119", "Rare", "Normal Spell", "Spell", "Draw 2 cards.", "https://images.ygoprodeck.com/images/cards/55144522.jpg"),
        TcgCardEntity("ygo:LOB:24094653", "ygo:LOB", TcgGame.YUGIOH, "Polymerization", "LOB-059", "Super Rare", "Normal Spell", "Spell", "Fusion Summon 1 Fusion Monster from your Extra Deck, using monsters from your hand or field as Fusion Material.", "https://images.ygoprodeck.com/images/cards/24094653.jpg"),
        TcgCardEntity("ygo:LOB:70781052", "ygo:LOB", TcgGame.YUGIOH, "Celtic Guardian", "LOB-007", "Super Rare", "Warrior / Normal Monster", "ATK 1400 / DEF 1200 • LV 4", "An elf who learned to wield a sword, he baffles enemies with lightning-swift attacks.", "https://images.ygoprodeck.com/images/cards/91152256.jpg"),

        // MRD - Metal Raiders
        TcgCardEntity("ygo:MRD:70781052", "ygo:MRD", TcgGame.YUGIOH, "Summoned Skull", "MRD-003", "Ultra Rare", "Fiend / Normal Monster", "ATK 2500 / DEF 1200 • LV 6", "A fiend with dark powers for confusing the enemy. Among the Fiend-Type monsters, this monster boasts considerable force.", "https://images.ygoprodeck.com/images/cards/70781052.jpg"),
        TcgCardEntity("ygo:MRD:44095762", "ygo:MRD", TcgGame.YUGIOH, "Mirror Force", "MRD-138", "Ultra Rare", "Normal Trap", "Trap", "When an opponent's monster declares an attack: Destroy all your opponent's Attack Position monsters.", "https://images.ygoprodeck.com/images/cards/44095762.jpg"),
        TcgCardEntity("ygo:MRD:41420027", "ygo:MRD", TcgGame.YUGIOH, "Solemn Judgment", "MRD-127", "Ultra Rare", "Counter Trap", "Trap", "When a monster(s) would be Summoned, OR a Spell/Trap Card is activated: Pay half your LP; negate the Summon or activation, and if you do, destroy that card.", "https://images.ygoprodeck.com/images/cards/41420027.jpg"),
        TcgCardEntity("ygo:MRD:40640057", "ygo:MRD", TcgGame.YUGIOH, "Kuriboh", "MRD-071", "Super Rare", "Fiend / Effect Monster", "ATK 300 / DEF 200 • LV 1", "During damage calculation, if your opponent's monster attacks (Quick Effect): You can discard this card; you take no battle damage from that battle.", "https://images.ygoprodeck.com/images/cards/40640057.jpg"),
        TcgCardEntity("ygo:MRD:66602787", "ygo:MRD", TcgGame.YUGIOH, "Sanga of the Thunder", "MRD-025", "Super Rare", "Thunder / Effect Monster", "ATK 2600 / DEF 2200 • LV 7", "During damage calculation in your opponent's turn, if this card is being attacked: You can target the attacking monster; make that target's ATK 0 during damage calculation only.", "https://images.ygoprodeck.com/images/cards/25955164.jpg"),
        TcgCardEntity("ygo:MRD:04031928", "ygo:MRD", TcgGame.YUGIOH, "Change of Heart", "MRD-060", "Ultra Rare", "Normal Spell", "Spell", "Target 1 monster your opponent controls; take control of it until the End Phase.", "https://images.ygoprodeck.com/images/cards/4031928.jpg"),

        // RA01 - 25th Anniversary Rarity Collection
        TcgCardEntity("ygo:RA01:14558127", "ygo:RA01", TcgGame.YUGIOH, "Ash Blossom & Joyous Spring", "RA01-EN008", "Quarter Century Secret Rare", "Zombie / Tuner / Effect", "ATK 0 / DEF 1800 • LV 3", "When a card or effect is activated that includes adding a card from Deck to hand, Special Summoning from Deck, or sending from Deck to GY: Discard this card; negate that effect.", "https://images.ygoprodeck.com/images/cards/14558127.jpg"),
        TcgCardEntity("ygo:RA01:10045474", "ygo:RA01", TcgGame.YUGIOH, "Infinite Impermanence", "RA01-EN075", "Secret Rare", "Normal Trap", "Trap", "Target 1 face-up monster your opponent controls; negate its effects until the end of this turn.", "https://images.ygoprodeck.com/images/cards/10045474.jpg"),
        TcgCardEntity("ygo:RA01:27204311", "ygo:RA01", TcgGame.YUGIOH, "Nibiru, the Primal Being", "RA01-EN015", "Ultra Rare", "Rock / Effect Monster", "ATK 3000 / DEF 600 • LV 11", "During the Main Phase, if your opponent Normal or Special Summoned 5 or more monsters this turn (Quick Effect): Tribute as many face-up monsters on the field as possible, and Special Summon this card.", "https://images.ygoprodeck.com/images/cards/27204311.jpg"),
        TcgCardEntity("ygo:RA01:84815190", "ygo:RA01", TcgGame.YUGIOH, "Baronne de Fleur", "RA01-EN034", "Collector's Rare", "Warrior / Synchro / Effect", "ATK 3000 / DEF 2400 • LV 10", "1 Tuner + 1+ non-Tuner monsters. Once per turn: You can target 1 card on the field; destroy it.", "https://images.ygoprodeck.com/images/cards/84815190.jpg"),
        TcgCardEntity("ygo:RA01:24224830", "ygo:RA01", TcgGame.YUGIOH, "Called by the Grave", "RA01-EN056", "Super Rare", "Quick-Play Spell", "Spell", "Target 1 monster in your opponent's GY; banish it, and negate its activated effects until the end of the next turn.", "https://images.ygoprodeck.com/images/cards/24224830.jpg"),
        TcgCardEntity("ygo:RA01:23434538", "ygo:RA01", TcgGame.YUGIOH, "Effect Veiler", "RA01-EN001", "Super Rare", "Spellcaster / Tuner / Effect", "ATK 0 / DEF 0 • LV 1", "During your opponent's Main Phase (Quick Effect): Send this card from your hand to the GY, then target 1 Effect Monster your opponent controls; negate its effects until the end of this turn.", "https://images.ygoprodeck.com/images/cards/97268402.jpg"),

        // SDWD - Structure Deck: Blue-Eyes White Destiny
        TcgCardEntity("ygo:SDWD:89631139", "ygo:SDWD", TcgGame.YUGIOH, "Blue-Eyes White Dragon", "SDWD-EN001", "Common", "Dragon / Normal Monster", "ATK 3000 / DEF 2500 • LV 8", "This legendary dragon is a powerful engine of destruction.", "https://images.ygoprodeck.com/images/cards/89631139.jpg"),
        TcgCardEntity("ygo:SDWD:38517737", "ygo:SDWD", TcgGame.YUGIOH, "Blue-Eyes Alternative White Dragon", "SDWD-EN005", "Common", "Dragon / Effect Monster", "ATK 3000 / DEF 2500 • LV 8", "Must first be Special Summoned from your hand by revealing Blue-Eyes White Dragon in your hand.", "https://images.ygoprodeck.com/images/cards/38517737.jpg"),
        TcgCardEntity("ygo:SDWD:8240199", "ygo:SDWD", TcgGame.YUGIOH, "Sage with Eyes of Blue", "SDWD-EN010", "Common", "Spellcaster / Tuner / Effect", "ATK 0 / DEF 1500 • LV 1", "When this card is Normal Summoned: You can add 1 Level 1 LIGHT Tuner from your Deck to your hand.", "https://images.ygoprodeck.com/images/cards/8240199.jpg"),
        TcgCardEntity("ygo:SDWD:59822133", "ygo:SDWD", TcgGame.YUGIOH, "Blue-Eyes Spirit Dragon", "SDWD-EN040", "Ultra Rare", "Dragon / Synchro / Effect", "ATK 2500 / DEF 3000 • LV 9", "1 Tuner + 1+ non-Tuner Blue-Eyes monsters. Neither player can Special Summon 2 or more monsters at the same time.", "https://images.ygoprodeck.com/images/cards/59822133.jpg"),
        TcgCardEntity("ygo:SDWD:43973174", "ygo:SDWD", TcgGame.YUGIOH, "The White Stone of Ancients", "SDWD-EN012", "Common", "Dragon / Tuner / Effect", "ATK 600 / DEF 500 • LV 1", "Once per turn, during the End Phase, if this card is in the GY because it was sent there this turn: Special Summon 1 Blue-Eyes monster from your Deck.", "https://images.ygoprodeck.com/images/cards/71039903.jpg"),
        TcgCardEntity("ygo:SDWD:14558127", "ygo:SDWD", TcgGame.YUGIOH, "Ash Blossom & Joyous Spring", "SDWD-EN018", "Common", "Zombie / Tuner / Effect", "ATK 0 / DEF 1800 • LV 3", "Negate an effect that adds from Deck, Special Summons from Deck, or sends from Deck to GY.", "https://images.ygoprodeck.com/images/cards/14558127.jpg"),

        // SR14 - Structure Deck: Fire Kings
        TcgCardEntity("ygo:SR14:37910722", "ygo:SR14", TcgGame.YUGIOH, "Legendary Fire King Ponix", "SR14-EN001", "Ultra Rare", "Winged Beast / Effect", "ATK 500 / DEF 200 • LV 1", "If a monster(s) that was originally FIRE is destroyed by battle or card effect: You can Special Summon this card from your hand.", "https://images.ygoprodeck.com/images/cards/34358408.jpg"),
        TcgCardEntity("ygo:SR14:98385955", "ygo:SR14", TcgGame.YUGIOH, "Sacred Fire King Garunix", "SR14-EN002", "Ultra Rare", "Winged Beast / Effect", "ATK 2700 / DEF 1700 • LV 8", "If a monster(s) that was originally FIRE is destroyed by battle or card effect: Special Summon this card from your GY or hand.", "https://images.ygoprodeck.com/images/cards/98385955.jpg"),
        TcgCardEntity("ygo:SR14:57554544", "ygo:SR14", TcgGame.YUGIOH, "Fire King Island", "SR14-EN025", "Common", "Field Spell", "Spell", "Destroy 1 monster in your hand or you control, and if you do, add 1 Fire King monster from your Deck to your hand.", "https://images.ygoprodeck.com/images/cards/57554544.jpg"),
        TcgCardEntity("ygo:SR14:57103969", "ygo:SR14", TcgGame.YUGIOH, "Fire Formation - Tenki", "SR14-EN030", "Common", "Continuous Spell", "Spell", "When this card is activated: You can add 1 Level 4 or lower Beast-Warrior monster from your Deck to your hand.", "https://images.ygoprodeck.com/images/cards/57103969.jpg"),
        TcgCardEntity("ygo:SR14:10045474", "ygo:SR14", TcgGame.YUGIOH, "Infinite Impermanence", "SR14-EN038", "Common", "Normal Trap", "Trap", "Target 1 face-up monster your opponent controls; negate its effects until the end of this turn.", "https://images.ygoprodeck.com/images/cards/10045474.jpg"),
        TcgCardEntity("ygo:SR14:59438930", "ygo:SR14", TcgGame.YUGIOH, "Droll & Lock Bird", "SR14-EN019", "Common", "Spellcaster / Effect", "ATK 0 / DEF 0 • LV 1", "If a card(s) is added from the Main Deck to your opponent's hand, except during the Draw Phase: Neither player can add cards from their Deck to their hand for the rest of this turn.", "https://images.ygoprodeck.com/images/cards/94145021.jpg"),

        // MTG: FDN - Foundations
        TcgCardEntity("mtg:fdn:1", "mtg:fdn", TcgGame.MTG, "Llanowar Elves", "001", "Common", "Creature — Elf Druid", "{G} • 1/1", "{T}: Add {G}.", scryfallImage("Llanowar Elves", "fdn")),
        TcgCardEntity("mtg:fdn:2", "mtg:fdn", TcgGame.MTG, "Sire of Seven Deaths", "002", "Mythic", "Creature — Eldrazi", "{7} • 7/7", "First strike, vigilance, menace, trample, reach, lifelink, ward—Pay 7 life.", scryfallImage("Sire of Seven Deaths", "fdn")),
        TcgCardEntity("mtg:fdn:3", "mtg:fdn", TcgGame.MTG, "Doubling Season", "003", "Mythic", "Enchantment", "{4}{G}", "If an effect would create one or more tokens under your control, it creates twice that many of those tokens instead.", scryfallImage("Doubling Season", "fdn")),
        TcgCardEntity("mtg:fdn:4", "mtg:fdn", TcgGame.MTG, "Liliana, Dreadhorde General", "004", "Mythic", "Legendary Planeswalker — Liliana", "{4}{B}{B} • Loyalty 6", "Whenever a creature you control dies, draw a card.", scryfallImage("Liliana, Dreadhorde General", "fdn")),
        TcgCardEntity("mtg:fdn:5", "mtg:fdn", TcgGame.MTG, "Koma, World-Eater", "005", "Rare", "Legendary Creature — Serpent", "{3}{G}{G}{U}{U} • 8/12", "This spell can't be countered. Trample, ward {4}.", scryfallImage("Koma, World-Eater", "fdn")),
        TcgCardEntity("mtg:fdn:6", "mtg:fdn", TcgGame.MTG, "Day of Judgment", "006", "Rare", "Sorcery", "{2}{W}{W}", "Destroy all creatures.", scryfallImage("Day of Judgment", "fdn")),
        TcgCardEntity("mtg:fdn:7", "mtg:fdn", TcgGame.MTG, "Opt", "007", "Common", "Instant", "{U}", "Scry 1. Draw a card.", scryfallImage("Opt", "fdn")),
        TcgCardEntity("mtg:fdn:8", "mtg:fdn", TcgGame.MTG, "Burst Lightning", "008", "Common", "Instant", "{R}", "Kicker {4}. Burst Lightning deals 2 damage to any target. If this spell was kicked, it deals 4 damage instead.", scryfallImage("Burst Lightning", "fdn")),

        // MTG: BLB - Bloomburrow
        TcgCardEntity("mtg:blb:1", "mtg:blb", TcgGame.MTG, "Maha, Its Feathers Night", "100", "Mythic", "Legendary Creature — Elemental Bird", "{3}{B}{B} • 6/5", "Flying, trample, ward—Discard a card. Creatures your opponents control have base toughness 1.", scryfallImage("Maha, Its Feathers Night", "blb")),
        TcgCardEntity("mtg:blb:2", "mtg:blb", TcgGame.MTG, "Lumra, Bellow of the Woods", "183", "Mythic", "Legendary Creature — Elemental Bear", "{4}{G}{G} • */*", "Vigilance, reach. Lumra's power and toughness are each equal to the number of lands you control.", scryfallImage("Lumra, Bellow of the Woods", "blb")),
        TcgCardEntity("mtg:blb:3", "mtg:blb", TcgGame.MTG, "Ral, Crackling Wit", "230", "Mythic", "Legendary Planeswalker — Ral", "{2}{U}{R} • Loyalty 4", "Whenever you cast a noncreature spell, put a loyalty counter on Ral.", scryfallImage("Ral, Crackling Wit", "blb")),
        TcgCardEntity("mtg:blb:4", "mtg:blb", TcgGame.MTG, "Three Tree City", "260", "Rare", "Legendary Land", "Land", "As Three Tree City enters, choose a creature type.", scryfallImage("Three Tree City", "blb")),
        TcgCardEntity("mtg:blb:5", "mtg:blb", TcgGame.MTG, "Heartfire Hero", "138", "Uncommon", "Creature — Mouse Soldier", "{R} • 1/1", "Valiant — Whenever Heartfire Hero becomes the target of a spell or ability you control for the first time each turn, put a +1/+1 counter on it.", scryfallImage("Heartfire Hero", "blb")),
        TcgCardEntity("mtg:blb:6", "mtg:blb", TcgGame.MTG, "Pawpatch Recruit", "187", "Rare", "Creature — Rabbit Warrior", "{G} • 2/1", "Offspring {2}. Trample.", scryfallImage("Pawpatch Recruit", "blb")),

        // MTG: MH3 - Modern Horizons 3
        TcgCardEntity("mtg:mh3:1", "mtg:mh3", TcgGame.MTG, "Ulamog, the Defiler", "015", "Mythic", "Legendary Creature — Eldrazi", "{10} • 7/7", "When you cast this spell, target opponent exiles half their library, rounded up. Ward—Sacrifice two permanents.", scryfallImage("Ulamog, the Defiler", "mh3")),
        TcgCardEntity("mtg:mh3:2", "mtg:mh3", TcgGame.MTG, "Emrakul, the World Anew", "006", "Mythic", "Legendary Creature — Eldrazi", "{12} • 12/12", "When you cast this spell, gain control of all creatures target player controls. Flying, protection from spells and from permanents that were cast this turn.", scryfallImage("Emrakul, the World Anew", "mh3")),
        TcgCardEntity("mtg:mh3:3", "mtg:mh3", TcgGame.MTG, "Ajani, Nacatl Pariah", "237", "Mythic", "Legendary Creature — Cat Warrior", "{1}{W} • 1/2", "When Ajani enters, create a 2/1 white Cat Warrior creature token.", scryfallImage("Ajani, Nacatl Pariah", "mh3")),
        TcgCardEntity("mtg:mh3:4", "mtg:mh3", TcgGame.MTG, "Psychic Frog", "199", "Rare", "Creature — Frog", "{U}{B} • 1/2", "Whenever Psychic Frog deals combat damage to a player or planeswalker, draw a card.", scryfallImage("Psychic Frog", "mh3")),
        TcgCardEntity("mtg:mh3:5", "mtg:mh3", TcgGame.MTG, "Flooded Strand", "220", "Rare", "Land", "Land", "{T}, Pay 1 life, Sacrifice Flooded Strand: Search your library for a Plains or Island card, put it onto the battlefield, then shuffle.", scryfallImage("Flooded Strand", "mh3")),
        TcgCardEntity("mtg:mh3:6", "mtg:mh3", TcgGame.MTG, "Nadu, Winged Wisdom", "193", "Rare", "Legendary Creature — Bird Wizard", "{1}{G}{U} • 3/4", "Flying. Creatures you control have triggered card advantage.", scryfallImage("Nadu, Winged Wisdom", "mh3")),

        // MTG: LCC - Lost Caverns of Ixalan Commander (Veloci-Ramp-Tor Precon highlights)
        TcgCardEntity("mtg:lcc:1", "mtg:lcc", TcgGame.MTG, "Pantlaza, Sun-Favored", "004", "Mythic", "Legendary Creature — Dinosaur", "{2}{R}{G}{W} • 4/4", "Whenever Pantlaza or another Dinosaur you control enters, you may discover X, where X is that creature's toughness.", scryfallImage("Pantlaza, Sun-Favored", "lcc")),
        TcgCardEntity("mtg:lcc:2", "mtg:lcc", TcgGame.MTG, "Gishath, Sun's Avatar", "270", "Mythic", "Legendary Creature — Dinosaur Avatar", "{5}{R}{G}{W} • 7/6", "Vigilance, trample, haste.", scryfallImage("Gishath, Sun's Avatar", "lcc")),
        TcgCardEntity("mtg:lcc:3", "mtg:lcc", TcgGame.MTG, "Wrathful Raptors", "088", "Rare", "Creature — Dinosaur", "{4}{R} • 5/5", "Trample. Whenever a Dinosaur you control is dealt damage, it deals that much damage to any target that isn't a Dinosaur.", scryfallImage("Wrathful Raptors", "lcc")),
        TcgCardEntity("mtg:lcc:4", "mtg:lcc", TcgGame.MTG, "Sol Ring", "313", "Uncommon", "Artifact", "{1}", "{T}: Add {C}{C}.", scryfallImage("Sol Ring", "lcc")),
        TcgCardEntity("mtg:lcc:5", "mtg:lcc", TcgGame.MTG, "Arcane Signet", "299", "Common", "Artifact", "{2}", "{T}: Add one mana of any color in your commander's color identity.", scryfallImage("Arcane Signet", "lcc")),
        TcgCardEntity("mtg:lcc:6", "mtg:lcc", TcgGame.MTG, "Command Tower", "325", "Common", "Land", "Land", "{T}: Add one mana of any color in your commander's color identity.", scryfallImage("Command Tower", "lcc")),

        // MTG: BLC - Bloomburrow Commander (Squirreled Away Precon highlights)
        TcgCardEntity("mtg:blc:1", "mtg:blc", TcgGame.MTG, "Hazel of the Rootbloom", "002", "Mythic", "Legendary Creature — Squirrel Druid", "{2}{B}{G} • 3/5", "At the beginning of your end step, create a token that's a copy of target token you control.", scryfallImage("Hazel of the Rootbloom", "blc")),
        TcgCardEntity("mtg:blc:2", "mtg:blc", TcgGame.MTG, "Chatterfang, Squirrel General", "218", "Mythic", "Legendary Creature — Squirrel Warrior", "{2}{G} • 3/3", "Forestwalk. If one or more tokens would be created under your control, those tokens plus that many 1/1 green Squirrel creature tokens are created instead.", scryfallImage("Chatterfang, Squirrel General", "blc")),
        TcgCardEntity("mtg:blc:3", "mtg:blc", TcgGame.MTG, "Toski, Bearer of Secrets", "241", "Rare", "Legendary Creature — Squirrel", "{3}{G} • 1/1", "This spell can't be countered. Indestructible. Whenever a creature you control deals combat damage to a player, draw a card.", scryfallImage("Toski, Bearer of Secrets", "blc")),
        TcgCardEntity("mtg:blc:4", "mtg:blc", TcgGame.MTG, "Sol Ring", "284", "Uncommon", "Artifact", "{1}", "{T}: Add {C}{C}.", scryfallImage("Sol Ring", "blc")),
        TcgCardEntity("mtg:blc:5", "mtg:blc", TcgGame.MTG, "Arcane Signet", "260", "Common", "Artifact", "{2}", "{T}: Add one mana of any color in your commander's color identity.", scryfallImage("Arcane Signet", "blc")),
        TcgCardEntity("mtg:blc:6", "mtg:blc", TcgGame.MTG, "Command Tower", "298", "Common", "Land", "Land", "{T}: Add one mana of any color in your commander's color identity.", scryfallImage("Command Tower", "blc"))
    )

    val initialPrecons: List<PreconDeckEntity> = listOf(
        PreconDeckEntity(
            id = "precon:mtg:fdn-starter-collection",
            game = TcgGame.MTG,
            name = "Foundations Starter Collection",
            category = "Box Set",
            releaseYear = 2024,
            setCode = "FDN",
            description = "Complete Foundations Starter Collection box with all 380+ staple cards, Commander staples, and lands.",
            deckFileName = "StarterCollection_FDN"
        ),
        PreconDeckEntity(
            id = "precon:mtg:fdn-beginner-box",
            game = TcgGame.MTG,
            name = "Foundations Beginner Box",
            category = "Beginner Box",
            releaseYear = 2024,
            setCode = "FDN",
            description = "Guided Foundations Beginner Box featuring Jumpstart-style half-decks and iconic reprints."
        ),
        PreconDeckEntity(
            id = "precon:ygo:sdwd",
            game = TcgGame.YUGIOH,
            name = "Structure Deck: Blue-Eyes White Destiny",
            category = "Structure Deck",
            releaseYear = 2025,
            setCode = "SDWD",
            description = "Iconic Blue-Eyes White Dragon strategy with Synchro support and competitive staples like Ash Blossom."
        ),
        PreconDeckEntity(
            id = "precon:ygo:sr14",
            game = TcgGame.YUGIOH,
            name = "Structure Deck: Fire Kings",
            category = "Structure Deck",
            releaseYear = 2023,
            setCode = "SR14",
            description = "Explosive FIRE destruction archetype featuring Sacred Fire King Garunix, Ponix, and Infinite Impermanence."
        ),
        PreconDeckEntity(
            id = "precon:mtg:lcc-veloci",
            game = TcgGame.MTG,
            name = "Veloci-Ramp-Tor",
            category = "Commander Precon",
            releaseYear = 2023,
            setCode = "LCC",
            description = "Naya Dinosaur ramp deck led by Pantlaza, Sun-Favored from The Lost Caverns of Ixalan."
        ),
        PreconDeckEntity(
            id = "precon:mtg:blc-squirrel",
            game = TcgGame.MTG,
            name = "Squirreled Away",
            category = "Commander Precon",
            releaseYear = 2024,
            setCode = "BLC",
            description = "Golgari token & squirrel swarm deck led by Hazel of the Rootbloom from Bloomburrow."
        )
    )

    val initialPreconCards: List<PreconCardEntity> = listOf(
        // Blue-Eyes White Destiny
        PreconCardEntity(preconId = "precon:ygo:sdwd", cardId = "ygo:SDWD:89631139", cardName = "Blue-Eyes White Dragon", quantityInDeck = 3),
        PreconCardEntity(preconId = "precon:ygo:sdwd", cardId = "ygo:SDWD:38517737", cardName = "Blue-Eyes Alternative White Dragon", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:ygo:sdwd", cardId = "ygo:SDWD:8240199", cardName = "Sage with Eyes of Blue", quantityInDeck = 2),
        PreconCardEntity(preconId = "precon:ygo:sdwd", cardId = "ygo:SDWD:59822133", cardName = "Blue-Eyes Spirit Dragon", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:ygo:sdwd", cardId = "ygo:SDWD:43973174", cardName = "The White Stone of Ancients", quantityInDeck = 2),
        PreconCardEntity(preconId = "precon:ygo:sdwd", cardId = "ygo:SDWD:14558127", cardName = "Ash Blossom & Joyous Spring", quantityInDeck = 1),

        // Fire Kings
        PreconCardEntity(preconId = "precon:ygo:sr14", cardId = "ygo:SR14:37910722", cardName = "Legendary Fire King Ponix", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:ygo:sr14", cardId = "ygo:SR14:98385955", cardName = "Sacred Fire King Garunix", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:ygo:sr14", cardId = "ygo:SR14:57554544", cardName = "Fire King Island", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:ygo:sr14", cardId = "ygo:SR14:57103969", cardName = "Fire Formation - Tenki", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:ygo:sr14", cardId = "ygo:SR14:10045474", cardName = "Infinite Impermanence", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:ygo:sr14", cardId = "ygo:SR14:59438930", cardName = "Droll & Lock Bird", quantityInDeck = 1),

        // Veloci-Ramp-Tor
        PreconCardEntity(preconId = "precon:mtg:lcc-veloci", cardId = "mtg:lcc:1", cardName = "Pantlaza, Sun-Favored", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:lcc-veloci", cardId = "mtg:lcc:2", cardName = "Gishath, Sun's Avatar", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:lcc-veloci", cardId = "mtg:lcc:3", cardName = "Wrathful Raptors", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:lcc-veloci", cardId = "mtg:lcc:4", cardName = "Sol Ring", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:lcc-veloci", cardId = "mtg:lcc:5", cardName = "Arcane Signet", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:lcc-veloci", cardId = "mtg:lcc:6", cardName = "Command Tower", quantityInDeck = 1),

        // Squirreled Away
        PreconCardEntity(preconId = "precon:mtg:blc-squirrel", cardId = "mtg:blc:1", cardName = "Hazel of the Rootbloom", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:blc-squirrel", cardId = "mtg:blc:2", cardName = "Chatterfang, Squirrel General", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:blc-squirrel", cardId = "mtg:blc:3", cardName = "Toski, Bearer of Secrets", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:blc-squirrel", cardId = "mtg:blc:4", cardName = "Sol Ring", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:blc-squirrel", cardId = "mtg:blc:5", cardName = "Arcane Signet", quantityInDeck = 1),
        PreconCardEntity(preconId = "precon:mtg:blc-squirrel", cardId = "mtg:blc:6", cardName = "Command Tower", quantityInDeck = 1)
    )
}
