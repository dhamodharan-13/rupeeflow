package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.BackupDialog
import com.example.ui.screens.AnalysisScreen
import com.example.ui.screens.CustodyScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                var showBackupDialog by remember { mutableStateOf(false) }
                var showAddTransactionDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (selectedTabIndex) {
                                        0 -> "Finance Tracker"
                                        1 -> "Spending Insights"
                                        else -> "People's UPI"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { showBackupDialog = true },
                                    modifier = Modifier.testTag("btn_backup_restore")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Backup & Transfer Data",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    floatingActionButton = {
                        // Circled plus button in the bottom
                        FloatingActionButton(
                            onClick = { showAddTransactionDialog = true },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(6.dp),
                            modifier = Modifier
                                .size(58.dp)
                                .testTag("btn_fab_add_transaction")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Transaction",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            // Tab 1: Tracker
                            NavigationBarItem(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTabIndex == 0) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = "Tracker"
                                    )
                                },
                                label = { Text("Tracker") },
                                modifier = Modifier.testTag("nav_tab_tracker")
                            )

                            // Tab 2: Analysis
                            NavigationBarItem(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTabIndex == 1) Icons.Default.Analytics else Icons.Outlined.Analytics,
                                        contentDescription = "Analysis"
                                    )
                                },
                                label = { Text("Analysis") },
                                modifier = Modifier.testTag("nav_tab_analysis")
                            )

                            // Tab 3: People's UPI
                            NavigationBarItem(
                                selected = selectedTabIndex == 2,
                                onClick = { selectedTabIndex = 2 },
                                icon = {
                                    if (uiState.balances.otherPeopleTotalHeld > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge {
                                                    Text("${uiState.people.size}")
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selectedTabIndex == 2) Icons.Default.People else Icons.Outlined.People,
                                                contentDescription = "People"
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selectedTabIndex == 2) Icons.Default.People else Icons.Outlined.People,
                                            contentDescription = "People"
                                        )
                                    }
                                },
                                label = { Text("People's UPI") },
                                modifier = Modifier.testTag("nav_tab_people")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTabIndex) {
                            0 -> DashboardScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onNavigateToCustody = { selectedTabIndex = 2 }
                            )
                            1 -> AnalysisScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            2 -> CustodyScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                    }
                }

                if (showAddTransactionDialog) {
                    AddTransactionDialog(
                        initialType = TransactionType.EXPENSE,
                        initialMethod = PaymentMethod.UPI,
                        onDismiss = { showAddTransactionDialog = false },
                        onConfirm = { amount, type, method, reason, category, dateMillis ->
                            viewModel.addTransaction(
                                amount = amount,
                                type = type,
                                method = method,
                                reason = reason,
                                category = category,
                                dateMillis = dateMillis
                            )
                            showAddTransactionDialog = false
                        }
                    )
                }

                if (showBackupDialog) {
                    BackupDialog(
                        viewModel = viewModel,
                        onDismiss = { showBackupDialog = false }
                    )
                }
            }
        }
    }
}
