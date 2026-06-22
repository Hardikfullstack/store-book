package com.storebook.inventoryapp.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    
    // Staff Auth State
    var isStaffLogin by remember { mutableStateOf(false) }
    var staffUsername by remember { mutableStateOf("") }
    var staffPassword by remember { mutableStateOf("") }

    val callbacks =
        remember {
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval or instant validation
                    signInWithPhoneAuthCredential(auth, credential, onAuthSuccess) { err ->
                        isLoading = false
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isLoading = false
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.auth_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = { isStaffLogin = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = if (!isStaffLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text("Owner Login", fontWeight = if (!isStaffLogin) FontWeight.Bold else FontWeight.Normal) }
                TextButton(
                    onClick = { isStaffLogin = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = if (isStaffLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text("Staff Login", fontWeight = if (isStaffLogin) FontWeight.Bold else FontWeight.Normal) }
            }

            if (!isStaffLogin) {
                Text(
                    text =
                        if (isOtpSent) {
                            stringResource(
                                R.string.auth_enter_otp,
                            )
                        } else {
                            stringResource(R.string.auth_enter_phone)
                        },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

            Text(
                text =
                    if (isOtpSent) {
                        stringResource(
                            R.string.auth_otp_sent_to,
                            phoneNumber,
                        )
                    } else {
                        stringResource(R.string.auth_desc)
                    },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp),
            )

            if (!isOtpSent) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(stringResource(R.string.auth_phone_label)) },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
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
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.auth_err_invalid_phone),
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(R.string.auth_btn_send_otp), fontSize = 16.sp)
                    }
                }
            } else {
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text(stringResource(R.string.auth_otp_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (otpCode.length == 6) {
                            isLoading = true
                            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                            signInWithPhoneAuthCredential(auth, credential, onAuthSuccess) { err ->
                                isLoading = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.auth_err_invalid_otp),
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(R.string.auth_btn_verify), fontSize = 16.sp)
                    }
                }
            } } else {
                Text(
                    text = "Staff Login",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = "Enter your username and password",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 32.dp),
                )
                OutlinedTextField(
                    value = staffUsername,
                    onValueChange = { staffUsername = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = staffPassword,
                    onValueChange = { staffPassword = it },
                    label = { Text("Password (Pin)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
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
                                                    val prefs = appContext.getSharedPreferences("storebook_prefs", android.content.Context.MODE_PRIVATE)
                                                    prefs.edit().putString("user_role", role).putString("active_store_id", storeId).apply()
                                                    
                                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                                        try {
                                                            com.storebook.inventoryapp.data.sync.FirestoreSyncManager(appContext).syncAllData()
                                                        } catch (e: Exception) {
                                                            android.util.Log.e("AuthScreen", "Initial staff sync failed", e)
                                                        }
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
                                        Toast.makeText(context, task.exception?.message ?: "Login failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Log in as Staff", fontSize = 16.sp)
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
                            val prefs = appContext.getSharedPreferences("storebook_prefs", android.content.Context.MODE_PRIVATE)
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
                                    com.storebook.inventoryapp.data.sync.FirestoreSyncManager(appContext).syncAllData()
                                } catch (e: Exception) {
                                    android.util.Log.e("AuthScreen", "Initial owner sync failed", e)
                                }
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
