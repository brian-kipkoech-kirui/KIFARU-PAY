package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiCreditEvaluationResult
import com.example.data.AiCreditScoringEngine
import com.example.data.CreditProfileEntity
import com.example.data.CreditScoreFactor
import com.example.data.FactorStatus
import com.example.data.SimulatedFinancialMetrics
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AiCreditScoreDashboardComponent(
    creditProfile: CreditProfileEntity?,
    evaluationResult: AiCreditEvaluationResult,
    simulatedMetrics: SimulatedFinancialMetrics,
    selectedPresetId: String,
    isCalculating: Boolean,
    aiAdvisoryText: String?,
    isAdvisoryLoading: Boolean,
    language: String,
    onSelectPreset: (String) -> Unit,
    onUpdateMetrics: (SimulatedFinancialMetrics) -> Unit,
    onRecalculate: () -> Unit,
    onApplyToProfile: () -> Unit,
    onRequestLoan: (Double) -> Unit,
    onRepayLoan: (Double) -> Unit,
    onRequestAiCoaching: (Int, String) -> Unit
) {
    var showCustomSliders by remember { mutableStateOf(false) }
    var loanAmountSlider by remember { mutableStateOf(10000f) }
    var repaymentAmountText by remember { mutableStateOf("") }

    val activeLoan = creditProfile?.activeLoan ?: 0.0
    val maxLimit = evaluationResult.maxPreApprovedLimitKes
    val isSwahili = language == "SW"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        CreditScoreHeaderCard(
            language = language,
            isCalculating = isCalculating,
            onRecalculate = onRecalculate
        )

        // Semicircular Canvas Gauge Card
        CreditGaugeScoreCard(
            evaluation = evaluationResult,
            language = language
        )

        // Scenario Simulation & Preset Selector
        CreditSimulationSandboxCard(
            selectedPresetId = selectedPresetId,
            simulatedMetrics = simulatedMetrics,
            showCustomSliders = showCustomSliders,
            isCalculating = isCalculating,
            language = language,
            onSelectPreset = onSelectPreset,
            onToggleCustomSliders = { showCustomSliders = !showCustomSliders },
            onUpdateMetrics = onUpdateMetrics,
            onApplyToProfile = onApplyToProfile
        )

        // 5-Pillar Weighted Factor Breakdown
        CreditFactorsBreakdownCard(
            factors = evaluationResult.factors,
            language = language
        )

        // Live AI Underwriting & Gemini Advisory Section
        AiUnderwritingAdvisoryCard(
            evaluation = evaluationResult,
            aiAdvisoryText = aiAdvisoryText,
            isAdvisoryLoading = isAdvisoryLoading,
            language = language,
            onRequestAiCoaching = {
                onRequestAiCoaching(evaluationResult.overallScore, evaluationResult.riskGrade)
            }
        )

        // Micro-Boost Loan Application & Active Loan Repayment Hub
        MicroBoostLoanHubCard(
            creditProfile = creditProfile,
            evaluation = evaluationResult,
            loanAmountSlider = loanAmountSlider,
            repaymentAmountText = repaymentAmountText,
            language = language,
            onSliderChange = { loanAmountSlider = it },
            onRepaymentTextChange = { repaymentAmountText = it },
            onRequestLoan = onRequestLoan,
            onRepayLoan = onRepayLoan
        )

        // Comparison Table: Kifaru Boost vs Predatory Daily Loans (Fuliza / Tala)
        FulizaComparisonCard(
            evaluation = evaluationResult,
            language = language
        )
    }
}

@Composable
private fun CreditScoreHeaderCard(
    language: String,
    isCalculating: Boolean,
    onRecalculate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("credit_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = EmeraldDark.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GoldAccent, EmeraldPrimary)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Credit",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (language == "SW") "Injini Ya Mikopo Ya AI" else "AI Underwriting & Credit Score",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if (language == "SW") "Inapima mzunguko wa pesa & blockchain" else "Real-time cashflow & Base L2 provenance",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onRecalculate() }
                    .testTag("recalculate_score_btn"),
                color = EmeraldPrimary.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, EmeraldPrimary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCalculating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = EmeraldPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Recalculate",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == "SW") "Pima Upya" else "Audit AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditGaugeScoreCard(
    evaluation: AiCreditEvaluationResult,
    language: String
) {
    val animatedScore by animateIntAsState(
        targetValue = evaluation.overallScore,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "score_anim"
    )

    val scoreDelta = evaluation.overallScore - evaluation.previousScore

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("credit_score_gauge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (language == "SW") "ALAMA YAKO YA MKOPO YA KIFARU" else "KIFARU DECENTRALIZED CREDIT SCORE",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = GoldAccent
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Gauge Canvas
            Box(
                modifier = Modifier
                    .size(240.dp, 150.dp)
                    .testTag("credit_gauge_canvas_box"),
                contentAlignment = Alignment.Center
            ) {
                CreditScoreArcGauge(score = animatedScore)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    Text(
                        text = "$animatedScore",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            animatedScore >= 740 -> EmeraldPrimary
                            animatedScore >= 670 -> Color(0xFF66BB6A)
                            animatedScore >= 580 -> GoldAccent
                            else -> Color(0xFFFF7043)
                        }
                    )

                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${if (language == "SW") evaluation.tierNameSw else evaluation.tierNameEn} • ${evaluation.riskGrade}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scale Legend & Point Delta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "300 (Subprime)",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = if (scoreDelta >= 0) EmeraldPrimary.copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (scoreDelta >= 0) "▲ +$scoreDelta pts vs baseline" else "▼ $scoreDelta pts vs baseline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (scoreDelta >= 0) EmeraldPrimary else Color(0xFFFF5252),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "850 (Exceptional)",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // Key Metrics Pill Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (language == "SW") "Kiwango cha Mkopo" else "Pre-Approved Limit",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KES %,.0f".format(evaluation.maxPreApprovedLimitKes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (language == "SW") "Riba kwa Mwezi" else "Monthly Interest",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${evaluation.monthlyInterestRate}% / mo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GoldAccent
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (language == "SW") "Gharama ya Dhamana" else "Zero Paperwork",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Instant ⚡",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldLight
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditScoreArcGauge(score: Int) {
    val progress = ((score - 300f) / 550f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )

    Canvas(modifier = Modifier.size(230.dp, 140.dp)) {
        val strokeWidth = 18.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)
        val arcTopLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        // Background Track Arc
        drawArc(
            color = Color.DarkGray.copy(alpha = 0.35f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Gradient Active Arc
        val brush = Brush.horizontalGradient(
            0.0f to Color(0xFFFF5252),
            0.35f to Color(0xFFFFB300),
            0.65f to Color(0xFF00E676),
            1.0f to Color(0xFF00BFA5)
        )

        drawArc(
            brush = brush,
            startAngle = 180f,
            sweepAngle = 180f * animatedProgress,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Needle Indicator Bead
        val angleRad = (180f + (180f * animatedProgress)) * (PI / 180f)
        val radiusX = arcSize.width / 2
        val radiusY = arcSize.height / 2
        val centerX = arcTopLeft.x + radiusX
        val centerY = arcTopLeft.y + radiusY

        val beadX = (centerX + (radiusX * cos(angleRad))).toFloat()
        val beadY = (centerY + (radiusY * sin(angleRad))).toFloat()

        drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = Offset(beadX, beadY)
        )
        drawCircle(
            color = Color(0xFF00E676),
            radius = 6.dp.toPx(),
            center = Offset(beadX, beadY)
        )
    }
}

@Composable
private fun CreditSimulationSandboxCard(
    selectedPresetId: String,
    simulatedMetrics: SimulatedFinancialMetrics,
    showCustomSliders: Boolean,
    isCalculating: Boolean,
    language: String,
    onSelectPreset: (String) -> Unit,
    onToggleCustomSliders: () -> Unit,
    onUpdateMetrics: (SimulatedFinancialMetrics) -> Unit,
    onApplyToProfile: () -> Unit
) {
    val presets = AiCreditScoringEngine.getScenarioPresets()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("simulation_sandbox_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Simulation Sandbox",
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "SW") "Jaribio la Miamala & AI Simulator" else "Interactive Financial Pattern Simulator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = GoldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "What-If Engine",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = if (language == "SW")
                    "Chagua mwelekeo wa kifedha hapa chini uone jinsi akiba ya T-Bills, mauzo ya Till, na miamala ya Base L2 inavyoongeza alama yako:"
                else
                    "Test how different cashflow velocities, T-Bill staking amounts, and blockchain activity influence your credit rating and pre-approved limit in real time:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Preset Selection Pills (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = selectedPresetId == preset.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectPreset(preset.id) }
                            .testTag("preset_btn_${preset.id}"),
                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = if (language == "SW") preset.nameSw else preset.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == "SW") preset.subtitleSw else preset.subtitleEn,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Custom Sliders Expand/Collapse Button
            OutlinedButton(
                onClick = onToggleCustomSliders,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_custom_sliders_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = GoldAccent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showCustomSliders)
                        (if (language == "SW") "Funga Vigezo Vya Kina" else "Hide Custom Simulation Sliders")
                    else
                        (if (language == "SW") "Fungua Vigezo Vya Kina (Sliders)" else "Fine-Tune Custom Simulation Sliders"),
                    fontSize = 12.sp,
                    color = GoldAccent
                )
            }

            // Animated Custom Slider Panel
            AnimatedVisibility(
                visible = showCustomSliders,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Slider 1: Monthly Inflow
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == "SW") "Mapato ya Kila Mwezi:" else "Monthly Inflows / Till Turnover:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "KES %,.0f".format(simulatedMetrics.monthlyInflowKes),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                        Slider(
                            value = simulatedMetrics.monthlyInflowKes.toFloat(),
                            onValueChange = {
                                onUpdateMetrics(simulatedMetrics.copy(monthlyInflowKes = it.toDouble()))
                            },
                            valueRange = 5000f..300000f,
                            steps = 59,
                            colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary),
                            modifier = Modifier.testTag("slider_monthly_inflow")
                        )
                    }

                    // Slider 2: T-Bill / Vault Staked
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == "SW") "Akiba ya Hatifungani (T-Bills):" else "T-Bill & RWA Staked Vaults:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "KES %,.0f".format(simulatedMetrics.tbillStakedKes),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                        Slider(
                            value = simulatedMetrics.tbillStakedKes.toFloat(),
                            onValueChange = {
                                onUpdateMetrics(simulatedMetrics.copy(tbillStakedKes = it.toDouble()))
                            },
                            valueRange = 0f..150000f,
                            steps = 30,
                            colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent),
                            modifier = Modifier.testTag("slider_tbill_staked")
                        )
                    }

                    // Slider 3: Monthly Tx Count
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == "SW") "Miamala ya Base L2 kwa Mwezi:" else "Monthly Base L2 Transactions:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${simulatedMetrics.monthlyTxCount} txs/mo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                        }
                        Slider(
                            value = simulatedMetrics.monthlyTxCount.toFloat(),
                            onValueChange = {
                                onUpdateMetrics(simulatedMetrics.copy(monthlyTxCount = it.toInt()))
                            },
                            valueRange = 1f..100f,
                            steps = 99,
                            colors = SliderDefaults.colors(thumbColor = EmeraldLight, activeTrackColor = EmeraldLight),
                            modifier = Modifier.testTag("slider_tx_count")
                        )
                    }

                    // Slider 4: On-time Repayments
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == "SW") "Kiwango cha Ulipaji kwa Wakati:" else "On-Time Repayment Reliability:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${simulatedMetrics.onTimeRepaymentPercent.toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (simulatedMetrics.onTimeRepaymentPercent >= 90) EmeraldPrimary else Color(0xFFFF7043)
                            )
                        }
                        Slider(
                            value = simulatedMetrics.onTimeRepaymentPercent.toFloat(),
                            onValueChange = {
                                onUpdateMetrics(simulatedMetrics.copy(onTimeRepaymentPercent = it.toDouble()))
                            },
                            valueRange = 50f..100f,
                            steps = 50,
                            colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary),
                            modifier = Modifier.testTag("slider_repayment_rate")
                        )
                    }
                }
            }

            // Save / Apply simulated score to on-chain profile
            Button(
                onClick = onApplyToProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("save_simulated_profile_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == "SW") "Hifadhi Tathmini Hii Kwenye Wasifu" else "Save AI Underwriting to Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CreditFactorsBreakdownCard(
    factors: List<CreditScoreFactor>,
    language: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("credit_factors_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Factor Breakdown",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "SW") "Vipengele 5 Vya Alama Yako" else "5 Core Underwriting Pillars",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "Weighted Model",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            factors.forEach { factor ->
                FactorProgressRow(factor = factor, language = language)
            }
        }
    }
}

@Composable
private fun FactorProgressRow(
    factor: CreditScoreFactor,
    language: String
) {
    val statusColor = when (factor.status) {
        FactorStatus.EXCELLENT -> EmeraldPrimary
        FactorStatus.GOOD -> Color(0xFF66BB6A)
        FactorStatus.FAIR -> GoldAccent
        FactorStatus.ATTENTION_NEEDED -> Color(0xFFFF7043)
    }

    val icon = when (factor.id) {
        "CASHFLOW" -> Icons.Default.MonetizationOn
        "BLOCKCHAIN" -> Icons.Default.Bolt
        "STAKING" -> Icons.Default.Savings
        "REPAYMENT" -> Icons.Default.Security
        else -> Icons.Default.Groups
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = factor.id,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == "SW") factor.titleSw else factor.titleEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${factor.weightPercent}% weight",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${factor.score} / 100",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = statusColor
                )
            }
        }

        LinearProgressIndicator(
            progress = factor.score / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Text(
            text = if (language == "SW") factor.descriptionSw else factor.descriptionEn,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Tip",
                tint = GoldAccent,
                modifier = Modifier
                    .size(13.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (language == "SW") factor.tipSw else factor.tipEn,
                fontSize = 10.5.sp,
                color = GoldAccent
            )
        }
    }
}

@Composable
private fun AiUnderwritingAdvisoryCard(
    evaluation: AiCreditEvaluationResult,
    aiAdvisoryText: String?,
    isAdvisoryLoading: Boolean,
    language: String,
    onRequestAiCoaching: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_underwriting_advisory_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Coaching",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "SW") "Ushauri Wa Kifaru AI" else "AI Underwriting & Coaching",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Gemini Powered",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Algorithmic Rationale Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (language == "SW") "Tathmini ya Kiotomatiki:" else "Underwriting Summary:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == "SW") evaluation.aiUnderwritingRationaleSw else evaluation.aiUnderwritingRationaleEn,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Dynamic Coaching Response if present
            if (aiAdvisoryText != null) {
                Surface(
                    color = EmeraldDark.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (language == "SW") "Mpango Wa Kuongeza Alama Hadi 800+:" else "Action Plan to Reach 800+ Tier:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = aiAdvisoryText,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Coaching Trigger Button
            Button(
                onClick = onRequestAiCoaching,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("request_ai_credit_coaching_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isAdvisoryLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = EmeraldPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "SW") "AI Inachambua..." else "Analyzing Financial Vectors...",
                        fontSize = 12.sp
                    )
                } else {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == "SW") "Pata Mwongozo Wa AI Kufikia 800+" else "Generate Personalized 800+ Score Strategy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MicroBoostLoanHubCard(
    creditProfile: CreditProfileEntity?,
    evaluation: AiCreditEvaluationResult,
    loanAmountSlider: Float,
    repaymentAmountText: String,
    language: String,
    onSliderChange: (Float) -> Unit,
    onRepaymentTextChange: (String) -> Unit,
    onRequestLoan: (Double) -> Unit,
    onRepayLoan: (Double) -> Unit
) {
    val activeLoan = creditProfile?.activeLoan ?: 0.0
    val maxLimit = evaluation.maxPreApprovedLimitKes
    val interestRate = evaluation.monthlyInterestRate

    val calculatedInterestKes = loanAmountSlider * (interestRate / 100f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("micro_boost_loan_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, EmeraldPrimary)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Kifaru Boost",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "SW") "Mkopo Wa Kifaru Boost" else "Instant Kifaru Boost Loan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${interestRate}% / mo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Active Loan Settlement Banner if loan is outstanding
            if (activeLoan > 0) {
                Surface(
                    color = Color(0xFFFFB300).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GoldAccent)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == "SW") "Mkopo Uliopo:" else "Outstanding Loan Balance:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "KES %,.2f".format(activeLoan),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                        }

                        OutlinedTextField(
                            value = repaymentAmountText,
                            onValueChange = onRepaymentTextChange,
                            label = { Text(if (language == "SW") "Kiasi cha Kulipa (KES)" else "Repayment Amount (KES)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("repay_amount_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                val amt = repaymentAmountText.toDoubleOrNull() ?: activeLoan
                                if (amt > 0) {
                                    onRepayLoan(amt)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("submit_repay_loan_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (language == "SW") "Lipa Mkopo & Ongeza Alama (+5 pts)" else "Settle Loan & Boost Score (+5 pts)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Loan Request Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == "SW") "Chagua Kiasi Unachotaka:" else "Request Loan Amount:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KES %,.0f".format(loanAmountSlider),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = EmeraldPrimary
                    )
                }

                Slider(
                    value = loanAmountSlider,
                    onValueChange = onSliderChange,
                    valueRange = 1000f..maxLimit.toFloat().coerceAtLeast(5000f),
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary),
                    modifier = Modifier.testTag("loan_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "KES 1,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Max: KES %,.0f".format(maxLimit), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Interest & Cost Preview Pill
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == "SW") "Riba ya Mwezi (30 Siku):" else "Monthly Interest (30 Days):",
                        fontSize = 11.sp
                    )
                    Text(
                        text = "KES %,.2f (${interestRate}%)".format(calculatedInterestKes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = GoldAccent
                    )
                }
            }

            // Instant Disbursement Action
            Button(
                onClick = {
                    if (loanAmountSlider > 0 && loanAmountSlider <= maxLimit) {
                        onRequestLoan(loanAmountSlider.toDouble())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("instant_loan_disburse_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == "SW")
                        "Pokea KES %,.0f Papo Hapo Kwenye Pochi".format(loanAmountSlider)
                    else
                        "Disburse KES %,.0f Instantly to KES Wallet".format(loanAmountSlider),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}

@Composable
private fun FulizaComparisonCard(
    evaluation: AiCreditEvaluationResult,
    language: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fuliza_comparison_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = "Cost Comparison",
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "SW") "Kifaru Boost vs Fuliza / Tala" else "Kifaru Boost vs Predatory Daily Loans",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Text(
                text = if (language == "SW")
                    "Kwa mkopo wa KES 10,000 ndani ya mwezi mmoja, unaokoa KES %,.0f ukilinganisha na riba ya 1% ya kila siku ya Fuliza!"
                        .format(evaluation.fulizaSavingsComparisonKes)
                else
                    "On a KES 10,000 micro-loan over 30 days, Kifaru Boost saves you KES %,.0f compared to Fuliza's 1% daily compounding fees!"
                        .format(evaluation.fulizaSavingsComparisonKes),
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Kifaru Boost", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                        Text(text = "• ${evaluation.monthlyInterestRate}% / Month\n• 0 Default CRB Blacklist\n• Tokenized T-Bill backing", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFF5252).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Fuliza / Predatory", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFF7043))
                        Text(text = "• 1% / Day Compounding\n• Immediate CRB Penalty\n• High rollover debt traps", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
