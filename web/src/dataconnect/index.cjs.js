const { queryRef, executeQuery, mutationRef, executeMutation, validateArgs } = require('firebase/data-connect');

const connectorConfig = {
  connector: 'storebook-connector',
  service: 'store-book',
  location: 'us-central1'
};
exports.connectorConfig = connectorConfig;

exports.syncItemRef = function syncItemRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncItem', inputVars);
}
exports.syncItem = function syncItem(dcOrVars, vars) {
  return executeMutation(syncItemRef(dcOrVars, vars));
};
exports.syncSaleRef = function syncSaleRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSale', inputVars);
}
exports.syncSale = function syncSale(dcOrVars, vars) {
  return executeMutation(syncSaleRef(dcOrVars, vars));
};
exports.syncSaleItemRef = function syncSaleItemRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSaleItem', inputVars);
}
exports.syncSaleItem = function syncSaleItem(dcOrVars, vars) {
  return executeMutation(syncSaleItemRef(dcOrVars, vars));
};
exports.softDeleteItemRef = function softDeleteItemRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteItem', inputVars);
}
exports.softDeleteItem = function softDeleteItem(dcOrVars, vars) {
  return executeMutation(softDeleteItemRef(dcOrVars, vars));
};
exports.softDeleteSaleRef = function softDeleteSaleRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteSale', inputVars);
}
exports.softDeleteSale = function softDeleteSale(dcOrVars, vars) {
  return executeMutation(softDeleteSaleRef(dcOrVars, vars));
};
exports.syncUserRef = function syncUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncUser', inputVars);
}
exports.syncUser = function syncUser(dcOrVars, vars) {
  return executeMutation(syncUserRef(dcOrVars, vars));
};
exports.updateUserRef = function updateUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpdateUser', inputVars);
}
exports.updateUser = function updateUser(dcOrVars, vars) {
  return executeMutation(updateUserRef(dcOrVars, vars));
};
exports.syncStoreRef = function syncStoreRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncStore', inputVars);
}
exports.syncStore = function syncStore(dcOrVars, vars) {
  return executeMutation(syncStoreRef(dcOrVars, vars));
};
exports.updateStoreRef = function updateStoreRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpdateStore', inputVars);
}
exports.updateStore = function updateStore(dcOrVars, vars) {
  return executeMutation(updateStoreRef(dcOrVars, vars));
};
exports.syncUdhaarRef = function syncUdhaarRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncUdhaar', inputVars);
}
exports.syncUdhaar = function syncUdhaar(dcOrVars, vars) {
  return executeMutation(syncUdhaarRef(dcOrVars, vars));
};
exports.softDeleteUdhaarRef = function softDeleteUdhaarRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteUdhaar', inputVars);
}
exports.softDeleteUdhaar = function softDeleteUdhaar(dcOrVars, vars) {
  return executeMutation(softDeleteUdhaarRef(dcOrVars, vars));
};
exports.syncExpenseRef = function syncExpenseRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncExpense', inputVars);
}
exports.syncExpense = function syncExpense(dcOrVars, vars) {
  return executeMutation(syncExpenseRef(dcOrVars, vars));
};
exports.softDeleteExpenseRef = function softDeleteExpenseRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteExpense', inputVars);
}
exports.softDeleteExpense = function softDeleteExpense(dcOrVars, vars) {
  return executeMutation(softDeleteExpenseRef(dcOrVars, vars));
};
exports.syncSupplierRef = function syncSupplierRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSupplier', inputVars);
}
exports.syncSupplier = function syncSupplier(dcOrVars, vars) {
  return executeMutation(syncSupplierRef(dcOrVars, vars));
};
exports.upsertGlobalSettingRef = function upsertGlobalSettingRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertGlobalSetting', inputVars);
}
exports.upsertGlobalSetting = function upsertGlobalSetting(dcOrVars, vars) {
  return executeMutation(upsertGlobalSettingRef(dcOrVars, vars));
};
exports.createAdminAuditLogRef = function createAdminAuditLogRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'CreateAdminAuditLog', inputVars);
}
exports.createAdminAuditLog = function createAdminAuditLog(dcOrVars, vars) {
  return executeMutation(createAdminAuditLogRef(dcOrVars, vars));
};
exports.upsertAnnouncementRef = function upsertAnnouncementRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertAnnouncement', inputVars);
}
exports.upsertAnnouncement = function upsertAnnouncement(dcOrVars, vars) {
  return executeMutation(upsertAnnouncementRef(dcOrVars, vars));
};
exports.deleteAnnouncementRef = function deleteAnnouncementRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'DeleteAnnouncement', inputVars);
}
exports.deleteAnnouncement = function deleteAnnouncement(dcOrVars, vars) {
  return executeMutation(deleteAnnouncementRef(dcOrVars, vars));
};
exports.upsertPromoCodeRef = function upsertPromoCodeRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertPromoCode', inputVars);
}
exports.upsertPromoCode = function upsertPromoCode(dcOrVars, vars) {
  return executeMutation(upsertPromoCodeRef(dcOrVars, vars));
};
exports.deletePromoCodeRef = function deletePromoCodeRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'DeletePromoCode', inputVars);
}
exports.deletePromoCode = function deletePromoCode(dcOrVars, vars) {
  return executeMutation(deletePromoCodeRef(dcOrVars, vars));
};
exports.toggleStoreStatusRef = function toggleStoreStatusRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'ToggleStoreStatus', inputVars);
}
exports.toggleStoreStatus = function toggleStoreStatus(dcOrVars, vars) {
  return executeMutation(toggleStoreStatusRef(dcOrVars, vars));
};
exports.purgeStoreRef = function purgeStoreRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'PurgeStore', inputVars);
}
exports.purgeStore = function purgeStore(dcOrVars, vars) {
  return executeMutation(purgeStoreRef(dcOrVars, vars));
};
exports.createUserRef = function createUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'CreateUser', inputVars);
}
exports.createUser = function createUser(dcOrVars, vars) {
  return executeMutation(createUserRef(dcOrVars, vars));
};
exports.syncPurchaseRef = function syncPurchaseRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncPurchase', inputVars);
}
exports.syncPurchase = function syncPurchase(dcOrVars, vars) {
  return executeMutation(syncPurchaseRef(dcOrVars, vars));
};
exports.syncPurchaseItemRef = function syncPurchaseItemRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncPurchaseItem', inputVars);
}
exports.syncPurchaseItem = function syncPurchaseItem(dcOrVars, vars) {
  return executeMutation(syncPurchaseItemRef(dcOrVars, vars));
};
exports.syncItemBatchRef = function syncItemBatchRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncItemBatch', inputVars);
}
exports.syncItemBatch = function syncItemBatch(dcOrVars, vars) {
  return executeMutation(syncItemBatchRef(dcOrVars, vars));
};
exports.syncItemsRef = function syncItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncItems', inputVars);
}
exports.syncItems = function syncItems(dcOrVars, vars) {
  return executeQuery(syncItemsRef(dcOrVars, vars));
};
exports.syncSalesRef = function syncSalesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSales', inputVars);
}
exports.syncSales = function syncSales(dcOrVars, vars) {
  return executeQuery(syncSalesRef(dcOrVars, vars));
};
exports.syncSaleItemsRef = function syncSaleItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSaleItems', inputVars);
}
exports.syncSaleItems = function syncSaleItems(dcOrVars, vars) {
  return executeQuery(syncSaleItemsRef(dcOrVars, vars));
};
exports.getActiveItemsRef = function getActiveItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveItems', inputVars);
}
exports.getActiveItems = function getActiveItems(dcOrVars, vars) {
  return executeQuery(getActiveItemsRef(dcOrVars, vars));
};
exports.syncUdhaarsRef = function syncUdhaarsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncUdhaars', inputVars);
}
exports.syncUdhaars = function syncUdhaars(dcOrVars, vars) {
  return executeQuery(syncUdhaarsRef(dcOrVars, vars));
};
exports.syncExpensesRef = function syncExpensesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncExpenses', inputVars);
}
exports.syncExpenses = function syncExpenses(dcOrVars, vars) {
  return executeQuery(syncExpensesRef(dcOrVars, vars));
};
exports.getActiveSalesRef = function getActiveSalesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSales', inputVars);
}
exports.getActiveSales = function getActiveSales(dcOrVars, vars) {
  return executeQuery(getActiveSalesRef(dcOrVars, vars));
};
exports.getActiveSaleItemsRef = function getActiveSaleItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSaleItems', inputVars);
}
exports.getActiveSaleItems = function getActiveSaleItems(dcOrVars, vars) {
  return executeQuery(getActiveSaleItemsRef(dcOrVars, vars));
};
exports.getActiveUdhaarsRef = function getActiveUdhaarsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveUdhaars', inputVars);
}
exports.getActiveUdhaars = function getActiveUdhaars(dcOrVars, vars) {
  return executeQuery(getActiveUdhaarsRef(dcOrVars, vars));
};
exports.getActiveExpensesRef = function getActiveExpensesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveExpenses', inputVars);
}
exports.getActiveExpenses = function getActiveExpenses(dcOrVars, vars) {
  return executeQuery(getActiveExpensesRef(dcOrVars, vars));
};
exports.getActiveSuppliersRef = function getActiveSuppliersRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSuppliers', inputVars);
}
exports.getActiveSuppliers = function getActiveSuppliers(dcOrVars, vars) {
  return executeQuery(getActiveSuppliersRef(dcOrVars, vars));
};
exports.getUserRef = function getUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUser', inputVars);
}
exports.getUser = function getUser(dcOrVars, vars) {
  return executeQuery(getUserRef(dcOrVars, vars));
};
exports.getStoresPaginatedRef = function getStoresPaginatedRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStoresPaginated');
}
exports.getStoresPaginated = function getStoresPaginated(dc) {
  return executeQuery(getStoresPaginatedRef(dc));
};
exports.getUsersPaginatedRef = function getUsersPaginatedRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUsersPaginated');
}
exports.getUsersPaginated = function getUsersPaginated(dc) {
  return executeQuery(getUsersPaginatedRef(dc));
};
exports.getGlobalSettingsRef = function getGlobalSettingsRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetGlobalSettings');
}
exports.getGlobalSettings = function getGlobalSettings(dc) {
  return executeQuery(getGlobalSettingsRef(dc));
};
exports.getAdminAuditLogsRef = function getAdminAuditLogsRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetAdminAuditLogs');
}
exports.getAdminAuditLogs = function getAdminAuditLogs(dc) {
  return executeQuery(getAdminAuditLogsRef(dc));
};
exports.getAnnouncementsRef = function getAnnouncementsRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetAnnouncements');
}
exports.getAnnouncements = function getAnnouncements(dc) {
  return executeQuery(getAnnouncementsRef(dc));
};
exports.getPromoCodesRef = function getPromoCodesRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetPromoCodes');
}
exports.getPromoCodes = function getPromoCodes(dc) {
  return executeQuery(getPromoCodesRef(dc));
};
exports.syncSuppliersRef = function syncSuppliersRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSuppliers', inputVars);
}
exports.syncSuppliers = function syncSuppliers(dcOrVars, vars) {
  return executeQuery(syncSuppliersRef(dcOrVars, vars));
};
exports.syncPurchasesRef = function syncPurchasesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncPurchases', inputVars);
}
exports.syncPurchases = function syncPurchases(dcOrVars, vars) {
  return executeQuery(syncPurchasesRef(dcOrVars, vars));
};
exports.syncPurchaseItemsRef = function syncPurchaseItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncPurchaseItems', inputVars);
}
exports.syncPurchaseItems = function syncPurchaseItems(dcOrVars, vars) {
  return executeQuery(syncPurchaseItemsRef(dcOrVars, vars));
};
exports.syncItemBatchesRef = function syncItemBatchesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncItemBatches', inputVars);
}
exports.syncItemBatches = function syncItemBatches(dcOrVars, vars) {
  return executeQuery(syncItemBatchesRef(dcOrVars, vars));
};
exports.getItemsCountRef = function getItemsCountRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetItemsCount', inputVars);
}
exports.getItemsCount = function getItemsCount(dcOrVars, vars) {
  return executeQuery(getItemsCountRef(dcOrVars, vars));
};
exports.getSalesCountRef = function getSalesCountRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetSalesCount', inputVars);
}
exports.getSalesCount = function getSalesCount(dcOrVars, vars) {
  return executeQuery(getSalesCountRef(dcOrVars, vars));
};
exports.getUdhaarEntriesCountRef = function getUdhaarEntriesCountRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUdhaarEntriesCount', inputVars);
}
exports.getUdhaarEntriesCount = function getUdhaarEntriesCount(dcOrVars, vars) {
  return executeQuery(getUdhaarEntriesCountRef(dcOrVars, vars));
};
exports.getExpenseEntriesCountRef = function getExpenseEntriesCountRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetExpenseEntriesCount', inputVars);
}
exports.getExpenseEntriesCount = function getExpenseEntriesCount(dcOrVars, vars) {
  return executeQuery(getExpenseEntriesCountRef(dcOrVars, vars));
};
