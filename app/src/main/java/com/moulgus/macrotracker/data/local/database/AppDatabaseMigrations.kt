package com.moulgus.macrotracker.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `meal_templates` (
                `templateID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `meal_template_entries` (
                `templateEntryID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `templateID` INTEGER NOT NULL,
                `productID` INTEGER,
                `productName` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `unitName` TEXT NOT NULL,
                `amountInBaseUnit` REAL NOT NULL,
                `kcal` REAL NOT NULL,
                `protein` REAL NOT NULL,
                `carbs` REAL NOT NULL,
                `fat` REAL NOT NULL,
                FOREIGN KEY(`templateID`) REFERENCES `meal_templates`(`templateID`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`productID`) REFERENCES `products`(`productID`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_meal_template_entries_templateID`
            ON `meal_template_entries` (`templateID`)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_meal_template_entries_productID`
            ON `meal_template_entries` (`productID`)
            """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE `products`
            ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}