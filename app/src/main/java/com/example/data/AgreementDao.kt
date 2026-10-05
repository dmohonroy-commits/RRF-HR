package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgreementDao {
    @Query("SELECT * FROM agreements ORDER BY submissionTimestamp DESC")
    fun getAllAgreements(): Flow<List<AgreementEntity>>

    @Query("SELECT * FROM agreements WHERE id = :id LIMIT 1")
    suspend fun getAgreementById(id: Long): AgreementEntity?

    @Query("SELECT * FROM agreements WHERE isSubmittedByThisDevice = 1 ORDER BY submissionTimestamp DESC LIMIT 1")
    fun getLocalWorkerAgreement(): Flow<AgreementEntity?>

    @Query("SELECT * FROM agreements WHERE isSubmittedByThisDevice = 1 ORDER BY submissionTimestamp DESC")
    fun getLocalWorkerAgreements(): Flow<List<AgreementEntity>>

    @Query("SELECT COUNT(*) FROM agreements")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgreement(agreement: AgreementEntity): Long

    @Update
    suspend fun updateAgreement(agreement: AgreementEntity)

    @Delete
    suspend fun deleteAgreement(agreement: AgreementEntity)

    @Query("DELETE FROM agreements WHERE id = :id")
    suspend fun deleteAgreementById(id: Long)

    @Query("DELETE FROM agreements WHERE isSubmittedByThisDevice = 1")
    suspend fun deleteLocalWorkerAgreements()

    @Query("DELETE FROM agreements")
    suspend fun deleteAllAgreements()

    @Query("SELECT * FROM agreements ORDER BY submissionTimestamp DESC")
    suspend fun getAllAgreementsList(): List<AgreementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgreements(agreements: List<AgreementEntity>)

    @Query("UPDATE agreements SET isPrinted = :isPrinted, printDate = :printDate WHERE id = :id")
    suspend fun updatePrintStatus(id: Long, isPrinted: Boolean, printDate: String?)

    @Query("UPDATE agreements SET assignedBranch = :branch, dispatchDate = :dispatchDate, verificationStatus = :status WHERE id = :id")
    suspend fun dispatchToBranch(id: Long, branch: String, dispatchDate: String, status: String = "পাঠানো হয়েছে")

    @Query("UPDATE agreements SET verificationStatus = :status, verificationDate = :verificationDate, branchNotes = :notes WHERE id = :id")
    suspend fun updateBranchVerification(id: Long, status: String, verificationDate: String?, notes: String)
}
