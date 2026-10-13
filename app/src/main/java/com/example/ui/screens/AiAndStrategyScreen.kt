package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AiMessage
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun AiAndStrategyScreen(
    aiMessages: List<AiMessage>,
    isAiLoading: Boolean,
    language: String,
    onSendAiPrompt: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Kifaru AI Chat, 1: Whitepaper & Architecture Strategy

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Switcher
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val tabs = if (language == "SW")
                    listOf("Mshauri Wa AI", "Whitepaper & Mbinu")
                else
                    listOf("Kifaru AI Assistant", "Strategy & Whitepaper")

                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp)
                            .testTag("ai_strategy_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (selectedTab == 0) {
            KifaruAiChatSection(
                aiMessages = aiMessages,
                isAiLoading = isAiLoading,
                language = language,
                onSendAiPrompt = onSendAiPrompt
            )
        } else {
            WhitepaperSection(language = language)
        }
    }
}

@Composable
fun KifaruAiChatSection(
    aiMessages: List<AiMessage>,
    isAiLoading: Boolean,
    language: String,
    onSendAiPrompt: (String) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Question Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = if (language == "SW")
                listOf("M-Pesa Costs?", "T-Bill Yield?", "AI Loan?")
            else
                listOf("M-Pesa Costs?", "T-Bill Yield?", "AI Loan?")

            suggestions.forEach { sug ->
                Surface(
                    modifier = Modifier
                        .clickable { onSendAiPrompt(sug) }
                        .testTag("sug_chip_$sug"),
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldDark.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = sug,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(aiMessages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) EmeraldPrimary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUser) Icons.Default.Send else Icons.Default.AutoAwesome,
                                    contentDescription = msg.sender,
                                    tint = if (isUser) Color.Black else GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUser) "You" else "Kifaru AI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) Color.Black else GoldAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.message,
                                fontSize = 13.sp,
                                color = if (isUser) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = EmeraldPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kifaru AI is generating response...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Input Field Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text(if (language == "SW") "Uliza swali kuhusu Kifaru..." else "Ask Kifaru AI a question...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        onSendAiPrompt(promptInput)
                        promptInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(EmeraldPrimary, CircleShape)
                    .testTag("send_ai_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun WhitepaperSection(language: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("whitepaper_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Whitepaper",
                            tint = GoldAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kifaru Ecosystem Architecture & Strategy Proposal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "EXECUTIVE SUMMARY & SYSTEM SPECIFICATION",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 12.sp
                    )

                    WhitepaperParagraph(
                        title = "1. Challenge Assumptions & Market Reality",
                        content = "M-Pesa dominates Kenya with >98% market share and 30M+ active users, driven by its 300,000+ agent float monopoly and network effects. Replacing it requires a hybrid strategy: rather than fighting agents, Kifaru converts existing Dukas, Bodabodas, and Pharmacies into decentralized liquidity nodes. USSD (*789#) and encrypted SMS backhaul ensure access for feature phone users without requiring smartphone migration."
                    )

                    WhitepaperParagraph(
                        title = "2. Zero-Fee Economics & Monetization Engine",
                        content = "P2P transactions are 100% sponsored via ERC-4337 Account Abstraction on Base L2. Platform monetization shifts away from user friction to B2B FX spreads (0.25% on Diaspora remittances), merchant payment processing (0.1% vs Safaricom's 1.5%), 13.5% Treasury Bill yield sharing (1.5% management margin), and AI-driven micro-loan interest (1.2%/month vs Fuliza daily compounding)."
                    )

                    WhitepaperParagraph(
                        title = "3. Core Architecture & Regulatory Blueprint",
                        content = "Built on Base L2 for <$0.001 gas fees and 1,000+ TPS capacity. Fully compliant with Central Bank of Kenya (CBK) Payment System Regulations, Kenya Data Protection Act 2019, and FATF Travel Rule requirements via an onshore CBK-licensed Payment Service Provider (PSP) entity."
                    )

                    WhitepaperParagraph(
                        title = "4. Decentralized AI Credit Scoring Engine",
                        content = "Replaces punitive collateral and daily overdraft fees with dynamic ML scoring derived from mobile cashflow graphs, till volume, Chama group history, and salary inflows — enabling instant zero-paperwork micro-loans."
                    )
                }
            }
        }
    }
}

@Composable
fun WhitepaperParagraph(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = GoldAccent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = content,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
