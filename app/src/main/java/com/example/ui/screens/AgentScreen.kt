package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentEntity
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun AgentScreen(
    agents: List<AgentEntity>,
    language: String,
    onAgentCashIn: (String, Double) -> Unit,
    onAgentCashOut: (String, Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Agent Network",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "SW") "Mtandao Wa Mageni Wa Liquidity" else "Decentralized Liquidity Agent Network",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = if (language == "SW")
                            "Mageni wetu ni Maduka, Bodaboda, na Farmasi za mtaani kwako. Hazihitaji mtaji mkubwa wa Safaricom float na zinahakikisha fedha taslimu zipo masaa 24."
                        else
                            "Kifaru replaces Safaricom's centralized agent monopoly with local Dukas, Bodabodas, and Pharmacies maintaining dynamic float balances.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(agents) { agent ->
            AgentCard(
                agent = agent,
                language = language,
                onAgentCashIn = onAgentCashIn,
                onAgentCashOut = onAgentCashOut
            )
        }
    }
}

@Composable
fun AgentCard(
    agent: AgentEntity,
    language: String,
    onAgentCashIn: (String, Double) -> Unit,
    onAgentCashOut: (String, Double) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("CASH_IN") } // "CASH_IN" or "CASH_OUT"
    var amountText by remember { mutableStateOf("1500") }

    val icon = when (agent.category) {
        "BODABODA" -> Icons.Default.DirectionsBike
        "PHARMACY" -> Icons.Default.LocalPharmacy
        "PETROL_STATION" -> Icons.Default.LocalGasStation
        else -> Icons.Default.Storefront
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_card_${agent.id}"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = agent.category,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = agent.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${agent.locationName} (${agent.distanceKm} km)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${agent.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Float levels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "KES Float:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KES %,.0f".format(agent.floatKes),
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "USDC Float:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$%,.0f USDC".format(agent.floatUsdc),
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 13.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        actionType = "CASH_IN"
                        isExpanded = !isExpanded
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("agent_cashin_btn_${agent.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cash-In Deposit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        actionType = "CASH_OUT"
                        isExpanded = !isExpanded
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("agent_cashout_btn_${agent.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cash-Out Withdraw", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (actionType == "CASH_IN")
                            "Deposit cash at ${agent.name} to receive KES in your wallet"
                        else
                            "Withdraw cash from your wallet at ${agent.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (KES)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("agent_amount_input_${agent.id}"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                if (actionType == "CASH_IN") {
                                    onAgentCashIn(agent.name, amt)
                                } else {
                                    onAgentCashOut(agent.name, amt)
                                }
                                isExpanded = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("agent_submit_btn_${agent.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (actionType == "CASH_IN") "Confirm KES %,.2f Cash-In".format(amountText.toDoubleOrNull() ?: 0.0) else "Confirm KES %,.2f Cash-Out".format(amountText.toDoubleOrNull() ?: 0.0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
