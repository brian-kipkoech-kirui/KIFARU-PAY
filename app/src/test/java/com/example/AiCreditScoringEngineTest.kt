package com.example

import com.example.data.AiCreditScoringEngine
import com.example.data.SimulatedFinancialMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCreditScoringEngineTest {

    @Test
    fun testDefaultBaselineScore() {
        val metrics = SimulatedFinancialMetrics()
        val result = AiCreditScoringEngine.calculateScore(metrics)

        assertTrue("Score should be between 300 and 850", result.overallScore in 300..850)
        assertEquals(5, result.factors.size)
        assertTrue("Max pre-approved limit should be positive", result.maxPreApprovedLimitKes > 0)
        assertTrue("Monthly rate should be affordable vs Fuliza", result.monthlyInterestRate <= 2.5)
    }

    @Test
    fun testHighStakingYieldMerchantScoreBoost() {
        val highStakerMetrics = SimulatedFinancialMetrics(
            monthlyInflowKes = 180000.0,
            tbillStakedKes = 65000.0,
            monthlyTxCount = 55,
            onTimeRepaymentPercent = 100.0,
            chamaTrustScore = 4.9
        )
        val result = AiCreditScoringEngine.calculateScore(highStakerMetrics)

        assertTrue("High volume staker should achieve prime score >= 750", result.overallScore >= 750)
        assertTrue("Pre-approved limit should scale >= 80,000 KES", result.maxPreApprovedLimitKes >= 80000.0)
    }

    @Test
    fun testSubprimeHighDebtEvaluation() {
        val distressedMetrics = SimulatedFinancialMetrics(
            monthlyInflowKes = 8000.0,
            tbillStakedKes = 0.0,
            monthlyTxCount = 2,
            onTimeRepaymentPercent = 55.0,
            chamaTrustScore = 2.0
        )
        val result = AiCreditScoringEngine.calculateScore(distressedMetrics)

        assertTrue("Distressed score should be below 600", result.overallScore < 600)
        assertTrue("Pre-approved limit should be constrained", result.maxPreApprovedLimitKes <= 15000.0)
    }

    @Test
    fun testScenarioPresetsAvailability() {
        val presets = AiCreditScoringEngine.getScenarioPresets()
        assertEquals(5, presets.size)
        assertTrue(presets.any { it.id == "DUKA_MERCHANT" })
        assertTrue(presets.any { it.id == "TBILL_STAKER" })
    }
}
