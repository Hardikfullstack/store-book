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
        queries.insertOrUpdateInvoiceSettings(
            storeId = settings.storeId,
            shopName = settings.shopName,
            shopAddress = settings.shopAddress,
            shopGstin = settings.shopGstin,
            logoPath = settings.logoPath,
            accentColor = settings.accentColor,
            headerText = settings.headerText,
            footerText = settings.footerText,
            bankDetails = settings.bankDetails,
            terms = settings.terms,
            showGstBreakdown = if (settings.showGstBreakdown) 1L else 0L,
            templateStyle = settings.templateStyle,
            updatedAt = updatedAt,
        )
    }

    fun getUnsyncedInvoiceSettings(): List<Invoice_settings> {
        return queries.getUnsyncedInvoiceSettings().executeAsList()
    }

    fun markSynced(localId: Long, cloudId: String) {
        queries.markInvoiceSettingsSynced(cloudId = cloudId, id = localId)
    }

    private fun mapToDomain(row: Invoice_settings): InvoiceSettings =
        InvoiceSettings(
            id = row.id,
            storeId = row.store_id,
            shopName = row.shop_name,
            shopAddress = row.shop_address,
            shopGstin = row.shop_gstin,
            logoPath = row.logo_path,
            accentColor = row.accent_color,
            headerText = row.header_text,
            footerText = row.footer_text,
            bankDetails = row.bank_details,
            terms = row.terms,
            showGstBreakdown = row.show_gst_breakdown == 1L,
            templateStyle = row.template_style,
        )
}
