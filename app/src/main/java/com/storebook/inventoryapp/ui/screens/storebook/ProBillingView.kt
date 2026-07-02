package com.storebook.inventoryapp.ui.screens.storebook

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.ProductDetails
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.data.play.PlayBillingManager
import com.storebook.inventoryapp.ui.theme.Gold200
import com.storebook.inventoryapp.ui.theme.Gold400
import com.storebook.inventoryapp.ui.theme.StoreBookTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProBillingView(
    isProActive: Boolean,
    onRequireSignIn: () -> Unit,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    val activity = remember(ctx) {
        var context = ctx
        while (context is android.content.ContextWrapper) {
            if (context is Activity) return@remember context
            context = context.baseContext
        }
        null
    }
    val billingManager = remember { PlayBillingManager(ctx.applicationContext) }
    DisposableEffect(billingManager) {
        onDispose {
            billingManager.endConnection()
        }
    }
    val billingState by billingManager.state.collectAsState()
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    var selectedPlanIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var productsFetched by remember { mutableStateOf<List<ProductDetails>?>(null) }

    LaunchedEffect(Unit) {
        if (!billingState.isBillingReady) billingManager.connect()
    }

    LaunchedEffect(billingState.isBillingReady) {
        if (billingState.isBillingReady) {
            billingManager.fetchProductDetails(
                onSuccess = { products ->
                    productsFetched = products
                },
                onFailed = { /* ignore */ },
            )
        }
    }

    val isDark = true // Always force dark mode for Premium screen

    // Premium Dark/Gold Gradient Background
    val bgGradient = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D), Color(0xFF1F1A0C))
        } else {
            listOf(Color(0xFFFFFFFF), Color(0xFFF9F9F9), Color(0xFFFFFDF5))
        }
    )

    val glassBgColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    val glassBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)

    StoreBookTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (billingState.isLoading && !billingState.isBillingReady) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Gold400)
                } else if (!billingState.isBillingReady) {
                    Text(
                        stringResource(id = R.string.pro_err_play_store),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                val isUserLoggedIn = auth.currentUser != null
                val isActuallyPro = (billingState.isProUnlocked || isProActive) && isUserLoggedIn

                // Header Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Gold400, Color(0xFFB8860B)) // Using a dark gold color
                            )
                        )
                        .border(2.dp, Gold200.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isActuallyPro) Icons.Default.WorkspacePremium else Icons.Default.AutoAwesome,
                        contentDescription = "Premium",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "StoreBook PRO",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = if (isDark) Color.White else Color.Black
                )

                Text(
                    text = "Unlock the full potential of your business with our premium tools.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (isActuallyPro) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(glassBgColor)
                            .border(1.dp, glassBorderColor, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = Gold400,
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pro Membership Active",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "You have full access to all premium features.",
                                    fontSize = 13.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Glassmorphism Features Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(glassBgColor)
                        .border(1.dp, glassBorderColor, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Premium Features",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Gold400,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val features = listOf(
                            Icons.Default.CloudSync to "Cloud Backup & Sync",
                            Icons.Default.Inventory to "Unlimited Inventory Items",
                            Icons.Default.QueryStats to "Detailed P&L Reports",
                            Icons.Default.NotificationsActive to "Smart Low-Stock Alerts",
                            Icons.Default.Share to "WhatsApp Invoice Sharing"
                        )

                        features.forEach { feature ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Gold400.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        feature.first,
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint = Gold400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = feature.second,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isActuallyPro) {
                    Button(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://play.google.com/store/account/subscriptions")
                            )
                            ctx.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold400, contentColor = Color.White)
                    ) {
                        Text("Manage Subscription", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    val products = productsFetched
                    if (products != null && products.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            products.forEachIndexed { index, product ->
                                val price =
                                    product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                                        ?: product.oneTimePurchaseOfferDetails?.formattedPrice
                                        ?: ""

                                PlanCard(
                                    icon = if (index == 0) Icons.Default.WorkspacePremium else if (index == 1) Icons.Default.Star else Icons.Default.FlashOn,
                                    title = product.name,
                                    subtitle = "$price - ${product.description}",
                                    isSelected = selectedPlanIndex == index,
                                    isDark = isDark,
                                    onClick = {
                                        selectedPlanIndex = index
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (auth.currentUser == null) {
                                    onRequireSignIn()
                                } else {
                                    val product = products[selectedPlanIndex]
                                    val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
                                    (activity as? androidx.activity.ComponentActivity)?.let { act ->
                                        billingManager.launchBillingFlow(
                                            act,
                                            product,
                                            offerToken,
                                            onSuccess = { onDismiss() },
                                            onFail = { err ->
                                                android.widget.Toast.makeText(
                                                    ctx,
                                                    err,
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold400, contentColor = Color.White)
                        ) {
                            Text("Continue to Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Fallback purchase cards
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            PlanCard(
                                icon = Icons.Default.WorkspacePremium,
                                title = "Lifetime Pro",
                                subtitle = "One time payment — Forever access",
                                isSelected = selectedPlanIndex == 0,
                                isDark = isDark,
                                onClick = {
                                    selectedPlanIndex = 0
                                }
                            )
                            PlanCard(
                                icon = Icons.Default.Star,
                                title = "Annual Pro",
                                subtitle = "₹299 / year — Best value",
                                isSelected = selectedPlanIndex == 1,
                                isDark = isDark,
                                onClick = {
                                    selectedPlanIndex = 1
                                }
                            )
                            PlanCard(
                                icon = Icons.Default.FlashOn,
                                title = "Monthly Pro",
                                subtitle = "₹79 / month — Auto-renewed",
                                isSelected = selectedPlanIndex == 2,
                                isDark = isDark,
                                onClick = {
                                    selectedPlanIndex = 2
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (auth.currentUser == null) {
                                    onRequireSignIn()
                                } else {
                                    android.widget.Toast.makeText(ctx, "Play Store billing is unavailable on this device.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold400, contentColor = Color.White)
                        ) {
                            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Purchases are detected automatically when connected to Play Store",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Close/Back Button
            FilledTonalIconButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp)
                    .size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: (() -> Unit)? = null,
) {
    val glassBgColor = if (isSelected) {
        Gold400.copy(alpha = if (isDark) 0.15f else 0.1f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    }

    val glassBorderColor = if (isSelected) {
        Gold400.copy(alpha = 0.5f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(glassBgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, glassBorderColor, RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Gold400 else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.ui_element_desc),
                    tint = if (isSelected) Color.White else (if (isDark) Color.White else Color.Black),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDark) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
                )
            }

            Icon(
                Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.ui_element_desc),
                tint = if (isSelected) Gold400 else Color.Transparent,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
