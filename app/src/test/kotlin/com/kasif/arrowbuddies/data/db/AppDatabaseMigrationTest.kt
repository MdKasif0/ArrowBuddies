package com.kasif.arrowbuddies.data.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AppDatabaseMigrationTest {

    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setUp() {
        val context: Context = RuntimeEnvironment.getApplication()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("migration_test.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Version 1 schema: placeholder table
                    db.execSQL("CREATE TABLE IF NOT EXISTS `placeholder` (`id` TEXT NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("INSERT INTO `placeholder` (`id`) VALUES ('test_placeholder')")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        db = FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    @After
    fun tearDown() {
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }

    @Test
    fun migration1To2CreatesAllMetaProgressionTablesAndDefaultProfile() {
        // Assert initial v1 state
        val v1Cursor = db.query("SELECT id FROM placeholder")
        assertThat(v1Cursor.count).isEqualTo(1)
        v1Cursor.close()

        // Execute Migration 1 -> 2
        AppDatabaseMigrations.MIGRATION_1_2.migrate(db)

        // Verify player_profile table and default row
        val profileCursor = db.query("SELECT id, berries, hints, undos, totalRescued, currentLevelIndex FROM player_profile WHERE id = 1")
        assertThat(profileCursor.moveToFirst()).isTrue()
        assertThat(profileCursor.getInt(0)).isEqualTo(1)
        assertThat(profileCursor.getInt(1)).isEqualTo(100)
        assertThat(profileCursor.getInt(2)).isEqualTo(3)
        assertThat(profileCursor.getInt(3)).isEqualTo(3)
        assertThat(profileCursor.getInt(4)).isEqualTo(0)
        assertThat(profileCursor.getInt(5)).isEqualTo(1)
        profileCursor.close()

        // Verify level_progress table by inserting and reading
        db.execSQL("INSERT INTO level_progress (levelId, bestStars, bestMistakes, completedAt) VALUES ('w1-l01', 3, 0, 1000)")
        val levelCursor = db.query("SELECT bestStars, bestMistakes FROM level_progress WHERE levelId = 'w1-l01'")
        assertThat(levelCursor.moveToFirst()).isTrue()
        assertThat(levelCursor.getInt(0)).isEqualTo(3)
        assertThat(levelCursor.getInt(1)).isEqualTo(0)
        levelCursor.close()

        // Verify species_progress table
        db.execSQL("INSERT INTO species_progress (species, rescuedCount, tier, equippedAccessory, firstRescuedAt) VALUES ('BUNNY', 15, 'BRONZE', 'BOW', 2000)")
        val speciesCursor = db.query("SELECT tier, equippedAccessory FROM species_progress WHERE species = 'BUNNY'")
        assertThat(speciesCursor.moveToFirst()).isTrue()
        assertThat(speciesCursor.getString(0)).isEqualTo("BRONZE")
        assertThat(speciesCursor.getString(1)).isEqualTo("BOW")
        speciesCursor.close()

        // Verify decor_item table
        db.execSQL("INSERT INTO decor_item (id, owned, slot, zoneId, costBerries) VALUES ('decor_pond_1', 1, 'z1_s1', 1, 150)")
        val decorCursor = db.query("SELECT owned, costBerries FROM decor_item WHERE id = 'decor_pond_1'")
        assertThat(decorCursor.moveToFirst()).isTrue()
        assertThat(decorCursor.getInt(0)).isEqualTo(1)
        assertThat(decorCursor.getInt(1)).isEqualTo(150)
        decorCursor.close()

        // Verify daily_record table
        db.execSQL("INSERT INTO daily_record (date, completed, stars, mistakes, completedAt) VALUES ('20261110', 1, 3, 0, 3000)")
        val dailyCursor = db.query("SELECT completed, stars FROM daily_record WHERE date = '20261110'")
        assertThat(dailyCursor.moveToFirst()).isTrue()
        assertThat(dailyCursor.getInt(0)).isEqualTo(1)
        dailyCursor.close()

        // Verify achievement table
        db.execSQL("INSERT INTO achievement (id, unlockedAt, progress, maxProgress) VALUES ('first_rescue', 4000, 1, 1)")
        val achCursor = db.query("SELECT unlockedAt, progress FROM achievement WHERE id = 'first_rescue'")
        assertThat(achCursor.moveToFirst()).isTrue()
        assertThat(achCursor.getLong(0)).isEqualTo(4000L)
        achCursor.close()
    }
}
