package com.example.data

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class CreditScoreFactor(
    val id: String,
    val titleEn: String,
    val titleSw: String,
    val score: Int, // 0 to 100
    val weightPercent: Int, // e.g. 30 for 30%
    val status: FactorStatus,
    val impactPoints: Int,
    val descriptionEn: String,
    val descriptionSw: String,
    val tipEn: String,
    val tipSw: String
)

enum class FactorStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    ATTENTION_NEEDED
}

data class SimulatedFinancialMetrics(
    val monthlyInflowKes: Double = 45000.0,
    val tbillStakedKes: Double = 7000.0,
    val monthlyTxCount: Int = 28,
    val onTimeRepaymentPercent: Double = 98.0,
    val chamaTrustRating: Double = 4.8, // 1.0 to 5.0
    val activeLoanKes: Double = 0.0,
    val onChainAccountAgeDays: Int = 180,
    val zeroFeeP2pTxCount: Int = 16
)

data class SimulationPreset(
    val id: String,
    val nameEn: String,
    val nameSw: String,
    val subtitleEn: String,
    val subtitleSw: String,
    val metrics: SimulatedFinancialMetrics
)

data class AiCreditEvaluationResult(
    val overallScore: Int, // 300 to 850
    val previousScore: Int = 710,
    val tierNameEn: String,
    val tierNameSw: String,
    val riskGrade: String, // "AAA", "AA", "A", "BBB", "BB"
    val maxPreApprovedLimitKes: Double,
    val monthlyInterestRate: Double, // % per month
    val factors: List<CreditScoreFactor>,
    val metrics: SimulatedFinancialMetrics,
    val aiUnderwritingRationaleEn: String,
    val aiUnderwritingRationaleSw: String,
    val actionableRecommendationsEn: List<String>,
    val actionableRecommendationsSw: List<String>,
    val fulizaSavingsComparisonKes: Double
)

object AiCreditScoringEngine {

    fun getScenarioPresets(): List<SimulationPreset> {
        return listOf(
            SimulationPreset(
                id = "DEFAULT_PROFILE",
                nameEn = "Your Baseline Profile",
                nameSw = "Wasifu Wako Wa Sasa",
                subtitleEn = "Live on-chain & mobile wallet state",
                subtitleSw = "Hali ya sasa ya pochi yako",
                metrics = SimulatedFinancialMetrics(
                    monthlyInflowKes = 48500.0,
                    tbillStakedKes = 7050.0,
                    monthlyTxCount = 32,
                    onTimeRepaymentPercent = 98.0,
                    chamaTrustRating = 4.8,
                    activeLoanKes = 0.0,
                    onChainAccountAgeDays = 190,
                    zeroFeeP2pTxCount = 20
                )
            ),
            SimulationPreset(
                id = "DUKA_MERCHANT",
                nameEn = "🏪 Duka & Merchant Node",
                nameSw = "🏪 Muuzaji Wa Duka & Wakala",
                subtitleEn = "High till turnovers & daily cash velocity",
                subtitleSw = "Mzunguko mkubwa wa mauzo ya kila siku",
                metrics = SimulatedFinancialMetrics(
                    monthlyInflowKes = 165000.0,
                    tbillStakedKes = 25000.0,
                    monthlyTxCount = 84,
                    onTimeRepaymentPercent = 100.0,
                    chamaTrustRating = 4.9,
                    activeLoanKes = 0.0,
                    onChainAccountAgeDays = 340,
                    zeroFeeP2pTxCount = 48
                )
            ),
            SimulationPreset(
                id = "TBILL_INVESTOR",
                nameEn = "📈 T-Bill & RWA Staker",
                nameSw = "📈 Mwekezaji Wa Hatifungani",
                subtitleEn = "High savings balance & yield compounding",
                subtitleSw = "Akiba kubwa ya Hatifungani za Serikali",
                metrics = SimulatedFinancialMetrics(
                    monthlyInflowKes = 95000.0,
                    tbillStakedKes = 120000.0,
                    monthlyTxCount = 24,
                    onTimeRepaymentPercent = 100.0,
                    chamaTrustRating = 5.0,
                    activeLoanKes = 0.0,
                    onChainAccountAgeDays = 420,
                    zeroFeeP2pTxCount = 18
                )
            ),
            SimulationPreset(
                id = "CHAMA_TREASURER",
                nameEn = "🤝 Chama & Social Leader",
                nameSw = "🤝 Kiongozi Wa Chama Cha Akiba",
                subtitleEn = "Maximum peer reputation & group staking",
                subtitleSw = "Uaminifu wa juu wa kijamii na vikundi",
                metrics = SimulatedFinancialMetrics(
                    monthlyInflowKes = 72000.0,
                    tbillStakedKes = 35000.0,
                    monthlyTxCount = 52,
                    onTimeRepaymentPercent = 99.0,
                    chamaTrustRating = 5.0,
                    activeLoanKes = 0.0,
                    onChainAccountAgeDays = 260,
                    zeroFeeP2pTxCount = 38
                )
            ),
            SimulationPreset(
                id = "HIGH_UTILIZATION_STRESSED",
                nameEn = "⚠️ Irregular Inflow / High Debt",
                nameSw = "⚠️ Mapato Hafifu & Madeni",
                subtitleEn = "Simulated stress scenario for risk audit",
                subtitleSw = "Tathmini ya hatari ya mtiririko duni",
                metrics = SimulatedFinancialMetrics(
                    monthlyInflowKes = 12000.0,
                    tbillStakedKes = 0.0,
                    monthlyTxCount = 6,
                    onTimeRepaymentPercent = 70.0,
                    chamaTrustRating = 3.2,
                    activeLoanKes = 8500.0,
                    onChainAccountAgeDays = 45,
                    zeroFeeP2pTxCount = 3
                )
            )
        )
    }

    fun calculateScore(metrics: SimulatedFinancialMetrics): AiCreditEvaluationResult {
        // 1. Factor: Cashflow Health & Revenue (30% weight)
        val inflowScore = when {
            metrics.monthlyInflowKes >= 150000.0 -> 98
            metrics.monthlyInflowKes >= 80000.0 -> 90
            metrics.monthlyInflowKes >= 40000.0 -> 82
            metrics.monthlyInflowKes >= 20000.0 -> 68
            metrics.monthlyInflowKes >= 10000.0 -> 52
            else -> 35
        }
        val cashflowStatus = getFactorStatus(inflowScore)

        // 2. Factor: Blockchain & Smart Account Footprint (25% weight)
        val txActivityScore = min(100, (metrics.monthlyTxCount * 1.3 + (metrics.onChainAccountAgeDays / 5.0)).toInt())
        val blockchainScore = max(30, min(99, txActivityScore))
        val blockchainStatus = getFactorStatus(blockchainScore)

        // 3. Factor: T-Bill & Yield Vault Staking (20% weight)
        val stakingScore = when {
            metrics.tbillStakedKes >= 80000.0 -> 99
            metrics.tbillStakedKes >= 30000.0 -> 92
            metrics.tbillStakedKes >= 10000.0 -> 84
            metrics.tbillStakedKes >= 3000.0 -> 72
            metrics.tbillStakedKes > 0.0 -> 58
            else -> 30
        }
        val stakingStatus = getFactorStatus(stakingScore)

        // 4. Factor: Repayment Reliability & Debt Health (15% weight)
        val repaymentScore = min(100, max(25, metrics.onTimeRepaymentPercent.toInt()))
        val repaymentStatus = getFactorStatus(repaymentScore)

        // 5. Factor: Chama & Social Network Trust (10% weight)
        val socialScore = min(100, max(30, (metrics.chamaTrustRating * 20.0).toInt()))
        val socialStatus = getFactorStatus(socialScore)

        // Weighted computation: base 300 + 550 * (weighted_factor / 100)
        val weightedFactor = (
            inflowScore * 0.30 +
            blockchainScore * 0.25 +
            stakingScore * 0.20 +
            repaymentScore * 0.15 +
            socialScore * 0.10
        )

        val computedScore = (300 + (5.50 * weightedFactor)).roundToInt().coerceIn(300, 850)

        val (tierNameEn, tierNameSw, riskGrade, maxLimit, rate) = when {
            computedScore >= 800 -> TierInfo("Prime Exceptional", "Bora Zaidi (AAA)", "AAA", 150000.0, 0.9)
            computedScore >= 740 -> TierInfo("Prime / Very Good", "Kiwango Cha Juu (AA)", "AA", 80000.0, 1.1)
            computedScore >= 670 -> TierInfo("Good Standing", "Nzuri Sana (A)", "A", 45000.0, 1.4)
            computedScore >= 580 -> TierInfo("Fair / Moderate", "Wastani (BBB)", "BBB", 20000.0, 1.8)
            else -> TierInfo("Building Credit", "Inajenga Upya (BB)", "BB", 7500.0, 2.4)
        }

        val factorList = listOf(
            CreditScoreFactor(
                id = "CASHFLOW",
                titleEn = "Cashflow Velocity & Volume",
                titleSw = "Mzunguko Wa Fedha & Mapato",
                score = inflowScore,
                weightPercent = 30,
                status = cashflowStatus,
                impactPoints = (inflowScore * 0.30 * 5.5 / 10).roundToInt(),
                descriptionEn = "Monthly inflow of KES %,.0f with regular transaction frequency.".format(metrics.monthlyInflowKes),
                descriptionSw = "Mapato ya kila mwezi ya KES %,.0f na miamala ya kila mara.".format(metrics.monthlyInflowKes),
                tipEn = "Directing additional Till sales or salary inflows will push this factor to 95+.",
                tipSw = "Kuelekeza mauzo zaidi ya Till kutaongeza alama hii hadi 95+."
            ),
            CreditScoreFactor(
                id = "BLOCKCHAIN",
                titleEn = "Base L2 Smart Account Footprint",
                titleSw = "Historia Kwenye Mtandao Wa Blockchain",
                score = blockchainScore,
                weightPercent = 25,
                status = blockchainStatus,
                impactPoints = (blockchainScore * 0.25 * 5.5 / 10).roundToInt(),
                descriptionEn = "${metrics.monthlyTxCount} monthly gasless transactions on Base L2 over ${metrics.onChainAccountAgeDays} days.",
                descriptionSw = "Miamala ${metrics.monthlyTxCount} ya bila ada kwenye Base L2 ndani ya siku ${metrics.onChainAccountAgeDays}.",
                tipEn = "Transacting weekly with zero fees enhances your decentralized smart account identity.",
                tipSw = "Kufanya miamala kila wiki bila ada kunaimarisha utambulisho wako wa kidijitali."
            ),
            CreditScoreFactor(
                id = "STAKING",
                titleEn = "Treasury Bill & RWA Staking",
                titleSw = "Akiba Ya Hatifungani Za Serikali",
                score = stakingScore,
                weightPercent = 20,
                status = stakingStatus,
                impactPoints = (stakingScore * 0.20 * 5.5 / 10).roundToInt(),
                descriptionEn = "KES %,.0f staked in 13.5% APY Government 91-Day T-Bills & MMF vaults.".format(metrics.tbillStakedKes),
                descriptionSw = "KES %,.0f zilizowekezwa kwenye Hatifungani za 13.5% APY na MMF.".format(metrics.tbillStakedKes),
                tipEn = "Locking a portion of float into yield vaults acts as dynamic micro-collateral.",
                tipSw = "Kuweka akiba kwenye Vault ya Hatifungani hutumika kama dhamana ya papo hapo."
            ),
            CreditScoreFactor(
                id = "REPAYMENT",
                titleEn = "Repayment Punctuality & History",
                titleSw = "Uaminifu Wa Kulipa Mikopo",
                score = repaymentScore,
                weightPercent = 15,
                status = repaymentStatus,
                impactPoints = (repaymentScore * 0.15 * 5.5 / 10).roundToInt(),
                descriptionEn = "%.1f%% on-time settlement rate with zero historical defaults.".format(metrics.onTimeRepaymentPercent),
                descriptionSw = "Kiwango cha %.1f%% cha ulipaji kwa wakati bila malimbikizo.".format(metrics.onTimeRepaymentPercent),
                tipEn = "Maintaining 100% early repayment guarantees automatic limit increases.",
                tipSw = "Kulipa mapema huhakikisha ongezeko la moja kwa moja la kiwango chako cha mkopo."
            ),
            CreditScoreFactor(
                id = "CHAMA_SOCIAL",
                titleEn = "Chama & Social Trust Network",
                titleSw = "Uaminifu Wa Chama & Jamii",
                score = socialScore,
                weightPercent = 10,
                status = socialStatus,
                impactPoints = (socialScore * 0.10 * 5.5 / 10).roundToInt(),
                descriptionEn = "%.1f / 5.0 Peer social rating and agent liquidity node endorsement.".format(metrics.chamaTrustRating),
                descriptionSw = "Alama ya %.1f / 5.0 ya uaminifu wa kikundi na mawakala wa Kifaru.".format(metrics.chamaTrustRating),
                tipEn = "Participating in verified Chama saving circles enhances your community trust score.",
                tipSw = "Kushiriki katika vikundi vya Chama vilivyothibitishwa huongeza alama yako ya kijamii."
            )
        )

        // Rationale & Recommendations
        val rationaleEn = "AI Underwriting computed a $riskGrade rating based on strong cashflow turnover (KES %,.0f/mo) and disciplined T-Bill staking (KES %,.0f). Your Base L2 on-chain execution footprint confirms zero-fee transaction consistency."
            .format(metrics.monthlyInflowKes, metrics.tbillStakedKes)

        val rationaleSw = "Injini ya AI imetoa daraja la $riskGrade kutokana na mzunguko thabiti wa mapato (KES %,.0f/mwezi) na uwekezaji wa Hatifungani (KES %,.0f). Shughuli zako kwenye mtandao wa Base L2 zinathibitisha uaminifu mkubwa."
            .format(metrics.monthlyInflowKes, metrics.tbillStakedKes)

        val recsEn = listOf(
            "Stake KES 5,000 more in 91-Day T-Bills (13.5% APY) to unlock a +22 point boost.",
            "Route your weekly utility & merchant Till payments via Kifaru Pay to enhance cashflow velocity.",
            "Maintain zero overdue loans to qualify for an instant limit upgrade to KES %,.0f.".format(maxLimit * 1.3)
        )

        val recsSw = listOf(
            "Wekeza KES 5,000 zaidi kwenye Hatifungani za Serikali (13.5% APY) ili kuongeza alama +22.",
            "Lipa bili za kila wiki kupitia Till ya Kifaru ili kuongeza kasi ya mzunguko wa fedha.",
            "Lipa mikopo kwa wakati ili kufungua kiwango kikubwa zaidi cha hadi KES %,.0f.".format(maxLimit * 1.3)
        )

        // Fuliza comparison: KES 10,000 borrowed for 30 days
        // Kifaru: 10,000 * (rate / 100) = ~110 KES
        // Fuliza: 10,000 * 1% daily = ~3,000 KES (saving ~2,890 KES)
        val benchmarkLoan = 10000.0
        val kifaruFee = benchmarkLoan * (rate / 100.0)
        val fulizaFee = benchmarkLoan * 0.01 * 30.0
        val savings = fulizaFee - kifaruFee

        return AiCreditEvaluationResult(
            overallScore = computedScore,
            previousScore = max(300, computedScore - 18),
            tierNameEn = tierNameEn,
            tierNameSw = tierNameSw,
            riskGrade = riskGrade,
            maxPreApprovedLimitKes = maxLimit,
            monthlyInterestRate = rate,
            factors = factorList,
            metrics = metrics,
            aiUnderwritingRationaleEn = rationaleEn,
            aiUnderwritingRationaleSw = rationaleSw,
            actionableRecommendationsEn = recsEn,
            actionableRecommendationsSw = recsSw,
            fulizaSavingsComparisonKes = savings
        )
    }

    private fun getFactorStatus(score: Int): FactorStatus {
        return when {
            score >= 85 -> FactorStatus.EXCELLENT
            score >= 70 -> FactorStatus.GOOD
            score >= 50 -> FactorStatus.FAIR
            else -> FactorStatus.ATTENTION_NEEDED
        }
    }

    private data class TierInfo(
        val tierEn: String,
        val tierSw: String,
        val riskGrade: String,
        val maxLimit: Double,
        val rate: Double
    )
}
