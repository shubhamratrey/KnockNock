package com.sillylife.knocknock.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sillylife.knocknock.database.dao.ContactsDao
import com.sillylife.knocknock.database.entities.ContactsEntity


@Database(entities = [ContactsEntity::class], version = 1)
abstract class KnockNockDatabase : RoomDatabase() {

    abstract fun contactsDao(): ContactsDao

    companion object {

        @Volatile
        private var INSTANCE: KnockNockDatabase? = null
        private const val DATABASE_NAME = "KnockNockDatabase"

        fun getInstance(context: Context): KnockNockDatabase? {
            if (INSTANCE == null) {
                synchronized(KnockNockDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            KnockNockDatabase::class.java,
                            DATABASE_NAME).allowMainThreadQueries()
//                            .addMigrations(
//                                    MIGRATION_1_TO_2,
//                                    MIGRATION_2_TO_3,
//                                    MIGRATION_3_TO_4,
//                                    MIGRATION_4_TO_5,
//                                    MIGRATION_5_TO_6,
//                                    MIGRATION_6_TO_7,
//                            )
                            .setJournalMode(JournalMode.AUTOMATIC)
                            .build()
                }
            }
            return INSTANCE
        }
    }
}
