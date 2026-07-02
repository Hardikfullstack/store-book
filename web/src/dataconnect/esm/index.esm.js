import { queryRef, executeQuery, mutationRef, executeMutation, validateArgs } from 'firebase/data-connect';

export const connectorConfig = {
  connector: 'storebook-connector',
  service: 'store-book',
  location: 'us-central1'
};

export function syncItemRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncItem', inputVars);
}

export function syncItem(dcOrVars, vars) {
  return executeMutation(syncItemRef(dcOrVars, vars));
}

export function syncSaleRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSale', inputVars);
}

export function syncSale(dcOrVars, vars) {
  return executeMutation(syncSaleRef(dcOrVars, vars));
}

export function syncSaleItemRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSaleItem', inputVars);
}

export function syncSaleItem(dcOrVars, vars) {
  return executeMutation(syncSaleItemRef(dcOrVars, vars));
}

export function softDeleteItemRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteItem', inputVars);
}

export function softDeleteItem(dcOrVars, vars) {
  return executeMutation(softDeleteItemRef(dcOrVars, vars));
}

export function softDeleteSaleRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteSale', inputVars);
}

export function softDeleteSale(dcOrVars, vars) {
  return executeMutation(softDeleteSaleRef(dcOrVars, vars));
}

export function syncUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncUser', inputVars);
}

export function syncUser(dcOrVars, vars) {
  return executeMutation(syncUserRef(dcOrVars, vars));
}

export function updateUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpdateUser', inputVars);
}

export function updateUser(dcOrVars, vars) {
  return executeMutation(updateUserRef(dcOrVars, vars));
}

export function syncStoreRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncStore', inputVars);
}

export function syncStore(dcOrVars, vars) {
  return executeMutation(syncStoreRef(dcOrVars, vars));
}

export function updateStoreRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpdateStore', inputVars);
}

export function updateStore(dcOrVars, vars) {
  return executeMutation(updateStoreRef(dcOrVars, vars));
}

export function syncUdhaarRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncUdhaar', inputVars);
}

export function syncUdhaar(dcOrVars, vars) {
  return executeMutation(syncUdhaarRef(dcOrVars, vars));
}

export function softDeleteUdhaarRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteUdhaar', inputVars);
}

export function softDeleteUdhaar(dcOrVars, vars) {
  return executeMutation(softDeleteUdhaarRef(dcOrVars, vars));
}

export function syncExpenseRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncExpense', inputVars);
}

export function syncExpense(dcOrVars, vars) {
  return executeMutation(syncExpenseRef(dcOrVars, vars));
}

export function softDeleteExpenseRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteExpense', inputVars);
}

export function softDeleteExpense(dcOrVars, vars) {
  return executeMutation(softDeleteExpenseRef(dcOrVars, vars));
}

export function syncSupplierRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSupplier', inputVars);
}

export function syncSupplier(dcOrVars, vars) {
  return executeMutation(syncSupplierRef(dcOrVars, vars));
}

export function upsertGlobalSettingRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertGlobalSetting', inputVars);
}

export function upsertGlobalSetting(dcOrVars, vars) {
  return executeMutation(upsertGlobalSettingRef(dcOrVars, vars));
}

export function createAdminAuditLogRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'CreateAdminAuditLog', inputVars);
}

export function createAdminAuditLog(dcOrVars, vars) {
  return executeMutation(createAdminAuditLogRef(dcOrVars, vars));
}

export function upsertAnnouncementRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertAnnouncement', inputVars);
}

export function upsertAnnouncement(dcOrVars, vars) {
  return executeMutation(upsertAnnouncementRef(dcOrVars, vars));
}

export function deleteAnnouncementRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'DeleteAnnouncement', inputVars);
}

export function deleteAnnouncement(dcOrVars, vars) {
  return executeMutation(deleteAnnouncementRef(dcOrVars, vars));
}

export function upsertPromoCodeRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertPromoCode', inputVars);
}

export function upsertPromoCode(dcOrVars, vars) {
  return executeMutation(upsertPromoCodeRef(dcOrVars, vars));
}

export function deletePromoCodeRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'DeletePromoCode', inputVars);
}

export function deletePromoCode(dcOrVars, vars) {
  return executeMutation(deletePromoCodeRef(dcOrVars, vars));
}

export function toggleStoreStatusRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'ToggleStoreStatus', inputVars);
}

export function toggleStoreStatus(dcOrVars, vars) {
  return executeMutation(toggleStoreStatusRef(dcOrVars, vars));
}

export function purgeStoreRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'PurgeStore', inputVars);
}

export function purgeStore(dcOrVars, vars) {
  return executeMutation(purgeStoreRef(dcOrVars, vars));
}

export function createUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'CreateUser', inputVars);
}

export function createUser(dcOrVars, vars) {
  return executeMutation(createUserRef(dcOrVars, vars));
}

export function syncPurchaseRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncPurchase', inputVars);
}

export function syncPurchase(dcOrVars, vars) {
  return executeMutation(syncPurchaseRef(dcOrVars, vars));
}

export function syncPurchaseItemRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncPurchaseItem', inputVars);
}

export function syncPurchaseItem(dcOrVars, vars) {
  return executeMutation(syncPurchaseItemRef(dcOrVars, vars));
}

export function syncItemBatchRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncItemBatch', inputVars);
}

export function syncItemBatch(dcOrVars, vars) {
  return executeMutation(syncItemBatchRef(dcOrVars, vars));
}

export function syncItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncItems', inputVars);
}

export function syncItems(dcOrVars, vars) {
  return executeQuery(syncItemsRef(dcOrVars, vars));
}

export function syncSalesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSales', inputVars);
}

export function syncSales(dcOrVars, vars) {
  return executeQuery(syncSalesRef(dcOrVars, vars));
}

export function syncSaleItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSaleItems', inputVars);
}

export function syncSaleItems(dcOrVars, vars) {
  return executeQuery(syncSaleItemsRef(dcOrVars, vars));
}

export function getActiveItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveItems', inputVars);
}

export function getActiveItems(dcOrVars, vars) {
  return executeQuery(getActiveItemsRef(dcOrVars, vars));
}

export function getItemsCountRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetItemsCount', inputVars);
}

export function getItemsCount(dcOrVars, vars) {
  return executeQuery(getItemsCountRef(dcOrVars, vars));
}

export function syncUdhaarsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncUdhaars', inputVars);
}

export function syncUdhaars(dcOrVars, vars) {
  return executeQuery(syncUdhaarsRef(dcOrVars, vars));
}

export function syncExpensesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncExpenses', inputVars);
}

export function syncExpenses(dcOrVars, vars) {
  return executeQuery(syncExpensesRef(dcOrVars, vars));
}

export function getActiveSalesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSales', inputVars);
}

export function getActiveSales(dcOrVars, vars) {
  return executeQuery(getActiveSalesRef(dcOrVars, vars));
}

export function getActiveSaleItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSaleItems', inputVars);
}

export function getActiveSaleItems(dcOrVars, vars) {
  return executeQuery(getActiveSaleItemsRef(dcOrVars, vars));
}

export function getActiveUdhaarsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveUdhaars', inputVars);
}

export function getActiveUdhaars(dcOrVars, vars) {
  return executeQuery(getActiveUdhaarsRef(dcOrVars, vars));
}

export function getActiveExpensesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveExpenses', inputVars);
}

export function getActiveExpenses(dcOrVars, vars) {
  return executeQuery(getActiveExpensesRef(dcOrVars, vars));
}

export function getActiveSuppliersRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSuppliers', inputVars);
}

export function getActiveSuppliers(dcOrVars, vars) {
  return executeQuery(getActiveSuppliersRef(dcOrVars, vars));
}

export function getUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUser', inputVars);
}

export function getUser(dcOrVars, vars) {
  return executeQuery(getUserRef(dcOrVars, vars));
}

export function getStoresPaginatedRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStoresPaginated');
}

export function getStoresPaginated(dc) {
  return executeQuery(getStoresPaginatedRef(dc));
}

export function getUsersPaginatedRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUsersPaginated');
}

export function getUsersPaginated(dc) {
  return executeQuery(getUsersPaginatedRef(dc));
}

export function getGlobalSettingsRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetGlobalSettings');
}

export function getGlobalSettings(dc) {
  return executeQuery(getGlobalSettingsRef(dc));
}

export function getAdminAuditLogsRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetAdminAuditLogs');
}

export function getAdminAuditLogs(dc) {
  return executeQuery(getAdminAuditLogsRef(dc));
}

export function getAnnouncementsRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetAnnouncements');
}

export function getAnnouncements(dc) {
  return executeQuery(getAnnouncementsRef(dc));
}

export function getPromoCodesRef(dc) {
  const { dc: dcInstance} = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetPromoCodes');
}

export function getPromoCodes(dc) {
  return executeQuery(getPromoCodesRef(dc));
}

export function syncSuppliersRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSuppliers', inputVars);
}

export function syncSuppliers(dcOrVars, vars) {
  return executeQuery(syncSuppliersRef(dcOrVars, vars));
}

export function syncPurchasesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncPurchases', inputVars);
}

export function syncPurchases(dcOrVars, vars) {
  return executeQuery(syncPurchasesRef(dcOrVars, vars));
}

export function syncPurchaseItemsRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncPurchaseItems', inputVars);
}

export function syncPurchaseItems(dcOrVars, vars) {
  return executeQuery(syncPurchaseItemsRef(dcOrVars, vars));
}

export function syncItemBatchesRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncItemBatches', inputVars);
}

export function syncItemBatches(dcOrVars, vars) {
  return executeQuery(syncItemBatchesRef(dcOrVars, vars));
}

