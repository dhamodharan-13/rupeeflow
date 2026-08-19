package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.viewmodel.FinanceUiState
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun WealthDetailsScreen(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToCustody: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBalanceHidden by remember { mutableStateOf(true) }
    val balances = uiState.balances

    // Calculate percentage weights
    val totalPositive = maxOf(
        1.0,
        maxOf(0.0, balances.myUpiBalance) + maxOf(0.0, balances.walletBalance) + maxOf(0.0, balances.treasureSafeBalance)
    )
    val upiPct = if (totalPositive > 0) ((maxOf(0.0, balances.myUpiBalance) / totalPositive) * 100).toInt() else 0
    val walletPct = if (totalPositive > 0) ((maxOf(0.0, balances.walletBalance) / totalPositive) * 100).toInt() else 0
    val safePct = if (totalPositive > 0) ((maxOf(0.0, balances.treasureSafeBalance) / totalPositive) * 100).toInt() else 0

    val upiWeight = (maxOf(0.0, balances.myUpiBalance) / totalPositive).toFloat().coerceAtLeast(0.05f)
    val walletWeight = (maxOf(0.0, balances.walletBalance) / totalPositive).toFloat().coerceAtLeast(0.05f)
    val safeWeight = (maxOf(0.0, balances.treasureSafeBalance) / totalPositive).toFloat().coerceAtLeast(0.05f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wealth_details_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Navigation Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_from_wealth")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "My Wealth Breakdown",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { isBalanceHidden = !isBalanceHidden },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Balance Privacy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Net Worth Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "TOTAL NET WORTH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isBalanceHidden) "₹ ••••••••"
                        else String.format(Locale.getDefault(), "₹%,.2f", balances.myTotalNetWorth),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress allocation bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(upiWeight)
                                .fillMaxHeight()
                                .background(AppleBlue)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .weight(walletWeight)
                                .fillMaxHeight()
                                .background(AppleGreen)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .weight(safeWeight)
                                .fillMaxHeight()
                                .background(AppleOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Percentage Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AllocationBadge(label = "UPI", percentage = "$upiPct%", color = AppleBlue)
                        AllocationBadge(label = "Wallet", percentage = "$walletPct%", color = AppleGreen)
                        AllocationBadge(label = "Safe", percentage = "$safePct%", color = AppleOrange)
                    }
                }
            }
        }

        // 3. Accounts & Storage Cards Header
        item {
            Text(
                text = "YOUR 3 ACCOUNTS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // 4. Account 1: Bank UPI Detailed Card
        item {
            DetailedAccountCard(
                title = "Bank UPI Account",
                subtitle = "Digital bank balance for online transactions",
                balance = balances.myUpiBalance,
                accentColor = AppleBlue,
                icon = Icons.Default.CreditCard,
                percentage = upiPct,
                extraInfo = if (balances.otherPeopleTotalHeld > 0) {
                    "+₹${String.format(Locale.getDefault(), "%,.0f", balances.otherPeopleTotalHeld)} held for others (Bank total: ₹${String.format(Locale.getDefault(), "%,.0f", balances.totalBankUpiBalance)})"
                } else null,
                onExtraInfoClick = onNavigateToCustody,
                isHidden = isBalanceHidden
            )
        }

        // 5. Account 2: Cash Wallet Detailed Card
        item {
            DetailedAccountCard(
                title = "Cash in Wallet",
                subtitle = "Physical cash bills & coins on hand",
                balance = balances.walletBalance,
                accentColor = AppleGreen,
                icon = Icons.Default.Wallet,
                percentage = walletPct,
                isHidden = isBalanceHidden
            )
        }

        // 6. Account 3: Treasure Safe Detailed Card
        item {
            DetailedAccountCard(
                title = "Treasure Safe",
                subtitle = "Emergency home stash & reserve savings",
                balance = balances.treasureSafeBalance,
                accentColor = AppleOrange,
                icon = Icons.Default.Lock,
                percentage = safePct,
                isHidden = isBalanceHidden
            )
        }
    }
}

@Composable
fun DetailedAccountCard(
    title: String,
    subtitle: String,
    balance: Double,
    accentColor: Color,
    icon: ImageVector,
    percentage: Int,
    isHidden: Boolean,
    extraInfo: String? = null,
    onExtraInfoClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detailed_card_${title.lowercase()}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Icon + Title + Percentage Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Big Amount Display
            Text(
                text = if (isHidden) "₹ ••••••••"
                else String.format(Locale.getDefault(), "₹%,.2f", balance),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Optional extra custody badge for Bank UPI
            if (extraInfo != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExtraInfoClick?.invoke() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isHidden) "Bank UPI: ₹••••" else extraInfo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllocationBadge(
    label: String,
    percentage: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = percentage,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
