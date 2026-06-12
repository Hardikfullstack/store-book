package com.storebook.inventoryapp.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class AppUpdateHelper(
    private val context: Context,
) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var installStateListener: InstallStateUpdatedListener? = null

    interface UpdateStatusListener {
        fun onUpdateAvailable(appUpdateInfo: AppUpdateInfo)

        fun onUpdateNotAvailable()

        fun onUpdateFailed(e: Exception)

        fun onFlexibleUpdateDownloaded()
    }

    fun checkForUpdate(listener: UpdateStatusListener) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    listener.onUpdateAvailable(appUpdateInfo)
                } else {
                    listener.onUpdateNotAvailable()
                }
            }.addOnFailureListener { exception ->
                listener.onUpdateFailed(exception)
            }
    }

    fun startUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        updateType: Int,
        requestCode: Int,
    ) {
        if (updateType == AppUpdateType.FLEXIBLE) {
            registerInstallStateListener()
        }
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateType,
                activity,
                requestCode,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerInstallStateListener() {
        if (installStateListener == null) {
            installStateListener =
                InstallStateUpdatedListener { state ->
                    if (state.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackBarForCompleteUpdate()
                    }
                }
            appUpdateManager.registerListener(installStateListener!!)
        }
    }

    fun unregisterListener() {
        installStateListener?.let {
            appUpdateManager.unregisterListener(it)
            installStateListener = null
        }
    }

    fun checkPendingUpdate(
        activity: Activity,
        requestCode: Int,
    ) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        requestCode,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate()
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Toast.makeText(context, "An update has just been downloaded. Restarting app...", Toast.LENGTH_LONG).show()
        appUpdateManager.completeUpdate()
    }
}
