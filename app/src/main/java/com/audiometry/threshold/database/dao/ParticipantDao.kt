package com.audiometry.threshold.database.dao

import androidx.room.*
import com.audiometry.threshold.database.entity.Participant

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants ORDER BY nomeCompleto ASC")
    suspend fun getAll(): List<Participant>

    @Query("""
        SELECT * FROM participants
        WHERE nomeCompleto LIKE '%' || :query || '%'
        ORDER BY nomeCompleto ASC
    """)
    suspend fun search(query: String): List<Participant>

    @Query("""
        SELECT * FROM participants
        WHERE (:status = '' OR statusAuditivo = :status)
          AND (:reeval = '' OR necessitaReavaliacao = :reeval)
          AND (:testType = '' OR testeRealizado = :testType)
        ORDER BY nomeCompleto ASC
    """)
    suspend fun filter(status: String, reeval: String, testType: String): List<Participant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(participant: Participant): Long

    @Update
    suspend fun update(participant: Participant)

    @Delete
    suspend fun delete(participant: Participant)

    @Query("SELECT COUNT(*) FROM participants")
    suspend fun count(): Int
}
