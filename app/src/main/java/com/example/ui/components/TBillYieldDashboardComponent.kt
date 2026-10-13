package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.YieldVaultEntity
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import java.util.Locale

enum class ChartTimeframe {
    MONTH_1, MONTH_3, MONTH_6, YEAR_1
}

enum class ChartMetricMode {
    APY_TREND, BALANCE_GROWTH
}

data class YieldDataPoint(
    val dateLabel: String,
    val apyRate: Float, // e.g. 13.5f
    val balance: Float, // e.g. 50000.0f
    val cumulativeYield: Float // e.g. 1250.0f
)

@Composable
fun TBillYieldDashboardComponent(
    vaults: List<YieldVaultEntity>,
    language: String = "EN",
    modifier: Modifier = Modifier,
    onStakeClick: ((vaultCode: String, amount: Double) -> Unit)? = null
) {
    var selectedTimeframe by remember { mutableStateOf(ChartTimeframe.MONTH_6) }
    var selectedMetricMode by remember { mutableStateOf(ChartMetricMode.BALANCE_GROWTH) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    // Interactive Projection State
    var simulationPrincipalText by remember { mutableStateOf("50000") }
    var simulationTenureDays by remember { mutableFloatStateOf(182f) }

    // Calculate aggregated metrics from vaults
    val totalStaked = remember(vaults) { vaults.sumOf { it.invested } }
    val totalYieldEarned = remember(vaults) { vaults.sumOf { it.earned } }
    val averageApy = remember(vaults) {
        if (vaults.isNotEmpty()) vaults.map { it.apy }.average() else 13.5
    }

    // Historical chart data simulation points based on selected timeframe
    val chartDataPoints = remember(selectedTimeframe) {
        generateChartDataPoints(selectedTimeframe)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tbill_yield_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section & Main APY Benchmark Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tbill_apy_header_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoGraph,
                                contentDescription = "T-Bill Yield",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == "SW") "Hatifungani Za Serikali (T-Bills)" else "Kenya T-Bills & MMF Yields",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == "SW") "Inasimamiwa na Benki Kuu ya Kenya (CBK)" else "Backed by Central Bank of Kenya Sovereign Debt",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = GoldAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified APY",
                                tint = GoldAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "13.50% APY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Metrics Summary Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Staked Balance Metric Box
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_staked_balance"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (language == "SW") "Salio Lililowekwa" else "Active Staked Balance",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "KES %,.2f".format(totalStaked),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (totalStaked > 0) "Generating daily compound" else "0 Staked - Start earning",
                                fontSize = 10.sp,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    // Total Earned Yield Box
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_yield_earned"),
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldDark.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (language == "SW") "Faida Iliyokusanywa" else "Cumulative Yield Earned",
                                fontSize = 10.sp,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+ KES %,.2f".format(totalYieldEarned),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Est. daily: KES %,.2f".format((totalStaked * (averageApy / 100.0)) / 365.0),
                                fontSize = 10.sp,
                                color = GoldAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rate Benchmark Comparison Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Yield Alpha",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kifaru Vault (13.5%) vs Bank Savings (3.2%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = EmeraldPrimary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+10.3% Alpha",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // --- Data Visualization Chart Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tbill_chart_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Chart Metric Mode Switcher & Timeframe Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ChartMetricChip(
                            label = if (language == "SW") "Salio na Faida" else "Growth Curve",
                            selected = selectedMetricMode == ChartMetricMode.BALANCE_GROWTH,
                            tag = "chart_mode_growth",
                            onClick = {
                                selectedMetricMode = ChartMetricMode.BALANCE_GROWTH
                                selectedPointIndex = null
                            }
                        )
                        ChartMetricChip(
                            label = if (language == "SW") "Kasi ya APY" else "APY Rate Trend",
                            selected = selectedMetricMode == ChartMetricMode.APY_TREND,
                            tag = "chart_mode_apy",
                            onClick = {
                                selectedMetricMode = ChartMetricMode.APY_TREND
                                selectedPointIndex = null
                            }
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Chart",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeframe Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChartTimeframe.values().forEach { tf ->
                        val label = when (tf) {
                            ChartTimeframe.MONTH_1 -> "1M"
                            ChartTimeframe.MONTH_3 -> "3M"
                            ChartTimeframe.MONTH_6 -> "6M"
                            ChartTimeframe.YEAR_1 -> "1Y"
                        }
                        FilterChip(
                            selected = selectedTimeframe == tf,
                            onClick = {
                                selectedTimeframe = tf
                                selectedPointIndex = null
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("timeframe_chip_$label"),
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Point Callout Tooltip Header when inspected
                val selectedPoint = selectedPointIndex?.let { idx ->
                    if (idx in chartDataPoints.indices) chartDataPoints[idx] else null
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("chart_tooltip_bar"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedPoint != null) EmeraldDark.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedPoint != null) EmeraldPrimary else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedPoint != null) {
                            Text(
                                text = "📅 ${selectedPoint.dateLabel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )

                            if (selectedMetricMode == ChartMetricMode.BALANCE_GROWTH) {
                                Text(
                                    text = "Balance: KES %,.0f (+KES %,.0f Yield)".format(selectedPoint.balance, selectedPoint.cumulativeYield),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                )
                            } else {
                                Text(
                                    text = "T-Bill Benchmark: %.2f%% APY".format(selectedPoint.apyRate),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Hint",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == "SW") "Gusa au buruta kwenye chati kuona faida kwa tarehe" else "Tap or drag across chart line to inspect historical yield points",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Compose Canvas Chart Component
                YieldChartCanvas(
                    dataPoints = chartDataPoints,
                    metricMode = selectedMetricMode,
                    selectedIndex = selectedPointIndex,
                    onPointSelected = { idx -> selectedPointIndex = idx },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("yield_canvas_chart")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Axis Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    chartDataPoints.filterIndexed { index, _ -> index % (chartDataPoints.size / 4) == 0 || index == chartDataPoints.lastIndex }.forEach { pt ->
                        Text(
                            text = pt.dateLabel,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- Interactive Yield Projection Simulator ---
        YieldCalculatorCard(
            language = language,
            simulationPrincipalText = simulationPrincipalText,
            onPrincipalChange = { simulationPrincipalText = it },
            simulationTenureDays = simulationTenureDays,
            onTenureChange = { simulationTenureDays = it },
            averageApy = averageApy,
            onStakeClick = onStakeClick
        )

        // --- T-Bill Auction Vault Types List ---
        TBillAuctionVaultsList(
            vaults = vaults,
            language = language,
            onStakeClick = onStakeClick
        )
    }
}

@Composable
fun ChartMetricChip(
    label: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) EmeraldPrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun YieldChartCanvas(
    dataPoints: List<YieldDataPoint>,
    metricMode: ChartMetricMode,
    selectedIndex: Int?,
    onPointSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = EmeraldPrimary
    val accentColor = GoldAccent
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    if (dataPoints.isEmpty()) return

    val values = remember(dataPoints, metricMode) {
        dataPoints.map {
            if (metricMode == ChartMetricMode.BALANCE_GROWTH) it.balance else it.apyRate
        }
    }

    val minY = remember(values) { (values.minOrNull() ?: 0f) * 0.95f }
    val maxY = remember(values) { (values.maxOrNull() ?: 100f) * 1.05f }
    val rangeY = if (maxY - minY == 0f) 1f else maxY - minY

    Canvas(
        modifier = modifier
            .pointerInput(dataPoints, metricMode) {
                detectTapGestures { offset ->
                    val width = size.width
                    val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                    val clickedIdx = (offset.x / stepX).toInt().coerceIn(0, dataPoints.lastIndex)
                    onPointSelected(clickedIdx)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
        }

        // Map values to (x, y) points
        val points = values.mapIndexed { index, value ->
            val x = index * stepX
            val normalizedY = (value - minY) / rangeY
            val y = height - (normalizedY * height)
            Offset(x, y)
        }

        // Build Smooth Path for Line & Area Brush Fill
        val strokePath = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            strokePath.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val controlX1 = p1.x + (p2.x - p1.x) / 2f
                val controlY1 = p1.y
                val controlX2 = p1.x + (p2.x - p1.x) / 2f
                val controlY2 = p2.y

                strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw Area Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        primaryColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Smooth Line Path
            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw glowing data point dots
            points.forEachIndexed { idx, pt ->
                val isSelected = selectedIndex == idx
                val dotRadius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx()

                if (isSelected) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.35f),
                        radius = dotRadius * 2f,
                        center = pt
                    )
                }

                drawCircle(
                    color = if (isSelected) accentColor else primaryColor,
                    radius = dotRadius,
                    center = pt
                )
                drawCircle(
                    color = Color.Black,
                    radius = dotRadius * 0.4f,
                    center = pt
                )
            }

            // Draw vertical crosshair guide line if a point is inspected
            if (selectedIndex != null && selectedIndex in points.indices) {
                val inspectedPt = points[selectedIndex]
                drawLine(
                    color = accentColor.copy(alpha = 0.8f),
                    start = Offset(inspectedPt.x, 0f),
                    end = Offset(inspectedPt.x, height),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }
        }
    }
}

@Composable
fun YieldCalculatorCard(
    language: String,
    simulationPrincipalText: String,
    onPrincipalChange: (String) -> Unit,
    simulationTenureDays: Float,
    onTenureChange: (Float) -> Unit,
    averageApy: Double,
    onStakeClick: ((String, Double) -> Unit)?
) {
    val principal = simulationPrincipalText.toDoubleOrNull() ?: 0.0
    val tenureDaysInt = simulationTenureDays.toInt()

    // Daily compounding yield formula: A = P * (1 + r/365)^(days) - P
    val estimatedYield = remember(principal, tenureDaysInt, averageApy) {
        val dailyRate = (averageApy / 100.0) / 365.0
        val totalMaturityAmount = principal * Math.pow(1.0 + dailyRate, tenureDaysInt.toDouble())
        (totalMaturityAmount - principal).coerceAtLeast(0.0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("yield_calculator_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Calculator",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "SW") "Kikokotoo Cha Faida Cha T-Bill" else "Interactive T-Bill Yield Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "%.2f%% APY".format(averageApy),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            // Input Row for Principal
            OutlinedTextField(
                value = simulationPrincipalText,
                onValueChange = onPrincipalChange,
                label = { Text(if (language == "SW") "Kiasi Cha Wekeza (KES)" else "Investment Principal (KES)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculator_principal_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Quick Preset Amount Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("10000", "50000", "100000", "250000").forEach { amountPreset ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPrincipalChange(amountPreset) }
                            .testTag("preset_btn_$amountPreset"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (simulationPrincipalText == amountPreset) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "KES ${amountPreset.toInt() / 1000}k",
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (simulationPrincipalText == amountPreset) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Tenure Duration Slider Section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == "SW") "Muda wa Uwekezaji:" else "Tenure Duration:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$tenureDaysInt Days (${getTenureName(tenureDaysInt)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                Slider(
                    value = simulationTenureDays,
                    onValueChange = onTenureChange,
                    valueRange = 30f..364f,
                    steps = 10,
                    modifier = Modifier.testTag("tenure_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimary,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Projected Maturity Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = EmeraldDark.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == "SW") "Makadirio ya Faida" else "Projected Yield Return",
                            fontSize = 11.sp,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "+ KES %,.2f".format(estimatedYield),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total at Maturity",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "KES %,.2f".format(principal + estimatedYield),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (principal > 0) {
                        onStakeClick?.invoke("TBILL_182", principal)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("calculator_stake_now_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Stake",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == "SW") "Weka Akiba Pata KES %,.0f".format(estimatedYield) else "Stake KES %,.0f Now & Earn Yield".format(principal),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TBillAuctionVaultsList(
    vaults: List<YieldVaultEntity>,
    language: String,
    onStakeClick: ((String, Double) -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (language == "SW") "Njia Za T-Bill na MMF Vaults" else "CBK Treasury Bill Vault Tiers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        vaults.forEach { vault ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auction_vault_${vault.code}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
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
                                .size(40.dp)
                                .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = vault.name,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = vault.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${vault.term} • Daily Compounding",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Surface(
                            color = GoldAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "+${vault.apy}% APY",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Staked: KES %,.0f".format(vault.invested),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Helper Functions
fun generateChartDataPoints(timeframe: ChartTimeframe): List<YieldDataPoint> {
    val count = when (timeframe) {
        ChartTimeframe.MONTH_1 -> 8
        ChartTimeframe.MONTH_3 -> 12
        ChartTimeframe.MONTH_6 -> 18
        ChartTimeframe.YEAR_1 -> 24
    }

    val baseBalance = 25000f
    val baseApy = 12.8f

    return (0 until count).map { i ->
        val fraction = i.toFloat() / (count - 1)
        val dateStr = when (timeframe) {
            ChartTimeframe.MONTH_1 -> "Day ${i * 4 + 1}"
            ChartTimeframe.MONTH_3 -> "Wk ${i + 1}"
            ChartTimeframe.MONTH_6 -> "Mth ${i / 3 + 1}.${(i % 3) * 10}"
            ChartTimeframe.YEAR_1 -> "Mth ${i / 2 + 1}"
        }

        val apy = baseApy + (fraction * 0.9f) + (Math.sin(i.toDouble() * 0.8).toFloat() * 0.15f)
        val cumulativeYield = (i * 320f) + (fraction * 1200f)
        val balance = baseBalance + cumulativeYield

        YieldDataPoint(
            dateLabel = dateStr,
            apyRate = apy,
            balance = balance,
            cumulativeYield = cumulativeYield
        )
    }
}

fun getTenureName(days: Int): String {
    return when {
        days <= 45 -> "30-Day Liquid MMF"
        days in 46..120 -> "91-Day CBK T-Bill Auction"
        days in 121..240 -> "182-Day CBK T-Bill Benchmark"
        else -> "364-Day Sovereign Debt Vault"
    }
}
