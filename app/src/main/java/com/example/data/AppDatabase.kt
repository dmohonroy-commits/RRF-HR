package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AgreementEntity::class, AddressItemEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agreementDao(): AgreementDao
    abstract fun addressDao(): AddressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE agreements ADD COLUMN editCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE agreements ADD COLUMN lastEditDate TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE agreements ADD COLUMN orgLogoPath TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE agreements ADD COLUMN assignedBranch TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE agreements ADD COLUMN dispatchDate TEXT")
                db.execSQL("ALTER TABLE agreements ADD COLUMN verificationStatus TEXT NOT NULL DEFAULT 'অপেক্ষমান'")
                db.execSQL("ALTER TABLE agreements ADD COLUMN verificationDate TEXT")
                db.execSQL("ALTER TABLE agreements ADD COLUMN branchNotes TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rrf_hr_database.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
