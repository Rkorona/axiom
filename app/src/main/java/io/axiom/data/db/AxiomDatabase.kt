package io.axiom.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Singleton Room database for Axiom.
 * Currently holds one table: [ProjectEntity].
 */
@Database(
    entities  = [ProjectEntity::class],
    version   = 1,
    exportSchema = false
)
abstract class AxiomDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile private var INSTANCE: AxiomDatabase? = null

        fun getInstance(context: Context): AxiomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AxiomDatabase::class.java,
                    "axiom.db"
                ).build().also { INSTANCE = it }
            }
    }
}
