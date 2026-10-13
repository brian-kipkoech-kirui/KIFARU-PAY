package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        AgentEntity::class,
        YieldVaultEntity::class,
        CreditProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KifaruDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun agentDao(): AgentDao
    abstract fun yieldVaultDao(): YieldVaultDao
    abstract fun creditProfileDao(): CreditProfileDao

    companion object {
        @Volatile
        private var INSTANCE: KifaruDatabase? = null

        fun getDatabase(context: Context): KifaruDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KifaruDatabase::class.java,
                    "kifaru_pay_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
