@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")
package com.storebook.inventoryapp.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.storebook.inventoryapp.ui.theme.Poppins
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.storebook.inventoryapp.R
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import com.storebook.inventoryapp.ui.theme.PrimaryButton

data class TabItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isStaff: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }

    var syncProgress by remember { mutableStateOf(0) }
    var syncMessage by remember { mutableStateOf("") }

    // Staff Auth State
    var isStaffLogin by remember { mutableStateOf(false) }
    var staffUsername by remember { mutableStateOf("") }
    var staffPassword by remember { mutableStateOf("") }

    var phoneError by remember { mutableStateOf<String?>(null) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var staffError by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = isLoading || isOtpSent) {
        if (isLoading) {
            // Block back button while loading
        } else if (isOtpSent) {
            // Go back to phone number entry
            isOtpSent = false
            otpCode = ""
            otpError = null
            phoneError = null
        }
    }

    val focusRequesterPassword = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val callbacks =
        remember {
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(auth, credential, onAuthSuccess, { p, m -> syncProgress = p; syncMessage = m }) { err ->
                        isLoading = false
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isLoading = false
                    phoneError = e.message
                    val msg = context.getString(R.string.auth_toast_verification_failed, e.message)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    verId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    isLoading = false
                    isOtpSent = true
                    verificationId = verId
                    resendToken = token
                    Toast.makeText(context, context.getString(R.string.auth_toast_otp_sent), Toast.LENGTH_SHORT).show()
                }
            }
        }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .imePadding()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // App Logo
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "StoreBook Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Name Typography
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Store Book",
                    fontFamily = Poppins,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = stringResource(R.string.auth_desc),
                fontFamily = Poppins,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Segmented Pill Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(27.dp)
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val options = listOf(
                    TabItem("Owner Login", Icons.Default.SupervisorAccount, false),
                    TabItem("Staff Login", Icons.Default.Badge, true)
                )

                options.forEach { item ->
                    val selected = isStaffLogin == item.isStaff

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(23.dp)
                            )
                            .clickable(enabled = !isLoading) {
                                isStaffLogin = item.isStaff
                                phoneError = null
                                otpError = null
                                staffError = null
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.label,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Input Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isStaffLogin) {
                        // Owner Login
                        if (!isOtpSent) {
                            Text(
                                text = stringResource(R.string.auth_enter_phone),
                                fontFamily = Poppins,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                            )

                            Text(
                                text = "We will send you a verification code",
                                fontFamily = Poppins,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 20.dp)
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = {
                                    phoneError = null
                                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                        phoneNumber = it
                                    }
                                },
                                enabled = !isLoading,
                                isError = phoneError != null,
                                supportingText = if (phoneError != null) { { Text(phoneError!!, color = MaterialTheme.colorScheme.error) } } else null,
                                label = { Text(stringResource(R.string.auth_phone_label)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint = if (phoneNumber.length == 10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                prefix = { Text("+91 ", fontWeight = FontWeight.Medium) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            val isPhoneBtnEnabled = !isLoading && phoneNumber.length == 10
                            androidx.compose.material3.Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (phoneNumber.length >= 10 && activity != null) {
                                        isLoading = true
                                        val options =
                                            PhoneAuthOptions
                                                .newBuilder(auth)
                                                .setPhoneNumber("+91$phoneNumber")
                                                .setTimeout(60L, TimeUnit.SECONDS)
                                                .setActivity(activity)
                                                .setCallbacks(callbacks)
                                                .build()
                                        PhoneAuthProvider.verifyPhoneNumber(options)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.auth_err_invalid_phone),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                enabled = isPhoneBtnEnabled,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.auth_btn_send_otp),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.auth_enter_otp),
                                fontFamily = Poppins,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                            )

                            Text(
                                text = stringResource(R.string.auth_otp_sent_to, "+91 $phoneNumber"),
                                fontFamily = Poppins,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 20.dp)
                            )

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = {
                                    otpError = null
                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                        otpCode = it
                                    }
                                },
                                enabled = !isLoading,
                                isError = otpError != null,
                                supportingText = if (otpError != null) { { Text(otpError!!, color = MaterialTheme.colorScheme.error) } } else null,
                                label = { Text(stringResource(R.string.auth_otp_label)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint = if (otpCode.length == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            val isOtpBtnEnabled = !isLoading && otpCode.length == 6
                            androidx.compose.material3.Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (otpCode.length == 6) {
                                        isLoading = true
                                        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                                        signInWithPhoneAuthCredential(auth, credential, onAuthSuccess, { p, m -> syncProgress = p; syncMessage = m }) { err ->
                                            isLoading = false
                                            otpError = err
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.auth_err_invalid_otp),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                enabled = isOtpBtnEnabled,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.auth_btn_verify),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                        }
                    } else {
                        // Staff Login
                        Text(
                            text = "Staff Login",
                            fontFamily = Poppins,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                        )

                        Text(
                            text = "Enter your username and password",
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 20.dp)
                        )

                        OutlinedTextField(
                            value = staffUsername,
                            onValueChange = { staffUsername = it; staffError = null },
                            enabled = !isLoading,
                            label = { Text("Username") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = stringResource(R.string.ui_element_desc),
                                    tint = if (staffUsername.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { focusRequesterPassword.requestFocus() }),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        var isPasswordVisible by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = staffPassword,
                            onValueChange = { staffPassword = it; staffError = null },
                            enabled = !isLoading,
                            isError = staffError != null,
                            supportingText = if (staffError != null) { { Text(staffError!!, color = MaterialTheme.colorScheme.error) } } else null,
                            label = { Text("Password (Pin)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = stringResource(R.string.ui_element_desc),
                                    tint = if (staffPassword.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterPassword),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val isStaffBtnEnabled = !isLoading && staffUsername.isNotBlank() && staffPassword.isNotBlank()
                        androidx.compose.material3.Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (staffUsername.isNotBlank() && staffPassword.isNotBlank()) {
                                    isLoading = true
                                    val dummyEmail = "${staffUsername.lowercase().replace(Regex("[^a-z0-9]"), "")}@storebook.internal"
                                    auth.signInWithEmailAndPassword(dummyEmail, staffPassword)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                val uid = auth.currentUser?.uid ?: ""
                                                if (uid.isNotEmpty()) {
                                                    db.collection("users").document(uid).get()
                                                        .addOnSuccessListener { doc ->
                                                            val role = doc.getString("role") ?: "staff"
                                                            val storeId = doc.getString("storeId") ?: "default"
                                                            val appContext = auth.app.applicationContext
                                                            val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(appContext)
                                                            prefs.edit().putString("user_role", role).putString("active_store_id", storeId).apply()

                                                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                                                try {
                                                                    com.storebook.inventoryapp.data.sync.FirestoreSyncManager(appContext).syncAllData { progress, message ->
                                                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                                            syncProgress = progress
                                                                            syncMessage = message
                                                                        }
                                                                    }
                                                                } catch (e: Exception) {
                                                                    if (e is kotlinx.coroutines.CancellationException) throw e
                                                                    if (com.storebook.inventoryapp.BuildConfig.DEBUG) android.util.Log.e("AuthScreen", "Initial staff sync failed", e)
                                                                }
                                                                kotlinx.coroutines.delay(800) // Wait for 100% animation to finish
                                                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                                    onAuthSuccess()
                                                                }
                                                            }
                                                        }
                                                        .addOnFailureListener { onAuthSuccess() }
                                                } else {
                                                    onAuthSuccess()
                                                }
                                            } else {
                                                isLoading = false
                                                staffError = task.exception?.message ?: "Login failed"
                                            }
                                        }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            enabled = isStaffBtnEnabled,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text("Log in as Staff", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Security & Trust Badges
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = stringResource(R.string.ui_element_desc),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Secure & Encrypted Backup",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Made with ❤️ for Indian Shop Owners",
                    fontFamily = Poppins,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Floating Back Button
        FilledTonalIconButton(
            onClick = {
                if (isLoading) return@FilledTonalIconButton
                if (isOtpSent) {
                    isOtpSent = false
                    otpCode = ""
                    otpError = null
                    phoneError = null
                } else {
                    onNavigateBack()
                }
            },
            enabled = !isLoading,
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
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(22.dp)
            )
        }

        // Full Screen Sync Progress Overlay
        if (isLoading && syncProgress > 0) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { /* Prevent dismiss */ },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Syncing",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Setting up your store...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = syncMessage,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val animatedProgress by animateFloatAsState(
                            targetValue = syncProgress / 100f,
                            animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
                        )

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer,
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        val displayProgress = (animatedProgress * 100).toInt()

                        Text(
                            text = "$displayProgress%",
                            fontWeight = FontWeight.Black,
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun signInWithPhoneAuthCredential(
    auth: FirebaseAuth,
    credential: PhoneAuthCredential,
    onSuccess: () -> Unit,
    onSyncProgress: (Int, String) -> Unit,
    onError: (String) -> Unit,
) {
    auth
        .signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val uid = auth.currentUser?.phoneNumber ?: auth.currentUser?.uid ?: ""
                if (uid.isNotEmpty()) {
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role") ?: "client"
                            val stores = doc.get("stores") as? List<String> ?: emptyList()
                            val storeId = if (stores.isNotEmpty()) stores[0] else "default"

                            val subscription = doc.get("subscription") as? Map<*, *>
                            val isPremium = subscription != null && subscription["plan"] == "pro" && subscription["status"] == "active"

                            val appContext = auth.app.applicationContext
                            val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(appContext)
                            val oldStoreId = prefs.getString("active_store_id", "default")

                            if (oldStoreId == "default" && storeId != "default") {
                                val oldDbFile = appContext.getDatabasePath("storebook_default.db")
                                val newDbFile = appContext.getDatabasePath("storebook_$storeId.db")
                                if (oldDbFile.exists() && !newDbFile.exists()) {
                                    oldDbFile.renameTo(newDbFile)
                                    val oldWal = java.io.File(oldDbFile.path + "-wal")
                                    if (oldWal.exists()) oldWal.renameTo(java.io.File(newDbFile.path + "-wal"))
                                    val oldShm = java.io.File(oldDbFile.path + "-shm")
                                    if (oldShm.exists()) oldShm.renameTo(java.io.File(newDbFile.path + "-shm"))
                                }
                            }

                            prefs.edit()
                                .putString("user_role", role)
                                .putString("active_store_id", storeId)
                                .putStringSet("user_stores", stores.toSet())
                                .putBoolean("is_premium", isPremium)
                                .apply()

                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                try {
                                    com.storebook.inventoryapp.data.sync.FirestoreSyncManager(appContext).syncAllData { progress, message ->
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            onSyncProgress(progress, message)
                                        }
                                    }
                                } catch (e: Exception) {
                                    if (e is kotlinx.coroutines.CancellationException) throw e
                                    if (com.storebook.inventoryapp.BuildConfig.DEBUG) android.util.Log.e("AuthScreen", "Initial owner sync failed", e)
                                }
                                kotlinx.coroutines.delay(800)
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    onSuccess()
                                }
                            }
                        }
                        .addOnFailureListener {
                            onSuccess()
                        }
                } else {
                    onSuccess()
                }
            } else {
                onError(task.exception?.message ?: "Authentication failed")
            }
        }
}
