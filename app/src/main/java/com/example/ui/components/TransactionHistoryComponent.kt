package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransactionFilter {
    ALL, INFLOWS, PAYMENTS, PENDING, FAILED
}

data class TransactionStatusInfo(
    val code: String,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color,
    val description: String
)

fun getTransactionStatusInfo(status: String, language: String = "EN"): TransactionStatusInfo {
    val upper = status.uppercase(Locale.getDefault())
    return when {
        upper in setOf("SUCCESS", "SUCCESSFUL", "COMPLETED", "SETTLED") -> TransactionStatusInfo(
            code = "COMPLETED",
            label = if (language == "SW") "Imekamilika" else "Completed",
            icon = Icons.Default.CheckCircle,
            color = EmeraldPrimary,
            backgroundColor = EmeraldPrimary.copy(alpha = 0.15f),
            description = if (language == "SW") "Muamala umekamilika kikamilifu na kuthibitishwa kwenye mtandao wa Base L2" else "Transaction successfully settled and verified on Base L2 network"
        )
        upper in setOf("PENDING", "PROCESSING", "IN_PROGRESS") -> TransactionStatusInfo(
            code = "PENDING",
            label = if (language == "SW") "Inasubiri" else "Pending",
            icon = Icons.Default.Pending,
            color = GoldAccent,
            backgroundColor = GoldAccent.copy(alpha = 0.15f),
            description = if (language == "SW") "Muamala unachakatwa, unasubiri saini ya node za mtandao" else "Transaction processing, awaiting node signatures"
        )
        upper in setOf("FAILED", "REJECTED", "DECLINED", "CANCELLED", "ERROR") -> TransactionStatusInfo(
            code = "FAILED",
            label = if (language == "SW") "Imefeli" else "Failed",
            icon = Icons.Default.Error,
            color = Color(0xFFFF5252),
            backgroundColor = Color(0xFFFF5252).copy(alpha = 0.15f),
            description = if (language == "SW") "Muamala haukufanikiwa au ulakataliwa na daraja la malipo" else "Transaction failed or declined by payment bridge"
        )
        else -> TransactionStatusInfo(
            code = upper,
            label = status.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() },
            icon = Icons.Default.Info,
            color = Color(0xFF90A4AE),
            backgroundColor = Color(0xFF90A4AE).copy(alpha = 0.15f),
            description = "Status: $status"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryComponent(
    transactions: List<TransactionEntity>,
    language: String = "EN",
    modifier: Modifier = Modifier,
    onTransactionClick: ((TransactionEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTxForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    // Derived statistics
    val totalInflow by remember(transactions) {
        derivedStateOf {
            transactions
                .filter { isTransactionInflow(it) }
                .sumOf { it.amount }
        }
    }

    val totalOutflow by remember(transactions) {
        derivedStateOf {
            transactions
                .filter { !isTransactionInflow(it) }
                .sumOf { it.amount }
        }
    }

    val totalFeesSaved by remember(transactions) {
        derivedStateOf {
            // Estimated savings vs M-Pesa standard tier (~1.5% fee)
            transactions.sumOf { (it.amount * 0.015) - it.fee }.coerceAtLeast(0.0)
        }
    }

    // Filtered transaction list
    val filteredTransactions by remember(transactions, selectedFilter, searchQuery, language) {
        derivedStateOf {
            transactions.filter { tx ->
                val statusInfo = getTransactionStatusInfo(tx.status, language)
                val matchesFilter = when (selectedFilter) {
                    TransactionFilter.ALL -> true
                    TransactionFilter.INFLOWS -> isTransactionInflow(tx)
                    TransactionFilter.PAYMENTS -> !isTransactionInflow(tx)
                    TransactionFilter.PENDING -> statusInfo.code == "PENDING"
                    TransactionFilter.FAILED -> statusInfo.code == "FAILED"
                }

                val matchesSearch = searchQuery.isBlank() ||
                        tx.recipient.contains(searchQuery, ignoreCase = true) ||
                        tx.sender.contains(searchQuery, ignoreCase = true) ||
                        tx.txType.contains(searchQuery, ignoreCase = true) ||
                        tx.notes.contains(searchQuery, ignoreCase = true) ||
                        tx.blockchainHash.contains(searchQuery, ignoreCase = true) ||
                        statusInfo.label.contains(searchQuery, ignoreCase = true)

                matchesFilter && matchesSearch
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_history_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Header Section & Summary Metrics Bar ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("transaction_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "History",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == "SW") "Lugha ya Miamala" else "Transaction Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        color = EmeraldDark.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (language == "SW") "Ada: KES 0 Saved" else "Saved KES %,.0f".format(totalFeesSaved),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inflows vs Outflows Metric Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Inflows Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_inflows_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Inflows",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == "SW") "Mapato (Inflows)" else "Total Inflows",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+ KES %,.0f".format(totalInflow),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                )
                            }
                        }
                    }

                    // Total Payments Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_outflows_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFF371A1A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Payments",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == "SW") "Matumizi (Payments)" else "Total Payments",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "- KES %,.0f".format(totalOutflow),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Search Bar Component ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("transaction_search_input"),
            placeholder = {
                Text(
                    text = if (language == "SW") "Tafuta muamala, namba au mpokeaji..." else "Search payments, recipients, or hash...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
        )

        // --- Filter Chips Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChipItem(
                label = if (language == "SW") "Zote (${transactions.size})" else "All (${transactions.size})",
                selected = selectedFilter == TransactionFilter.ALL,
                tag = "filter_chip_all",
                onClick = { selectedFilter = TransactionFilter.ALL }
            )

            FilterChipItem(
                label = if (language == "SW") "Mapato 📥" else "Inflows 📥",
                selected = selectedFilter == TransactionFilter.INFLOWS,
                tag = "filter_chip_inflows",
                onClick = { selectedFilter = TransactionFilter.INFLOWS }
            )

            FilterChipItem(
                label = if (language == "SW") "Malipo 📤" else "Payments 📤",
                selected = selectedFilter == TransactionFilter.PAYMENTS,
                tag = "filter_chip_payments",
                onClick = { selectedFilter = TransactionFilter.PAYMENTS }
            )

            FilterChipItem(
                label = if (language == "SW") "Inasubiri ⏳" else "Pending ⏳",
                selected = selectedFilter == TransactionFilter.PENDING,
                tag = "filter_chip_pending",
                onClick = { selectedFilter = TransactionFilter.PENDING }
            )

            FilterChipItem(
                label = if (language == "SW") "Imefeli ❌" else "Failed ❌",
                selected = selectedFilter == TransactionFilter.FAILED,
                tag = "filter_chip_failed",
                onClick = { selectedFilter = TransactionFilter.FAILED }
            )
        }

        // --- Transactions List Section ---
        if (filteredTransactions.isEmpty()) {
            EmptyTransactionsState(language = language, searchQuery = searchQuery)
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.animateContentSize()
            ) {
                filteredTransactions.forEach { tx ->
                    SleekTransactionCard(
                        tx = tx,
                        language = language,
                        onClick = {
                            selectedTxForDetail = tx
                            onTransactionClick?.invoke(tx)
                        }
                    )
                }
            }
        }
    }

    // --- Detail Bottom Sheet Modal ---
    selectedTxForDetail?.let { tx ->
        TransactionDetailModal(
            tx = tx,
            language = language,
            onDismiss = { selectedTxForDetail = null }
        )
    }
}

@Composable
fun FilterChipItem(
    label: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        modifier = Modifier.testTag(tag),
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPrimary,
            selectedLabelColor = Color.Black,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedBorderColor = GoldAccent
        )
    )
}

@Composable
fun SleekTransactionCard(
    tx: TransactionEntity,
    language: String,
    onClick: () -> Unit
) {
    val isInflow = isTransactionInflow(tx)
    val statusInfo = getTransactionStatusInfo(tx.status, language)
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(tx.timestamp))

    val borderColor = when (statusInfo.code) {
        "FAILED" -> Color(0xFFFF5252).copy(alpha = 0.35f)
        "PENDING" -> GoldAccent.copy(alpha = 0.35f)
        else -> if (isInflow) EmeraldPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("transaction_item_${tx.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Category Visual Icon Badge with Status Overlay
            Box(
                modifier = Modifier.size(46.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = when {
                                statusInfo.code == "FAILED" -> Color(0xFFFF5252).copy(alpha = 0.12f)
                                isInflow -> EmeraldPrimary.copy(alpha = 0.15f)
                                tx.txType.contains("CREDIT", ignoreCase = true) -> GoldAccent.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getTransactionIcon(tx.txType, isInflow),
                        contentDescription = tx.txType,
                        tint = when {
                            statusInfo.code == "FAILED" -> Color(0xFFFF5252)
                            isInflow -> EmeraldPrimary
                            tx.txType.contains("CREDIT", ignoreCase = true) -> GoldAccent
                            else -> Color(0xFFFF6B6B)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Small circular status indicator badge overlaid on avatar
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusInfo.icon,
                        contentDescription = statusInfo.label,
                        tint = statusInfo.color,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details Title, Status Badge & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getDisplayName(tx, isInflow),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Explicit status indicator pill tag
                    Surface(
                        color = statusInfo.backgroundColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = statusInfo.icon,
                                contentDescription = null,
                                tint = statusInfo.color,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = statusInfo.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusInfo.color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${getCategoryLabel(tx.txType)} • $formattedTime",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (tx.notes.isNotBlank()) {
                    Text(
                        text = tx.notes,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount & Zero Fee Tag Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isInflow) "+" else "-"}${tx.currency} %,.2f".format(tx.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = when {
                        statusInfo.code == "FAILED" -> Color(0xFFFF5252).copy(alpha = 0.8f)
                        isInflow -> EmeraldPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(2.dp))

                Surface(
                    color = if (tx.fee == 0.0) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (tx.fee == 0.0) "0 Fee" else "Fee: KES ${"%.2f".format(tx.fee)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.fee == 0.0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionsState(language: String, searchQuery: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "No results",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (searchQuery.isNotBlank()) {
                    if (language == "SW") "Hakuna miamala inayolingana na '$searchQuery'" else "No transactions matching '$searchQuery'"
                } else {
                    if (language == "SW") "Hakuna miamala katika kitengo hiki" else "No transactions found in this category"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (language == "SW") "Jaribu kubadilisha vichujio au neno la utafutaji." else "Try adjusting your search query or filter selection.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailModal(
    tx: TransactionEntity,
    language: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val isInflow = isTransactionInflow(tx)

    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm:ss", Locale.getDefault())
    val fullDateStr = dateFormat.format(Date(tx.timestamp))

    val statusInfo = getTransactionStatusInfo(tx.status, language)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("transaction_detail_modal")
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isInflow) EmeraldPrimary.copy(alpha = 0.15f) else Color(0xFF371A1A),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isInflow) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = "Direction",
                            tint = if (isInflow) EmeraldPrimary else Color(0xFFFF6B6B)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isInflow) "Payment Received" else "Payment Outflow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = getCategoryLabel(tx.txType),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusInfo.backgroundColor,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusInfo.color.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = null,
                            tint = statusInfo.color,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusInfo.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusInfo.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Status Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = statusInfo.backgroundColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, statusInfo.color.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusInfo.icon,
                        contentDescription = "Status Detail",
                        tint = statusInfo.color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Status: ${statusInfo.label}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = statusInfo.color
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = statusInfo.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Amount Display Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (language == "SW") "Kiasi Cha Muamala" else "Transaction Amount",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (isInflow) "+" else "-"}${tx.currency} %,.2f".format(tx.amount),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            statusInfo.code == "FAILED" -> Color(0xFFFF5252)
                            isInflow -> EmeraldPrimary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = "Status Verified",
                            tint = statusInfo.color,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${statusInfo.label} • Base L2 Smart Account",
                            fontSize = 11.sp,
                            color = statusInfo.color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detail Rows
            DetailRow(
                label = if (language == "SW") "Mtumaji" else "Sender",
                value = tx.sender.ifBlank { "Kifaru Pay Account" }
            )
            DetailRow(
                label = if (language == "SW") "Mpokeaji" else "Recipient",
                value = tx.recipient.ifBlank { "Kifaru Merchant / User" }
            )
            DetailRow(
                label = if (language == "SW") "Tarehe na Muda" else "Date & Time",
                value = fullDateStr
            )
            DetailRow(
                label = if (language == "SW") "Mtandao" else "Payment Network",
                value = "Base L2 Smart Account (*789# USSD)"
            )

            if (tx.notes.isNotBlank()) {
                DetailRow(
                    label = if (language == "SW") "Kumbukumbu" else "Reference Note",
                    value = tx.notes
                )
            }

            // Blockchain Hash Copy Row
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Blockchain Tx Hash",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = tx.blockchainHash,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Tx Hash", tx.blockchainHash)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied Tx Hash to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_hash_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Hash",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Receipt exported to PDF!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_receipt_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (language == "SW") "Shiriki Risiti" else "Share Receipt")
                }

                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("close_detail_modal_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (language == "SW") "Funga" else "Done",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Utility Helper Functions
fun isTransactionInflow(tx: TransactionEntity): Boolean {
    val inflowTypes = setOf("AGENT_CASH_IN", "CREDIT_LOAN", "SAVINGS_WITHDRAW", "INFLOW")
    if (inflowTypes.contains(tx.txType.uppercase(Locale.getDefault()))) return true
    if (tx.recipient.equals("You", ignoreCase = true) || tx.recipient.equals("User", ignoreCase = true)) return true
    return false
}

fun getTransactionIcon(txType: String, isInflow: Boolean): ImageVector {
    return when {
        isInflow -> Icons.Default.ArrowDownward
        txType.contains("PAYBILL", ignoreCase = true) -> Icons.Default.Receipt
        txType.contains("TILL", ignoreCase = true) -> Icons.Default.Receipt
        txType.contains("SWAP", ignoreCase = true) || txType.contains("CROSS_BORDER", ignoreCase = true) -> Icons.Default.CurrencyExchange
        txType.contains("CREDIT", ignoreCase = true) || txType.contains("LOAN", ignoreCase = true) -> Icons.Default.TrendingUp
        else -> Icons.Default.ArrowUpward
    }
}

fun getCategoryLabel(txType: String): String {
    return when (txType.uppercase(Locale.getDefault())) {
        "P2P_SEND" -> "P2P Transfer"
        "PAYBILL" -> "Paybill Payment"
        "TILL" -> "Buy Goods Till"
        "CROSS_BORDER" -> "Diaspora FX Transfer"
        "SAVINGS_DEPOSIT" -> "13.5% Yield Deposit"
        "SAVINGS_WITHDRAW" -> "Yield Vault Withdrawal"
        "CREDIT_LOAN" -> "Micro-Credit Disbursement"
        "AGENT_CASH_IN" -> "Agent Float Cash In"
        "AGENT_CASH_OUT" -> "Agent Float Cash Out"
        else -> txType.replace("_", " ")
    }
}

fun getDisplayName(tx: TransactionEntity, isInflow: Boolean): String {
    return if (isInflow) {
        if (tx.sender.isNotBlank()) "From ${tx.sender}" else "Deposit / Inflow"
    } else {
        if (tx.recipient.isNotBlank()) "To ${tx.recipient}" else "Payment / Outflow"
    }
}
