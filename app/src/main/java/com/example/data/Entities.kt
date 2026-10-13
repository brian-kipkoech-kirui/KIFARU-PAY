package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val currency: String, // e.g. "KES", "USDC", "USD", "EUR"
    val balance: Double,
    val yieldEarned: Double,
    val accountHash: String
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val txType: String, // "P2P_SEND", "PAYBILL", "TILL", "CROSS_BORDER", "SAVINGS_DEPOSIT", "SAVINGS_WITHDRAW", "CREDIT_LOAN", "AGENT_CASH_IN", "AGENT_CASH_OUT"
    val sender: String,
    val recipient: String,
    val amount: Double,
    val currency: String,
    val fee: Double,
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val blockchainHash: String,
    val notes: String
)

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "DUKA", "BODABODA", "PHARMACY", "PETROL_STATION", "SUPERMARKET"
    val locationName: String,
    val distanceKm: Double,
    val phone: String,
    val floatKes: Double,
    val floatUsdc: Double,
    val rating: Double
)

@Entity(tableName = "yield_vaults")
data class YieldVaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String, // "TBILL_91D", "MMF_KES", "USDC_YIELD"
    val name: String,
    val apy: Double,
    val invested: Double,
    val earned: Double,
    val term: String
)

@Entity(tableName = "credit_profile")
data class CreditProfileEntity(
    @PrimaryKey val id: Int = 1,
    val score: Int, // 300 to 850
    val maxLimit: Double,
    val usedLimit: Double,
    val activeLoan: Double,
    val interestRate: Double, // % per month
    val scoreAnalysis: String
)
