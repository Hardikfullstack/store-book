package com.storebook.inventoryapp.ui.viewmodels

enum class UserRole {
    OWNER,
    MANAGER,
    STAFF,
    ;

    companion object {
        fun fromString(role: String): UserRole =
            try {
                valueOf(role.uppercase())
            } catch (e: Exception) {
                STAFF
            }
    }
}

enum class AppPermission {
    VIEW_FINANCIALS,
    MANAGE_INVENTORY,
    VIEW_REPORTS,
    MANAGE_STAFF,
    EDIT_SETTINGS,
    MANAGE_PREMIUM,
    MANAGE_BUSINESS_SETTINGS,
}

fun UserRole.hasPermission(permission: AppPermission): Boolean =
    when (this) {
        UserRole.OWNER -> true
        UserRole.MANAGER ->
            permission != AppPermission.MANAGE_STAFF &&
                permission != AppPermission.EDIT_SETTINGS &&
                permission != AppPermission.MANAGE_BUSINESS_SETTINGS &&
                permission != AppPermission.MANAGE_PREMIUM
        UserRole.STAFF -> permission == AppPermission.MANAGE_INVENTORY || permission == AppPermission.VIEW_FINANCIALS
    }
