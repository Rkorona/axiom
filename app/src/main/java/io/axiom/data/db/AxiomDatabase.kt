package io.axiom.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Axiom 线程安全的 Room 数据库单例。
 */
@Database(
    entities     = [ProjectEntity::class],
    version      = 1,
    exportSchema = false
)
abstract class AxiomDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AxiomDatabase? = null

        fun getInstance(context: Context): AxiomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AxiomDatabase::class.java,
                    "axiom.db"
                )
                .fallbackToDestructiveMigration() // 🛡️ 边界防错：开发演进期自动降级重建，避免 Schema 验证破坏
                .build()
                .also { INSTANCE = it }
            }
    }
}