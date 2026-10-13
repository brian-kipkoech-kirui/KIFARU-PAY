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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun PayScreen(
    language: String,
    onSendP2P: (String, Double, String) -> Unit,
    onPayTillOrBill: (String, String, Double, String) -> Unit,
    onSwapCurrency: (String, String, Double) -> Unit
) {
    var selectedPayTab by remember { mutableStateOf(0) } // 0: P2P Send, 1: Paybill/Till, 2: Cross-Border FX

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selector
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
                    val tabs = if (language == "SW")
                        listOf("Tuma (0 Ada)", "Lipa Till/Bill", "FX Diaspora")
                    else
                        listOf("P2P Send (0 Fee)", "Paybill & Till", "Cross-Border FX")

                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedPayTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                .clickable { selectedPayTab = index }
                                .padding(vertical = 10.dp)
                                .testTag("pay_tab_$index"),
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
        }

        // Form based on tab selection
        item {
            when (selectedPayTab) {
                0 -> P2PForm(language, onSendP2P)
                1 -> TillAndBillForm(language, onPayTillOrBill)
                2 -> CrossBorderFxForm(language, onSwapCurrency)
            }
        }
    }
}

@Composable
fun P2PForm(
    language: String,
    onSendP2P: (String, Double, String) -> Unit
) {
    var phoneOrAddress by remember { mutableStateOf("0722123456") }
    var amountText by remember { mutableStateOf("2500") }
    var noteText by remember { mutableStateOf("Chama Contribution") }

    val amountVal = amountText.toDoubleOrNull() ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("p2p_form_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "SW") "Tuma Fedha Bila Ada" else "Zero-Fee P2P Transfer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (language == "SW")
                    "Transfa zote kati ya watumiaji wa Kifaru Pay ni za BURE KABISA shukrani kwa Base ERC-4337 Account Abstraction."
                else
                    "All transfers between Kifaru Pay users are 100% FREE powered by ERC-4337 gas abstraction on Base L2.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = phoneOrAddress,
                onValueChange = { phoneOrAddress = it },
                label = { Text(if (language == "SW") "Nambari ya Simu au Wallet Address" else "Phone Number or Wallet Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("p2p_recipient_input"),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(if (language == "SW") "Kiasi (KES)" else "Amount (KES)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("p2p_amount_input"),
                leadingIcon = {
                    Text(
                        "KES",
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(if (language == "SW") "Sababu / Kumbukumbu" else "Payment Note / Reference") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("p2p_note_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Fee Comparison Box
            Surface(
                color = EmeraldDark.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kifaru Transfer Fee:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "KES 0.00 (Zero Fee)",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            fontSize = 14.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "M-Pesa Equivalent:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val mpesaFee = when {
                            amountVal <= 100 -> 0
                            amountVal <= 1000 -> 13
                            amountVal <= 2500 -> 33
                            amountVal <= 5000 -> 57
                            else -> 78
                        }
                        Text(
                            text = "KES $mpesaFee.00",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Button(
                onClick = { onSendP2P(phoneOrAddress, amountVal, noteText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_p2p_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = phoneOrAddress.isNotBlank() && amountVal > 0
            ) {
                Text(
                    text = if (language == "SW") "Tuma KES %,.2f Sasa".format(amountVal) else "Send KES %,.2f Now (0 Fee)".format(amountVal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun TillAndBillForm(
    language: String,
    onPayTillOrBill: (String, String, Double, String) -> Unit
) {
    var payType by remember { mutableStateOf("TILL") } // "TILL" or "PAYBILL"
    var targetNumber by remember { mutableStateOf("522522") }
    var amountText by remember { mutableStateOf("1200") }
    var noteText by remember { mutableStateOf("Groceries & Duka Items") }

    val amountVal = amountText.toDoubleOrNull() ?: 0.0
    val merchantFee = amountVal * 0.001 // 0.1% rate

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("till_form_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Till & Paybill",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "SW") "Lipa Duka / Paybill" else "Pay Till Number / Paybill",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Type Toggle
            Row(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { payType = "TILL" },
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                    color = if (payType == "TILL") EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Buy Goods (Till)",
                            fontWeight = FontWeight.Bold,
                            color = if (payType == "TILL") Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { payType = "PAYBILL" },
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                    color = if (payType == "PAYBILL") EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Paybill Number",
                            fontWeight = FontWeight.Bold,
                            color = if (payType == "PAYBILL") Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            OutlinedTextField(
                value = targetNumber,
                onValueChange = { targetNumber = it },
                label = { Text(if (payType == "TILL") "Till Number (Buy Goods)" else "Paybill Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("till_number_input"),
                leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = EmeraldPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (KES)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("till_amount_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Account Number / Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("till_note_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Merchant Rate: 0.1% (vs M-Pesa 1.5%)",
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Merchant Fee for this transaction: KES ${"%.2f".format(merchantFee)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { onPayTillOrBill(payType, targetNumber, amountVal, noteText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_till_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = targetNumber.isNotBlank() && amountVal > 0
            ) {
                Text(
                    text = "Confirm $payType Payment (KES %,.2f)".format(amountVal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun CrossBorderFxForm(
    language: String,
    onSwapCurrency: (String, String, Double) -> Unit
) {
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("KES") }
    var amountText by remember { mutableStateOf("100") }

    val amountVal = amountText.toDoubleOrNull() ?: 0.0

    // Rates relative to KES
    val rates = mapOf("KES" to 1.0, "USD" to 129.50, "USDC" to 129.50, "EUR" to 141.20)
    val fromRate = rates[fromCurrency] ?: 1.0
    val toRate = rates[toCurrency] ?: 1.0

    val convertedAmount = (amountVal * fromRate) / toRate

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fx_form_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = "Cross-Border FX",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "SW") "Kubadilisha Fedha Za Kigeni (FX)" else "Cross-Border Diaspora FX",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (language == "SW")
                    "Tuma Remittance kutoka USA/Europe au badilisha USDC kwenda KES chini ya sekunde 5 kwa ada ya < $0.01."
                else
                    "Instantly convert Diaspora Remittances (USD, EUR, USDC) to KES in under 5 seconds with zero hidden margins.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // From Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("You Pay ($fromCurrency)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fx_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Currency Dropdown pills
                Column {
                    listOf("USD", "USDC", "EUR", "KES").forEach { curr ->
                        if (curr != toCurrency) {
                            Surface(
                                modifier = Modifier
                                    .clickable { fromCurrency = curr }
                                    .padding(vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = if (curr == fromCurrency) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = curr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (curr == fromCurrency) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Swap",
                    tint = GoldAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Output Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "You Receive ($toCurrency)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$toCurrency %,.2f".format(convertedAmount),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "Transparent FX Rate: 1 $fromCurrency = ${"%.2f".format(fromRate / toRate)} $toCurrency",
                        fontSize = 11.sp,
                        color = GoldAccent
                    )
                }
            }

            Button(
                onClick = { onSwapCurrency(fromCurrency, toCurrency, amountVal) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_fx_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = amountVal > 0
            ) {
                Text(
                    text = "Execute Instant FX Swap ($amountVal $fromCurrency)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
