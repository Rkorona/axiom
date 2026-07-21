package io.axiom.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [ProjectEntity].
 * All query results are exposed as [Flow] for reactive UI updates.
 */
@Dao
interface ProjectDao {

    /** All projects, pinned first then most-recently-opened. */
    @Query("SELECT * FROM projects ORDER BY isPinned DESC, lastOpened DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    /** The [limit] most recently opened projects. */
    @Query("SELECT * FROM projects ORDER BY isPinned DESC, lastOpened DESC LIMIT :limit")
    fun observeRecent(limit: Int = 8): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE projects SET lastOpened = :time WHERE id = :id")
    suspend fun updateLastOpened(id: Long, time: Long)

    @Query("UPDATE projects SET isPinned = :pinned WHERE id = :id")
    suspend fun updatePinned(id: Long, pinned: Boolean)

    @Query("UPDATE projects SET lastEditedFile = :file WHERE id = :id")
    suspend fun updateLastEditedFile(id: Long, file: String)
}
