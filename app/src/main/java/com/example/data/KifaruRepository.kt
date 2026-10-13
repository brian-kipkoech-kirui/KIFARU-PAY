package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class KifaruRepository(private val db: KifaruDatabase) {
    val wallets: Flow<List<WalletEntity>> = db.walletDao().getAllWallets()
    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val agents: Flow<List<AgentEntity>> = db.agentDao().getAllAgents()
    val yieldVaults: Flow<List<YieldVaultEntity>> = db.yieldVaultDao().getAllVaults()
    val creditProfile: Flow<CreditProfileEntity?> = db.creditProfileDao().getCreditProfile()

    suspend fun seedInitialDataIfNecessary() {
        // Seed Wallets
        val existingKes = db.walletDao().getWalletByCurrency("KES")
        if (existingKes == null) {
            val initialWallets = listOf(
                WalletEntity("KES", 24850.00, 312.40, "0x7F2...KFR89"),
                WalletEntity("USDC", 185.50, 4.25, "0x7F2...KFR89"),
                WalletEntity("USD", 42.00, 0.00, "0x7F2...KFR89"),
                WalletEntity("EUR", 15.00, 0.00, "0x7F2...KFR89")
            )
            db.walletDao().insertWallets(initialWallets)
        }

        // Seed Initial Transactions
        if (db.transactionDao().getTransactionCount() == 0) {
            val txList = listOf(
                TransactionEntity(
                    txType = "P2P_SEND",
                    sender = "You (254712***890)",
                    recipient = "Wanjiku Kamau (254722***112)",
                    amount = 1500.0,
                    currency = "KES",
                    fee = 0.0,
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 1800000, // 30 mins ago
                    blockchainHash = "0x8f2a9c31b" + UUID.randomUUID().toString().take(8),
                    notes = "Chama contribution - 0 Fee"
                ),
                TransactionEntity(
                    txType = "AGENT_CASH_IN",
                    sender = "Mama Mboga Duka Float Node",
                    recipient = "You",
                    amount = 3500.0,
                    currency = "KES",
                    fee = 0.0,
                    status = "PENDING",
                    timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                    blockchainHash = "0x9a3f81e7d" + UUID.randomUUID().toString().take(8),
                    notes = "Agent float cash deposit awaiting node signature"
                ),
                TransactionEntity(
                    txType = "PAYBILL",
                    sender = "You",
                    recipient = "KPLC Electricity (Paybill 888888)",
                    amount = 850.0,
                    currency = "KES",
                    fee = 0.85,
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                    blockchainHash = "0x4b71d9e2a" + UUID.randomUUID().toString().take(8),
                    notes = "Token Purchase"
                ),
                TransactionEntity(
                    txType = "CROSS_BORDER",
                    sender = "You",
                    recipient = "John Doe (London, UK)",
                    amount = 12000.0,
                    currency = "KES",
                    fee = 12.0,
                    status = "FAILED",
                    timestamp = System.currentTimeMillis() - 129600000, // 1.5 days ago
                    blockchainHash = "0x7e2c04f9a" + UUID.randomUUID().toString().take(8),
                    notes = "Insufficient FX liquidity on provider bridge"
                ),
                TransactionEntity(
                    txType = "SAVINGS_DEPOSIT",
                    sender = "KES Wallet",
                    recipient = "Kenya T-Bill Vault (13.5% APY)",
                    amount = 5000.0,
                    currency = "KES",
                    fee = 0.0,
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                    blockchainHash = "0x12c9e710f" + UUID.randomUUID().toString().take(8),
                    notes = "Auto-Yield Staking"
                )
            )
            txList.forEach { db.transactionDao().insertTransaction(it) }
        }

        // Seed Agents
        val agentList = listOf(
            AgentEntity(
                name = "Mama Mboga Duka & Float Node",
                category = "DUKA",
                locationName = "Kilmimani, Ring Road Nairobi",
                distanceKm = 0.2,
                phone = "0711223344",
                floatKes = 125000.0,
                floatUsdc = 850.0,
                rating = 4.9
            ),
            AgentEntity(
                name = "Express Bodaboda Liquidity Hub",
                category = "BODABODA",
                locationName = "Westlands Stage, Nairobi",
                distanceKm = 0.5,
                phone = "0722334455",
                floatKes = 45000.0,
                floatUsdc = 300.0,
                rating = 4.8
            ),
            AgentEntity(
                name = "GoodHealth Pharmacy Cash Node",
                category = "PHARMACY",
                locationName = "Ngong Road Junction",
                distanceKm = 1.1,
                phone = "0733445566",
                floatKes = 280000.0,
                floatUsdc = 1500.0,
                rating = 5.0
            ),
            AgentEntity(
                name = "TotalEnergies Petrol Station",
                category = "PETROL_STATION",
                locationName = "Mombasa Road, Nairobi",
                distanceKm = 2.3,
                phone = "0700112233",
                floatKes = 500000.0,
                floatUsdc = 4000.0,
                rating = 4.9
            )
        )
        db.agentDao().insertAgents(agentList)

        // Seed Yield Vaults
        val vaults = listOf(
            YieldVaultEntity(
                code = "TBILL_91D",
                name = "Kenya Treasury Bills (Tokenized RWA)",
                apy = 13.5,
                invested = 5000.0,
                earned = 285.40,
                term = "91 Days T-Bill Backed"
            ),
            YieldVaultEntity(
                code = "MMF_KES",
                name = "East Africa Money Market Fund",
                apy = 12.2,
                invested = 2000.0,
                earned = 27.00,
                term = "Daily Liquidity"
            ),
            YieldVaultEntity(
                code = "USDC_YIELD",
                name = "USDC Yield Vault (Base L2 Staking)",
                apy = 8.5,
                invested = 50.0,
                earned = 4.25,
                term = "Instant Withdrawal"
            )
        )
        db.yieldVaultDao().insertVaults(vaults)

        // Seed Credit Profile
        db.creditProfileDao().insertOrUpdateCreditProfile(
            CreditProfileEntity(
                id = 1,
                score = 745,
                maxLimit = 50000.0,
                usedLimit = 0.0,
                activeLoan = 0.0,
                interestRate = 1.2, // 1.2% per month vs Fuliza's high daily fee
                scoreAnalysis = "High score driven by consistent Till Cashflow (+120), Chama participation (+80), and salary inflows (+110)."
            )
        )
    }

    suspend fun sendP2P(recipient: String, amountKes: Double, notes: String): Result<String> {
        val kesWallet = db.walletDao().getWalletByCurrency("KES") ?: return Result.failure(Exception("KES Wallet not found"))
        if (kesWallet.balance < amountKes) {
            return Result.failure(Exception("Insufficient KES Balance"))
        }

        val newBalance = kesWallet.balance - amountKes
        db.walletDao().insertOrUpdateWallet(kesWallet.copy(balance = newBalance))

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        val tx = TransactionEntity(
            txType = "P2P_SEND",
            sender = "You",
            recipient = recipient,
            amount = amountKes,
            currency = "KES",
            fee = 0.0, // Strictly Zero Fee!
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            blockchainHash = txHash,
            notes = notes.ifEmpty { "P2P Transfer (Zero Fee)" }
        )
        db.transactionDao().insertTransaction(tx)
        return Result.success(txHash)
    }

    suspend fun payTillOrBill(type: String, targetNumber: String, amountKes: Double, notes: String): Result<String> {
        val kesWallet = db.walletDao().getWalletByCurrency("KES") ?: return Result.failure(Exception("KES Wallet not found"))
        val merchantFee = amountKes * 0.001 // 0.1% flat merchant rate
        val totalDeduction = amountKes

        if (kesWallet.balance < totalDeduction) {
            return Result.failure(Exception("Insufficient KES balance"))
        }

        val newBalance = kesWallet.balance - totalDeduction
        db.walletDao().insertOrUpdateWallet(kesWallet.copy(balance = newBalance))

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        val tx = TransactionEntity(
            txType = if (type == "TILL") "TILL" else "PAYBILL",
            sender = "You",
            recipient = "$type $targetNumber",
            amount = amountKes,
            currency = "KES",
            fee = merchantFee,
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            blockchainHash = txHash,
            notes = notes.ifEmpty { "Merchant Payment" }
        )
        db.transactionDao().insertTransaction(tx)
        return Result.success(txHash)
    }

    suspend fun swapCurrency(fromCurrency: String, toCurrency: String, amountFrom: Double): Result<String> {
        val fromWallet = db.walletDao().getWalletByCurrency(fromCurrency) ?: return Result.failure(Exception("Source wallet missing"))
        val toWallet = db.walletDao().getWalletByCurrency(toCurrency) ?: return Result.failure(Exception("Destination wallet missing"))

        if (fromWallet.balance < amountFrom) {
            return Result.failure(Exception("Insufficient $fromCurrency balance"))
        }

        // FX Rates (1 USD = 129.5 KES, 1 EUR = 141.2 KES, 1 USDC = 129.5 KES)
        fun getRateInKes(curr: String): Double = when (curr) {
            "KES" -> 1.0
            "USD", "USDC" -> 129.50
            "EUR" -> 141.20
            else -> 1.0
        }

        val amountInKes = amountFrom * getRateInKes(fromCurrency)
        val amountTo = amountInKes / getRateInKes(toCurrency)

        db.walletDao().insertOrUpdateWallet(fromWallet.copy(balance = fromWallet.balance - amountFrom))
        db.walletDao().insertOrUpdateWallet(toWallet.copy(balance = toWallet.balance + amountTo))

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        val tx = TransactionEntity(
            txType = "CROSS_BORDER",
            sender = "$fromCurrency Account",
            recipient = "$toCurrency Account",
            amount = amountFrom,
            currency = fromCurrency,
            fee = 0.001, // Sub-cent fee
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            blockchainHash = txHash,
            notes = "Instant Multi-Currency Swap ($amountFrom $fromCurrency -> ${"%.2f".format(amountTo)} $toCurrency)"
        )
        db.transactionDao().insertTransaction(tx)
        return Result.success(txHash)
    }

    suspend fun depositYieldVault(vaultCode: String, amount: Double): Result<String> {
        val vault = db.yieldVaultDao().getVaultByCode(vaultCode) ?: return Result.failure(Exception("Vault not found"))
        val currency = if (vaultCode == "USDC_YIELD") "USDC" else "KES"
        val wallet = db.walletDao().getWalletByCurrency(currency) ?: return Result.failure(Exception("Wallet not found"))

        if (wallet.balance < amount) {
            return Result.failure(Exception("Insufficient $currency balance"))
        }

        db.walletDao().insertOrUpdateWallet(wallet.copy(balance = wallet.balance - amount))
        db.yieldVaultDao().updateVault(vault.copy(invested = vault.invested + amount))

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        val tx = TransactionEntity(
            txType = "SAVINGS_DEPOSIT",
            sender = "$currency Wallet",
            recipient = vault.name,
            amount = amount,
            currency = currency,
            fee = 0.0,
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            blockchainHash = txHash,
            notes = "Staked in ${vault.name} @ ${vault.apy}% APY"
        )
        db.transactionDao().insertTransaction(tx)
        return Result.success(txHash)
    }

    suspend fun applyCreditLoan(amountKes: Double): Result<String> {
        val kesWallet = db.walletDao().getWalletByCurrency("KES") ?: return Result.failure(Exception("KES Wallet not found"))
        val currentProfile = db.creditProfileDao().getCreditProfileOnce()
        val currentActive = currentProfile?.activeLoan ?: 0.0
        val currentUsed = currentProfile?.usedLimit ?: 0.0
        val maxLimit = currentProfile?.maxLimit ?: 50000.0

        if (currentUsed + amountKes > maxLimit) {
            return Result.failure(Exception("Exceeds pre-approved credit limit of KES %,.2f".format(maxLimit)))
        }

        db.walletDao().insertOrUpdateWallet(kesWallet.copy(balance = kesWallet.balance + amountKes))
        if (currentProfile != null) {
            db.creditProfileDao().insertOrUpdateCreditProfile(
                currentProfile.copy(
                    activeLoan = currentActive + amountKes,
                    usedLimit = currentUsed + amountKes
                )
            )
        }

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        val tx = TransactionEntity(
            txType = "CREDIT_LOAN",
            sender = "Kifaru AI Credit Engine",
            recipient = "Your KES Wallet",
            amount = amountKes,
            currency = "KES",
            fee = 0.0,
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            blockchainHash = txHash,
            notes = "Instant Micro-loan disbursed at 1.2% monthly interest"
        )
        db.transactionDao().insertTransaction(tx)
        return Result.success(txHash)
    }

    suspend fun repayCreditLoan(amountKes: Double): Result<String> {
        val kesWallet = db.walletDao().getWalletByCurrency("KES") ?: return Result.failure(Exception("KES Wallet not found"))
        if (kesWallet.balance < amountKes) {
            return Result.failure(Exception("Insufficient KES balance to repay loan"))
        }

        val currentProfile = db.creditProfileDao().getCreditProfileOnce()
        val currentActive = currentProfile?.activeLoan ?: 0.0
        val currentUsed = currentProfile?.usedLimit ?: 0.0
        val repaymentAmount = kotlin.math.min(amountKes, currentActive.coerceAtLeast(amountKes))

        db.walletDao().insertOrUpdateWallet(kesWallet.copy(balance = kesWallet.balance - repaymentAmount))
        if (currentProfile != null) {
            val newActive = (currentActive - repaymentAmount).coerceAtLeast(0.0)
            val newUsed = (currentUsed - repaymentAmount).coerceAtLeast(0.0)
            // Reward early on-time repayment with a score boost!
            val newScore = kotlin.math.min(850, currentProfile.score + 5)
            db.creditProfileDao().insertOrUpdateCreditProfile(
                currentProfile.copy(
                    score = newScore,
                    activeLoan = newActive,
                    usedLimit = newUsed,
                    scoreAnalysis = "Score boosted to $newScore following on-time loan settlement."
                )
            )
        }

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        val tx = TransactionEntity(
            txType = "P2P_SEND",
            sender = "Your KES Wallet",
            recipient = "Kifaru Credit Liquidity Pool",
            amount = repaymentAmount,
            currency = "KES",
            fee = 0.0,
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            blockchainHash = txHash,
            notes = "Micro-loan repayment (On-time score boost +5 pts)"
        )
        db.transactionDao().insertTransaction(tx)
        return Result.success(txHash)
    }

    suspend fun updateCreditProfile(score: Int, maxLimit: Double, analysis: String) {
        val currentProfile = db.creditProfileDao().getCreditProfileOnce()
        val profile = currentProfile?.copy(
            score = score,
            maxLimit = maxLimit,
            scoreAnalysis = analysis
        ) ?: CreditProfileEntity(
            id = 1,
            score = score,
            maxLimit = maxLimit,
            usedLimit = 0.0,
            activeLoan = 0.0,
            interestRate = 1.2,
            scoreAnalysis = analysis
        )
        db.creditProfileDao().insertOrUpdateCreditProfile(profile)
    }

    suspend fun executeAgentCashIn(agentName: String, amountKes: Double): Result<String> {
        val kesWallet = db.walletDao().getWalletByCurrency("KES") ?: return Result.failure(Exception("KES Wallet not found"))
        db.walletDao().insertOrUpdateWallet(kesWallet.copy(balance = kesWallet.balance + amountKes))

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        val tx = TransactionEntity(
            txType = "AGENT_CASH_IN",
            sender = "Agent: $agentName",
            recipient = "Your Wallet",
            amount = amountKes,
            currency = "KES",
            fee = 0.0,
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            blockchainHash = txHash,
            notes = "Agent Liquidity Deposit"
        )
        db.transactionDao().insertTransaction(tx)
        return Result.success(txHash)
    }

    suspend fun executeAgentCashOut(agentName: String, amountKes: Double): Result<String> {
        val kesWallet = db.walletDao().getWalletByCurrency("KES") ?: return Result.failure(Exception("KES Wallet not found"))
        if (kesWallet.balance < amountKes) {
            return Result.failure(Exception("Insufficient KES Balance"))
        }

        db.walletDao().insertOrUpdateWallet(kesWallet.copy(balance = kesWallet.balance - amountKes))

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        val tx = TransactionEntity(
            txType = "AGENT_CASH_OUT",
            sender = "Your Wallet",
            recipient = "Agent: $agentName",
            amount = amountKes,
            currency = "KES",
            fee = 0.0,
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            blockchainHash = txHash,
            notes = "Agent Cash Withdrawal"
        )
        db.transactionDao().insertTransaction(tx)
        return Result.success(txHash)
    }
}
