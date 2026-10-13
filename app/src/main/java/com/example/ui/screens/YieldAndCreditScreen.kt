package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AiCreditEvaluationResult
import com.example.data.CreditProfileEntity
import com.example.data.SimulatedFinancialMetrics
import com.example.data.YieldVaultEntity
import com.example.ui.components.AiCreditScoreDashboardComponent
import com.example.ui.components.TBillYieldDashboardComponent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun YieldAndCreditScreen(
    vaults: List<YieldVaultEntity>,
    creditProfile: CreditProfileEntity?,
    creditEvaluation: AiCreditEvaluationResult,
    simulatedMetrics: SimulatedFinancialMetrics,
    selectedPresetId: String,
    isScoreCalculating: Boolean,
    aiAdvisoryText: String?,
    isAdvisoryLoading: Boolean,
    language: String,
    onDepositVault: (String, Double) -> Unit,
    onApplyCreditLoan: (Double) -> Unit,
    onRepayCreditLoan: (Double) -> Unit,
    onSelectPreset: (String) -> Unit,
    onUpdateMetrics: (SimulatedFinancialMetrics) -> Unit,
    onRecalculateScore: () -> Unit,
    onApplyToProfile: () -> Unit,
    onRequestAiCoaching: (Int, String) -> Unit
) {
    var selectedSection by remember { mutableStateOf(1) } // Default to AI Credit Engine (section 1) or Yield Vaults (section 0)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Selector
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val sections = if (language == "SW")
                        listOf("13.5% Vaults Za Akiba", "Kifaru AI Credit Engine")
                    else
                        listOf("Savings & Yield Vaults", "Decentralized AI Credit")

                    sections.forEachIndexed { index, title ->
                        val isSelected = selectedSection == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                .clickable { selectedSection = index }
                                .padding(vertical = 10.dp)
                                .testTag("yield_credit_tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (selectedSection == 0) {
            item {
                TBillYieldDashboardComponent(
                    vaults = vaults,
                    language = language,
                    onStakeClick = onDepositVault
                )
            }
        } else {
            item {
                AiCreditScoreDashboardComponent(
                    creditProfile = creditProfile,
                    evaluationResult = creditEvaluation,
                    simulatedMetrics = simulatedMetrics,
                    selectedPresetId = selectedPresetId,
                    isCalculating = isScoreCalculating,
                    aiAdvisoryText = aiAdvisoryText,
                    isAdvisoryLoading = isAdvisoryLoading,
                    language = language,
                    onSelectPreset = onSelectPreset,
                    onUpdateMetrics = onUpdateMetrics,
                    onRecalculate = onRecalculateScore,
                    onApplyToProfile = onApplyToProfile,
                    onRequestLoan = onApplyCreditLoan,
                    onRepayLoan = onRepayCreditLoan,
                    onRequestAiCoaching = onRequestAiCoaching
                )
            }
        }
    }
}

@Composable
fun VaultCard(
    vault: YieldVaultEntity,
    language: String,
    onDepositVault: (String, Double) -> Unit
) {
    var depositAmountText by remember { mutableStateOf("1000") }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_card_${vault.code}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = vault.name,
                            tint = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = vault.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = vault.term,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = GoldAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                ) {
                    Text(
                        text = "+${vault.apy}% APY",
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Staked:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KES %,.2f".format(vault.invested),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Yield Earned:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+KES %,.2f".format(vault.earned),
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 14.sp
                    )
                }
            }

            Button(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_expand_btn_${vault.code}"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isExpanded) "Hide Staking Panel" else "Deposit & Stake Funds")
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = depositAmountText,
                        onValueChange = { depositAmountText = it },
                        label = { Text("Deposit Amount (KES / USDC)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_input_${vault.code}"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            val amt = depositAmountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onDepositVault(vault.code, amt)
                                isExpanded = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("vault_submit_btn_${vault.code}"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirm Deposit into ${vault.name}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AiCreditCard(
    creditProfile: CreditProfileEntity?,
    language: String,
    onApplyCreditLoan: (Double) -> Unit
) {
    var loanAmountText by remember { mutableStateOf("5000") }
    val score = creditProfile?.score ?: 745
    val maxLimit = creditProfile?.maxLimit ?: 50000.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_credit_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Credit Engine",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "SW") "Injini Ya Mikopo Ya AI (Vs Fuliza)" else "Decentralized AI Credit Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Score Gauge Card
            Surface(
                color = EmeraldDark.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR KIFARU CREDIT SCORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )

                    Text(
                        text = "$score",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary
                    )

                    Text(
                        text = "Score Tier: PRIME / EXCELLENT (300-850 Scale)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = creditProfile?.scoreAnalysis ?: "Score computed from Till sales cashflows, Chama participation, and mobile history.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Limit Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pre-Approved Loan Limit:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KES %,.2f".format(maxLimit),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EmeraldPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Monthly Interest:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "1.2% / Month",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GoldAccent
                    )
                }
            }

            OutlinedTextField(
                value = loanAmountText,
                onValueChange = { loanAmountText = it },
                label = { Text("Loan Amount Requested (KES)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_amount_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Comparison with Fuliza
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Cost Comparison vs Fuliza:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldAccent
                    )
                    val requestedAmt = loanAmountText.toDoubleOrNull() ?: 5000.0
                    val kifaruCost = requestedAmt * 0.012
                    val fulizaCost = requestedAmt * 0.01 * 30 // Daily compounding
                    Text(
                        text = "• Kifaru Boost (30 Days): KES ${"%.2f".format(kifaruCost)}\n• Fuliza Equivalent (30 Days): KES ${"%.2f".format(fulizaCost)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Button(
                onClick = {
                    val amt = loanAmountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && amt <= maxLimit) {
                        onApplyCreditLoan(amt)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_loan_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Request Kifaru Boost Micro-Loan (KES %,.2f)".format(loanAmountText.toDoubleOrNull() ?: 0.0),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
