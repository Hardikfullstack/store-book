package com.storebook.inventoryapp.ui.screens.storebook

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.storebook.inventoryapp.utils.toRupee
import com.storebook.inventoryapp.utils.toRupeeWithDecimals
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.ProductDetails
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.data.play.BillingState
import com.storebook.inventoryapp.data.play.PlayBillingManager
import com.storebook.inventoryapp.ui.theme.Gold200
import com.storebook.inventoryapp.ui.theme.Gold400

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProBillingView(isProActive: Boolean, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    val billingManager = PlayBillingManager(ctx)
    val billingState by billingManager.state.collectAsState()

    val productsFetched by remember { mutableStateOf<List<ProductDetails>?>(null) }

    LaunchedEffect(Unit) {
        if (!billingState.isBillingReady) billingManager.connect()
    }

    LaunchedEffect(billingState.isBillingReady) {
        if (billingState.isBillingReady) {
            billingManager.fetchProductDetails(
                onSuccess = { products -> /* populated via state */ },
                onFailed = { /* ignore */ }
            )
        }
    }

    Box(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        when {
            billingState.isLoading && !billingState.isBillingReady -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            !billingState.isBillingReady -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.pro_err_play_store), color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (billingState.isProUnlocked) Gold200.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (billingState.isProUnlocked) "⭐" else "🔒", fontSize = 32.sp)
                    }

                    Text(stringResource(id = R.string.more_premium), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    if (billingState.isProUnlocked) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, "Pro active", tint = Gold400, modifier = Modifier.size(28.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(id = R.string.pro_active_title), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(stringResource(id = R.string.pro_active_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Features card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Gold200.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(stringResource(id = R.string.prem_pro_features), fontWeight = FontWeight.Black, fontSize = 15.sp)
                            listOf(
                                "☁️ Cloud backup & sync",
                                "📦 Unlimited inventory items",
                                "📊 Detailed P&L reports",
                                "🔔 Smart low-stock alerts",
                                "📱 WhatsApp invoice sharing"
                            ).forEach { feature ->
                                Text(feature, fontSize = 13.sp)
                            }
                        }
                    }

                    // Fallback purchase cards (shown when real product data isn't fetched)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlanCard(
                            icon = "💎",
                            title = "Lifetime Pro",
                            subtitle = "One time payment — Forever access",
                            isPrimary = true
                        )
                        PlanCard(
                            icon = "🎉",
                            title = "Annual Pro",
                            subtitle = "₹299 / year — Best value",
                            isPrimary = false
                        )
                        PlanCard(
                            icon = "👤",
                            title = "Monthly Pro",
                            subtitle = "₹79 / month — Auto-renewed",
                            isPrimary = false
                        )
                    }

                    HorizontalDivider()
                    Text(
                        "Purchases are detected automatically when connected to Play Store",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    icon: String,
    title: String,
    subtitle: String,
    isPrimary: Boolean,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).then(
                if (onClick == null) Modifier.alpha(0.5f) else Modifier
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Default.Check, "",
                tint = if (isPrimary) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
