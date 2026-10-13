package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AgentEntity
import com.example.data.AiCreditEvaluationResult
import com.example.data.AiCreditScoringEngine
import com.example.data.CreditProfileEntity
import com.example.data.KifaruDatabase
import com.example.data.KifaruRepository
import com.example.data.SimulatedFinancialMetrics
import com.example.data.SimulationPreset
import com.example.data.TransactionEntity
import com.example.data.WalletEntity
import com.example.data.YieldVaultEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiMessage(
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class KifaruViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KifaruRepository

    val wallets: StateFlow<List<WalletEntity>>
    val transactions: StateFlow<List<TransactionEntity>>
    val agents: StateFlow<List<AgentEntity>>
    val yieldVaults: StateFlow<List<YieldVaultEntity>>
    val creditProfile: StateFlow<CreditProfileEntity?>

    private val _currentLanguage = MutableStateFlow("EN") // "EN" or "SW"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _displayCurrency = MutableStateFlow("KES") // "KES" or "USD"
    val displayCurrency: StateFlow<String> = _displayCurrency.asStateFlow()

    val fxRateKesToUsd: Double = 129.50 // 1 USD = 129.50 KES

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    private val _simulatedMetrics = MutableStateFlow(SimulatedFinancialMetrics())
    val simulatedMetrics: StateFlow<SimulatedFinancialMetrics> = _simulatedMetrics.asStateFlow()

    private val _creditEvaluation = MutableStateFlow(AiCreditScoringEngine.calculateScore(SimulatedFinancialMetrics()))
    val creditEvaluation: StateFlow<AiCreditEvaluationResult> = _creditEvaluation.asStateFlow()

    private val _selectedPresetId = MutableStateFlow("DEFAULT_PROFILE")
    val selectedPresetId: StateFlow<String> = _selectedPresetId.asStateFlow()

    private val _isScoreCalculating = MutableStateFlow(false)
    val isScoreCalculating: StateFlow<Boolean> = _isScoreCalculating.asStateFlow()

    private val _aiCreditAdvisoryText = MutableStateFlow<String?>(null)
    val aiCreditAdvisoryText: StateFlow<String?> = _aiCreditAdvisoryText.asStateFlow()

    private val _isAdvisoryLoading = MutableStateFlow(false)
    val isAdvisoryLoading: StateFlow<Boolean> = _isAdvisoryLoading.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                "AI",
                "Hujambo! I am Kifaru AI, your blockchain financial advisor. Ask me about zero-fee transfers, 13.5% Treasury Bill yields, AI micro-loans, or M-Pesa cost comparisons."
            )
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        val db = KifaruDatabase.getDatabase(application)
        repository = KifaruRepository(db)

        wallets = repository.wallets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        agents = repository.agents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        yieldVaults = repository.yieldVaults.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        creditProfile = repository.creditProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        viewModelScope.launch(Dispatchers.IO) {
            repository.seedInitialDataIfNecessary()
        }
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == "EN") "SW" else "EN"
    }

    fun toggleDisplayCurrency() {
        _displayCurrency.value = if (_displayCurrency.value == "KES") "USD" else "KES"
        showFeedback(if (_displayCurrency.value == "USD") "Display converted to USD ($1 = KES 129.50)" else "Display converted to KES")
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
        showFeedback(if (_isOfflineMode.value) "Switched to USSD/Encrypted SMS Offline Mode (*789#)" else "Connected to Base L2 Blockchain Network")
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    private fun showFeedback(msg: String) {
        _actionFeedback.value = msg
    }

    fun sendP2P(recipient: String, amountKes: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.sendP2P(recipient, amountKes, notes)
            result.onSuccess { hash ->
                showFeedback("Sent KES ${"%.2f".format(amountKes)} to $recipient with ZERO Fees! Tx: $hash")
            }.onFailure { err ->
                showFeedback("P2P Transfer Failed: ${err.message}")
            }
        }
    }

    fun payTillOrBill(type: String, targetNumber: String, amountKes: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.payTillOrBill(type, targetNumber, amountKes, notes)
            result.onSuccess { hash ->
                showFeedback("Paid KES ${"%.2f".format(amountKes)} to $type $targetNumber. Tx: $hash")
            }.onFailure { err ->
                showFeedback("Payment Failed: ${err.message}")
            }
        }
    }

    fun swapCurrency(fromCurrency: String, toCurrency: String, amountFrom: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.swapCurrency(fromCurrency, toCurrency, amountFrom)
            result.onSuccess { hash ->
                showFeedback("Swapped $amountFrom $fromCurrency -> $toCurrency instantly! Tx: $hash")
            }.onFailure { err ->
                showFeedback("Swap Failed: ${err.message}")
            }
        }
    }

    fun depositYieldVault(vaultCode: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.depositYieldVault(vaultCode, amount)
            result.onSuccess { hash ->
                showFeedback("Deposited $amount into Vault! Start earning daily yield. Tx: $hash")
            }.onFailure { err ->
                showFeedback("Deposit Failed: ${err.message}")
            }
        }
    }

    fun applyCreditLoan(amountKes: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.applyCreditLoan(amountKes)
            result.onSuccess { hash ->
                showFeedback("Kifaru Boost Micro-loan of KES $amountKes disbursed instantly to wallet!")
            }.onFailure { err ->
                showFeedback("Loan Application Failed: ${err.message}")
            }
        }
    }

    fun repayCreditLoan(amountKes: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.repayCreditLoan(amountKes)
            result.onSuccess { hash ->
                showFeedback("Micro-loan of KES $amountKes successfully settled! Credit score boosted (+5 pts).")
            }.onFailure { err ->
                showFeedback("Repayment Failed: ${err.message}")
            }
        }
    }

    fun selectSimulationPreset(presetId: String) {
        _selectedPresetId.value = presetId
        val preset = AiCreditScoringEngine.getScenarioPresets().find { it.id == presetId }
        if (preset != null) {
            _simulatedMetrics.value = preset.metrics
            recalculateCreditScore(preset.metrics)
        }
    }

    fun updateSimulatedMetrics(metrics: SimulatedFinancialMetrics) {
        _simulatedMetrics.value = metrics
        recalculateCreditScore(metrics)
    }

    fun recalculateCreditScore(customMetrics: SimulatedFinancialMetrics? = null) {
        val metricsToEvaluate = customMetrics ?: _simulatedMetrics.value
        _isScoreCalculating.value = true
        val eval = AiCreditScoringEngine.calculateScore(metricsToEvaluate)
        _creditEvaluation.value = eval
        _isScoreCalculating.value = false
    }

    fun applySimulationScoreToLiveProfile() {
        val eval = _creditEvaluation.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCreditProfile(
                score = eval.overallScore,
                maxLimit = eval.maxPreApprovedLimitKes,
                analysis = eval.aiUnderwritingRationaleEn
            )
            showFeedback("Saved AI Underwriting: Score ${eval.overallScore} and pre-approved limit of KES %,.0f updated!".format(eval.maxPreApprovedLimitKes))
        }
    }

    fun requestAiCreditCoaching(score: Int, riskTier: String) {
        _isAdvisoryLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val isSwahili = _currentLanguage.value == "SW"
                val prompt = "User has an AI-calculated credit score of $score ($riskTier tier) based on Kenyan mobile money cashflow velocity, tokenized Treasury Bill staking, and Base L2 on-chain smart contract transactions. Provide 3 high-impact, practical financial tips to reach 820+ prime tier, emphasizing zero-fee Kifaru transactions, 13.5% Treasury Bill vaults, and avoiding predatory daily Fuliza loans. Respond in " + (if (isSwahili) "Kiswahili" else "English") + " in 2-3 short bullet points."

                if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                    val json = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().put("text", prompt))
                                })
                            })
                        })
                    }

                    val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val respStr = response.body?.string() ?: ""
                        val respJson = JSONObject(respStr)
                        val text = respJson
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        _aiCreditAdvisoryText.value = text
                        _isAdvisoryLoading.value = false
                        return@launch
                    }
                }

                // Smart local fallback if offline or API key missing
                val fallbackText = if (isSwahili) {
                    "🎯 **Ushauri wa AI kwa Alama Yako ($score)**:\n• **Ongeza Akiba ya T-Bills**: Wekeza KES 5,000+ kwenye Vault ya Hatifungani (13.5% APY) ili kupata alama +25.\n• **Tumia Till ya Kifaru**: Mzunguko thabiti wa biashara unathibitisha uwezo wako wa kulipa.\n• **Epuka Riba Kubwa**: Kifaru inatoza 1.2%/mwezi pekee badala ya Fuliza ya kila siku."
                } else {
                    "🎯 **AI Underwriting Coaching for Score $score**:\n• **Accelerate T-Bill Staking**: Allocating KES 5,000+ into the 91-Day Treasury Bill vault (13.5% APY) boosts your collateral weight by +25 points.\n• **Funnel Till & Utility Payments**: Transacting via Kifaru Zero-Fee P2P and Paybill builds verifiable Base L2 smart contract provenance.\n• **Maintain 100% Repayment Punctuality**: Timely settlement triggers automatic limit upgrades up to KES 150,000+ at ultra-low 0.9% monthly rates."
                }
                _aiCreditAdvisoryText.value = fallbackText
            } catch (e: Exception) {
                _aiCreditAdvisoryText.value = if (_currentLanguage.value == "SW") {
                    "Wekeza kwenye Hatifungani za Serikali (13.5% APY) na ulipie bili kupitia Kifaru Pay ili kuongeza alama yako ya mkopo hadi 800+."
                } else {
                    "Maximize your 13.5% Treasury Bill holdings and route daily merchant transactions through Kifaru Pay to unlock an 800+ credit tier."
                }
            } finally {
                _isAdvisoryLoading.value = false
            }
        }
    }

    fun executeAgentCashIn(agentName: String, amountKes: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.executeAgentCashIn(agentName, amountKes)
            result.onSuccess {
                showFeedback("Cash-In Successful: Received KES $amountKes from $agentName")
            }.onFailure { err ->
                showFeedback("Cash-In Failed: ${err.message}")
            }
        }
    }

    fun executeAgentCashOut(agentName: String, amountKes: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.executeAgentCashOut(agentName, amountKes)
            result.onSuccess {
                showFeedback("Cash-Out Successful: Withdrew KES $amountKes at $agentName")
            }.onFailure { err ->
                showFeedback("Cash-Out Failed: ${err.message}")
            }
        }
    }

    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return

        val userMsg = AiMessage("USER", prompt)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val isSwahili = _currentLanguage.value == "SW" || prompt.contains("hujambo", ignoreCase = true) || prompt.contains("habari", ignoreCase = true)

                if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                    val systemPrompt = "You are Kifaru AI, an expert fintech financial advisor for Kenya & East Africa. Explain concepts in a friendly, concise manner in " + (if (isSwahili) "Kiswahili" else "English") + ". Highlight zero-fee transactions, 13.5% Treasury Bill yields, and decentralized mobile money."

                    val json = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().put("text", "$systemPrompt\n\nUser Question: $prompt"))
                                })
                            })
                        })
                    }

                    val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val respStr = response.body?.string() ?: ""
                        val respJson = JSONObject(respStr)
                        val text = respJson
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        _aiMessages.value = _aiMessages.value + AiMessage("AI", text)
                        _isAiLoading.value = false
                        return@launch
                    }
                }

                // Fallback intelligent response engine
                val fallbackResponse = generateFallbackAiResponse(prompt, isSwahili)
                _aiMessages.value = _aiMessages.value + AiMessage("AI", fallbackResponse)
            } catch (e: Exception) {
                val fallback = generateFallbackAiResponse(prompt, _currentLanguage.value == "SW")
                _aiMessages.value = _aiMessages.value + AiMessage("AI", fallback)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private fun generateFallbackAiResponse(prompt: String, isSwahili: Boolean): String {
        val p = prompt.lowercase()
        return when {
            p.contains("fee") || p.contains("cost") || p.contains("m-pesa") || p.contains("mpesa") || p.contains("ada") -> {
                if (isSwahili) {
                    "Kifaru Pay haitozi ada yoyote kwa kutuma fedha (Zero P2P Fees). M-Pesa inatoza hadi KES 108 kwa miamala mikubwa, lakini Kifaru inatumia Base Layer-2 kupunguza gharama hadi sifuri. Wafanyabiashara wanalipa 0.1% pekee badala ya 1.5% ya Till!"
                } else {
                    "Kifaru Pay eliminates Send Money transaction fees completely (Zero P2P Fees)! Unlike M-Pesa which charges up to KES 108 on transfers, Kifaru uses ERC-4337 Account Abstraction on Base L2 to sponsor gas. Merchants pay only 0.1% vs 1.5% on Till numbers."
                }
            }
            p.contains("yield") || p.contains("treasury") || p.contains("tbill") || p.contains("interest") || p.contains("akiba") -> {
                if (isSwahili) {
                    "Unaweza kuwekeza akiba yako kwenye Hatifungani za Serikali (Tokenized 91-Day Treasury Bills) upate faida ya 13.5% APY kila mwaka. Tofauti na M-Shwari, faida yako inahesabiwa kila siku na unaweza kutoa muda wowote."
                } else {
                    "Kifaru Vaults allow you to earn 13.5% APY on idle KES balances through tokenized Government Treasury Bills (91-Day) and 12.2% in East Africa MMFs. Yield accrues daily with instant 24/7 liquidity."
                }
            }
            p.contains("loan") || p.contains("fuliza") || p.contains("credit") || p.contains("mkopo") -> {
                if (isSwahili) {
                    "Kifaru AI Credit Engine inakagua historia yako ya Till, Chama, na miamala bila karatasi. Inakupa mkopo wa papo hapo kwa riba nafuu ya 1.2% kwa mwezi badala ya Fuliza inayotoza ada za kila siku."
                } else {
                    "Our Decentralized AI Credit Engine evaluates your Till cashflows, Chama participation, and mobile history to offer instant micro-loans at 1.2% monthly interest — vastly cheaper than Fuliza's compounded daily fees."
                }
            }
            p.contains("agent") || p.contains("cash") || p.contains("duka") -> {
                if (isSwahili) {
                    "Mtandao wetu wa Agent za Kifaru unajumuisha Maduka, Bodaboda, na Farmasi zilizo karibu nawe. Hazihitaji mtaji mkubwa wa Safaricom float kwa sababu zinatumia USDC na KES mtawalia."
                } else {
                    "Kifaru replaces traditional M-Pesa agents with a decentralized liquidity network of local Dukas, Bodabodas, and Pharmacies equipped with dynamic float management and instant QR cash-in/cash-out."
                }
            }
            else -> {
                if (isSwahili) {
                    "Kifaru Pay ni mfumo wa kisasa wa kifedha wa blockchain Kenya. Tuma fedha bila ada, pata faida ya 13.5% kwenye Akiba ya T-Bills, na upate mikopo ya haraka!"
                } else {
                    "Kifaru Pay is Kenya's investor-grade blockchain payment ecosystem. Enjoy zero P2P fees, 13.5% Treasury Bill yield vaults, cross-border remittances under 5s, and offline USSD support!"
                }
            }
        }
    }
}
