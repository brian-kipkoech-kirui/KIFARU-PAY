package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.TransactionEntity
import com.example.data.WalletEntity
import com.example.ui.components.TransactionHistoryComponent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    wallets: List<WalletEntity>,
    transactions: List<TransactionEntity>,
    isOfflineMode: Boolean,
    language: String,
    displayCurrency: String = "KES",
    fxRateKesToUsd: Double = 129.50,
    onNavigateTab: (Int) -> Unit,
    onToggleOfflineMode: () -> Unit
) {
    var selectedWalletCurrency by remember { mutableStateOf("KES") }
    val activeWallet = wallets.find { it.currency == selectedWalletCurrency } ?: wallets.firstOrNull()

    // Calculate converted display balance if global display currency is USD
    val rawBalance = activeWallet?.balance ?: 0.0
    val isWalletKes = activeWallet?.currency == "KES"
    val isWalletUsd = activeWallet?.currency == "USD"

    val (formattedCurrencySymbol, displayAmountVal, secondaryConversionNotice) = when {
        displayCurrency == "USD" && isWalletKes -> {
            val usdVal = rawBalance / fxRateKesToUsd
            Triple("USD $", usdVal, "≈ KES %,.2f @ %.2f KES/USD".format(rawBalance, fxRateKesToUsd))
        }
        displayCurrency == "KES" && isWalletUsd -> {
            val kesVal = rawBalance * fxRateKesToUsd
            Triple("KES", kesVal, "≈ $%,.2f USD @ %.2f KES/USD".format(rawBalance, fxRateKesToUsd))
        }
        else -> {
            Triple(activeWallet?.currency ?: "KES", rawBalance, null)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card with Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_wallet_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Header Banner background
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner_1786106812400),
                        contentDescription = "Header Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(EmeraldPrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isOfflineMode) "USSD Offline (*789#)" else "Base L2 Verified",
                                        color = EmeraldPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = onToggleOfflineMode,
                                modifier = Modifier.testTag("offline_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isOfflineMode) Icons.Default.Call else Icons.Default.Lock,
                                    contentDescription = "Toggle Network Mode",
                                    tint = if (isOfflineMode) GoldAccent else EmeraldPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (language == "SW") "Salio Lako La Sasa" else "Total Available Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = formattedCurrencySymbol,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier.padding(bottom = 4.dp, end = 6.dp)
                            )
                            Text(
                                text = "%,.2f".format(displayAmountVal),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (secondaryConversionNotice != null) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = secondaryConversionNotice,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = "Account: ${activeWallet?.accountHash ?: "0x7F2...KFR89"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Currency Switcher Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            wallets.forEach { wallet ->
                                val isSelected = wallet.currency == selectedWalletCurrency
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedWalletCurrency = wallet.currency }
                                        .testTag("wallet_tab_${wallet.currency}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GoldAccent) else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = wallet.currency,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "%,.0f".format(wallet.balance),
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.Black.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = if (language == "SW") "Huduma Za Haraka" else "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Send,
                    label = if (language == "SW") "Tuma (0 Ada)" else "Send (0 Fee)",
                    tag = "action_send_btn",
                    modifier = Modifier.weight(1f)
                ) { onNavigateTab(1) }

                QuickActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = if (language == "SW") "Lipa Till/Bill" else "Pay Till/Bill",
                    tag = "action_pay_btn",
                    modifier = Modifier.weight(1f)
                ) { onNavigateTab(1) }

                QuickActionButton(
                    icon = Icons.Default.CurrencyExchange,
                    label = if (language == "SW") "Badilisha FX" else "Diaspora FX",
                    tag = "action_fx_btn",
                    modifier = Modifier.weight(1f)
                ) { onNavigateTab(1) }

                QuickActionButton(
                    icon = Icons.Default.TrendingUp,
                    label = if (language == "SW") "13.5% Akiba" else "13.5% Yield",
                    tag = "action_yield_btn",
                    modifier = Modifier.weight(1f)
                ) { onNavigateTab(2) }
            }
        }

        // Feature Comparison Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Zero Fees",
                            tint = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == "SW") "Kifaru Pay vs M-Pesa" else "Why Switch from M-Pesa?",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (language == "SW")
                                "• KES 0 kwa kutuma fedha\n• 0.1% pekee kwa wafanyabiashara\n• Faida ya 13.5% T-Bill kwenye akiba"
                            else
                                "• Zero P2P transfer fees (vs M-Pesa's high tiers)\n• 0.1% merchant rate (vs 1.5% Till)\n• 13.5% APY yield on idle balances",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // AI Credit Score Teaser Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateTab(2) }
                    .testTag("home_ai_credit_banner"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(GoldAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Credit",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == "SW") "Alama ya Mkopo ya AI" else "AI Underwritten Credit Score",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "785 • Prime",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (language == "SW")
                                "Kikomo cha mkopo: KES 80,000 (Riba 1.1%/mwezi). Gusa kuona uchambuzi wa AI →"
                            else
                                "Pre-approved up to KES 80,000 at 1.1%/mo. Tap to simulate & unlock →",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Recent Transactions History Component
        item {
            TransactionHistoryComponent(
                transactions = transactions,
                language = language
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity) {
    val isOutgoing = tx.txType == "P2P_SEND" || tx.txType == "PAYBILL" || tx.txType == "TILL" || tx.txType == "SAVINGS_DEPOSIT" || tx.txType == "AGENT_CASH_OUT"
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(tx.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (isOutgoing) Color(0xFF370000) else Color(0xFF003713),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOutgoing) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = tx.txType,
                    tint = if (isOutgoing) Color(0xFFFF6B6B) else EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.recipient.ifEmpty { tx.sender },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${tx.txType.replace("_", " ")} • $formattedTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Tx: ${tx.blockchainHash.take(16)}...",
                    fontSize = 10.sp,
                    color = GoldAccent.copy(alpha = 0.8f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isOutgoing) "-" else "+"}${tx.currency} %,.2f".format(tx.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isOutgoing) MaterialTheme.colorScheme.onSurface else EmeraldPrimary
                )

                Text(
                    text = if (tx.fee == 0.0) "Fee: KES 0" else "Fee: KES ${"%.2f".format(tx.fee)}",
                    fontSize = 11.sp,
                    color = if (tx.fee == 0.0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
