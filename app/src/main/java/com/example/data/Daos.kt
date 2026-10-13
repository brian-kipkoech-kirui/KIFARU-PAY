package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE currency = :currency")
    suspend fun getWalletByCurrency(currency: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: WalletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletEntity>)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity)
}

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents ORDER BY distanceKm ASC")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgents(agents: List<AgentEntity>)

    @Update
    suspend fun updateAgent(agent: AgentEntity)
}

@Dao
interface YieldVaultDao {
    @Query("SELECT * FROM yield_vaults")
    fun getAllVaults(): Flow<List<YieldVaultEntity>>

    @Query("SELECT * FROM yield_vaults WHERE code = :code")
    suspend fun getVaultByCode(code: String): YieldVaultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaults(vaults: List<YieldVaultEntity>)

    @Update
    suspend fun updateVault(vault: YieldVaultEntity)
}

@Dao
interface CreditProfileDao {
    @Query("SELECT * FROM credit_profile WHERE id = 1")
    fun getCreditProfile(): Flow<CreditProfileEntity?>

    @Query("SELECT * FROM credit_profile WHERE id = 1")
    suspend fun getCreditProfileOnce(): CreditProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCreditProfile(profile: CreditProfileEntity)
}
