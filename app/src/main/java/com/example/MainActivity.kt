package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.KifaruViewModel
import com.example.ui.screens.AgentScreen
import com.example.ui.screens.AiAndStrategyScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PayScreen
import com.example.ui.screens.YieldAndCreditScreen
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.KifaruPayTheme

class MainActivity : ComponentActivity() {
    private val viewModel: KifaruViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KifaruPayTheme(darkTheme = true) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: KifaruViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val agents by viewModel.agents.collectAsStateWithLifecycle()
    val yieldVaults by viewModel.yieldVaults.collectAsStateWithLifecycle()
    val creditProfile by viewModel.creditProfile.collectAsStateWithLifecycle()
    val creditEvaluation by viewModel.creditEvaluation.collectAsStateWithLifecycle()
    val simulatedMetrics by viewModel.simulatedMetrics.collectAsStateWithLifecycle()
    val selectedPresetId by viewModel.selectedPresetId.collectAsStateWithLifecycle()
    val isScoreCalculating by viewModel.isScoreCalculating.collectAsStateWithLifecycle()
    val aiCreditAdvisoryText by viewModel.aiCreditAdvisoryText.collectAsStateWithLifecycle()
    val isAdvisoryLoading by viewModel.isAdvisoryLoading.collectAsStateWithLifecycle()
    val language by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val displayCurrency by viewModel.displayCurrency.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val actionFeedback by viewModel.actionFeedback.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    LaunchedEffect(actionFeedback) {
        actionFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                language = language,
                displayCurrency = displayCurrency,
                isOfflineMode = isOfflineMode,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onToggleCurrency = { viewModel.toggleDisplayCurrency() }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                language = language,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    wallets = wallets,
                    transactions = transactions,
                    isOfflineMode = isOfflineMode,
                    language = language,
                    displayCurrency = displayCurrency,
                    fxRateKesToUsd = viewModel.fxRateKesToUsd,
                    onNavigateTab = { selectedTab = it },
                    onToggleOfflineMode = { viewModel.toggleOfflineMode() }
                )
                1 -> PayScreen(
                    language = language,
                    onSendP2P = { r, a, n -> viewModel.sendP2P(r, a, n) },
                    onPayTillOrBill = { t, n, a, note -> viewModel.payTillOrBill(t, n, a, note) },
                    onSwapCurrency = { f, t, a -> viewModel.swapCurrency(f, t, a) }
                )
                2 -> YieldAndCreditScreen(
                    vaults = yieldVaults,
                    creditProfile = creditProfile,
                    creditEvaluation = creditEvaluation,
                    simulatedMetrics = simulatedMetrics,
                    selectedPresetId = selectedPresetId,
                    isScoreCalculating = isScoreCalculating,
                    aiAdvisoryText = aiCreditAdvisoryText,
                    isAdvisoryLoading = isAdvisoryLoading,
                    language = language,
                    onDepositVault = { c, a -> viewModel.depositYieldVault(c, a) },
                    onApplyCreditLoan = { a -> viewModel.applyCreditLoan(a) },
                    onRepayCreditLoan = { a -> viewModel.repayCreditLoan(a) },
                    onSelectPreset = { id -> viewModel.selectSimulationPreset(id) },
                    onUpdateMetrics = { m -> viewModel.updateSimulatedMetrics(m) },
                    onRecalculateScore = { viewModel.recalculateCreditScore() },
                    onApplyToProfile = { viewModel.applySimulationScoreToLiveProfile() },
                    onRequestAiCoaching = { s, r -> viewModel.requestAiCreditCoaching(s, r) }
                )
                3 -> AgentScreen(
                    agents = agents,
                    language = language,
                    onAgentCashIn = { name, amt -> viewModel.executeAgentCashIn(name, amt) },
                    onAgentCashOut = { name, amt -> viewModel.executeAgentCashOut(name, amt) }
                )
                4 -> AiAndStrategyScreen(
                    aiMessages = aiMessages,
                    isAiLoading = isAiLoading,
                    language = language,
                    onSendAiPrompt = { p -> viewModel.sendAiPrompt(p) }
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    language: String,
    displayCurrency: String = "KES",
    isOfflineMode: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleCurrency: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(EmeraldDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🦏",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "KIFARU PAY",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = EmeraldPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isOfflineMode) "USSD *789# Offline" else "Base L2 Network",
                        fontSize = 9.5.sp,
                        color = GoldAccent
                    )
                }
            }

            // Header Action Buttons Row (Currency Toggle & Language Switcher)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // KES / USD Display Currency Toggle Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onToggleCurrency() }
                        .testTag("currency_toggle_btn"),
                    color = if (displayCurrency == "USD") GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (displayCurrency == "USD") GoldAccent else EmeraldPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "Currency Mode",
                            tint = if (displayCurrency == "USD") GoldAccent else EmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (displayCurrency == "KES") "KES 🇰🇪" else "USD 🇺🇸",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = if (displayCurrency == "USD") GoldAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Language Switcher Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onToggleLanguage() }
                        .testTag("language_toggle_btn"),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == "EN") "ENG" else "SWA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    language: String,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(if (language == "SW") "Nyumbani" else "Home", Icons.Default.Home, "nav_home"),
            Triple(if (language == "SW") "Lipa / Tuma" else "Pay & Send", Icons.Default.Send, "nav_pay"),
            Triple(if (language == "SW") "Akiba / Loan" else "Yield & Credit", Icons.Default.TrendingUp, "nav_yield"),
            Triple(if (language == "SW") "Agent Map" else "Agents", Icons.Default.Storefront, "nav_agent"),
            Triple(if (language == "SW") "AI & Strategy" else "AI & Docs", Icons.Default.AutoAwesome, "nav_ai")
        )

        items.forEachIndexed { index, (label, icon, tag) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.testTag(tag),
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EmeraldDark.copy(alpha = 0.5f)
                )
            )
        }
    }
}
