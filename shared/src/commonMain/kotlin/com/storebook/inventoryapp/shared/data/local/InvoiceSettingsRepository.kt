package com.storebook.inventoryapp.shared.data.local

import com.storebook.inventoryapp.shared.domain.models.InvoiceSettings

class InvoiceSettingsRepository(
    private val database: StoreBookDatabase,
) {
    private val queries = database.storeBookQueries

    fun getInvoiceSettings(storeId: String): InvoiceSettings? {
        val row = queries.getInvoiceSettingsByStoreId(storeId).executeAsOneOrNull() ?: return null
        return mapToDomain(row)
    }

    suspend fun setInvoiceSettings(settings: InvoiceSettings) {
        val updatedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val combinedBankDetails = combineBankAndUpi(settings.bankDetails, settings.upiId)
        queries.insertOrUpdateInvoiceSettings(
            storeId = settings.storeId,
            shopName = settings.shopName,
            shopAddress = settings.shopAddress,
            shopGstin = settings.shopGstin,
            logoPath = settings.logoPath,
            accentColor = settings.accentColor,
            footerText = settings.footerText,
            bankDetails = combinedBankDetails,
            terms = settings.terms,
            updatedAt = updatedAt,
        )
    }

    fun getUnsyncedInvoiceSettings(): List<Invoice_settings> {
        return queries.getUnsyncedInvoiceSettings().executeAsList()
    }

    fun markSynced(localId: Long, cloudId: String) {
        queries.markInvoiceSettingsSynced(cloudId = cloudId, id = localId)
    }

    private fun mapToDomain(row: Invoice_settings): InvoiceSettings {
        val (cleanBank, extractedUpi) = parseBankAndUpi(row.bank_details)
        return InvoiceSettings(
            id = row.id,
            storeId = row.store_id,
            shopName = row.shop_name,
            shopAddress = row.shop_address,
            shopGstin = row.shop_gstin,
            logoPath = row.logo_path,
            accentColor = row.accent_color,
            footerText = row.footer_text,
            bankDetails = cleanBank,
            terms = row.terms,
            upiId = extractedUpi,
        )
    }

    private fun parseBankAndUpi(raw: String?): Pair<String?, String?> {
        if (raw.isNullOrBlank()) return null to null
        val lines = raw.lines()
        val upiLine = lines.firstOrNull { line ->
            val trimmed = line.trim()
            trimmed.startsWith("UPI ID:", ignoreCase = true) || trimmed.startsWith("UPI:", ignoreCase = true)
        }
        val extractedUpi = upiLine?.substringAfter(":")?.trim()?.takeIf { it.isNotBlank() }
        val remainingBank = lines.filter { line ->
            val trimmed = line.trim()
            !trimmed.startsWith("UPI ID:", ignoreCase = true) && !trimmed.startsWith("UPI:", ignoreCase = true)
        }.joinToString("\n").trim().takeIf { it.isNotBlank() }
        return remainingBank to extractedUpi
    }

    private fun combineBankAndUpi(bank: String?, upi: String?): String? {
        val cleanBank = bank?.trim()?.takeIf { it.isNotBlank() }
        val cleanUpi = upi?.trim()?.takeIf { it.isNotBlank() }
        return buildString {
            if (cleanBank != null) append(cleanBank)
            if (cleanUpi != null) {
                if (isNotEmpty()) append("\n")
                append("UPI ID: $cleanUpi")
            }
        }.takeIf { it.isNotBlank() }
    }
}
