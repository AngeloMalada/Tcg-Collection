package com.tcgvault.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tcgvault.app.data.local.dao.TcgDao
import com.tcgvault.app.data.local.entity.CollectionEntryEntity
import com.tcgvault.app.data.local.entity.PreconCardEntity
import com.tcgvault.app.data.local.entity.PreconDeckEntity
import com.tcgvault.app.data.local.entity.TcgCardEntity
import com.tcgvault.app.data.local.entity.TcgSetEntity
import com.tcgvault.app.data.model.TcgGame

class TcgConverters {
    @TypeConverter
    fun fromGame(game: TcgGame): String = game.name

    @TypeConverter
    fun toGame(value: String): TcgGame = TcgGame.valueOf(value)
}

@Database(
    entities = [
        TcgSetEntity::class,
        TcgCardEntity::class,
        CollectionEntryEntity::class,
        PreconDeckEntity::class,
        PreconCardEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(TcgConverters::class)
abstract class TcgDatabase : RoomDatabase() {
    abstract fun tcgDao(): TcgDao

    companion object {
        @Volatile
        private var INSTANCE: TcgDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE precon_decks ADD COLUMN deckFileName TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tcg_sets ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE precon_decks ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tcg_cards ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): TcgDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TcgDatabase::class.java,
                    "tcg_vault_local.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
