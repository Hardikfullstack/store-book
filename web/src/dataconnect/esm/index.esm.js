import { queryRef, executeQuery, validateArgsWithOptions, mutationRef, executeMutation, validateArgs } from 'firebase/data-connect';

export const OrderDirection = {
  ASC: "ASC",
  DESC: "DESC",
}

export const connectorConfig = {
  connector: 'storebook-connector',
  service: 'store-book',
  location: 'us-central1'
};
export const syncItemRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncItem', inputVars);
}
syncItemRef.operationName = 'SyncItem';

export function syncItem(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncItemRef(dcInstance, inputVars));
}

export const syncSaleRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSale', inputVars);
}
syncSaleRef.operationName = 'SyncSale';

export function syncSale(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncSaleRef(dcInstance, inputVars));
}

export const syncSaleItemRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSaleItem', inputVars);
}
syncSaleItemRef.operationName = 'SyncSaleItem';

export function syncSaleItem(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncSaleItemRef(dcInstance, inputVars));
}

export const softDeleteItemRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteItem', inputVars);
}
softDeleteItemRef.operationName = 'SoftDeleteItem';

export function softDeleteItem(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(softDeleteItemRef(dcInstance, inputVars));
}

export const softDeleteSaleRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteSale', inputVars);
}
softDeleteSaleRef.operationName = 'SoftDeleteSale';

export function softDeleteSale(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(softDeleteSaleRef(dcInstance, inputVars));
}

export const syncUserRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncUser', inputVars);
}
syncUserRef.operationName = 'SyncUser';

export function syncUser(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncUserRef(dcInstance, inputVars));
}

export const updateUserRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpdateUser', inputVars);
}
updateUserRef.operationName = 'UpdateUser';

export function updateUser(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(updateUserRef(dcInstance, inputVars));
}

export const syncStoreRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncStore', inputVars);
}
syncStoreRef.operationName = 'SyncStore';

export function syncStore(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncStoreRef(dcInstance, inputVars));
}

export const updateStoreRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpdateStore', inputVars);
}
updateStoreRef.operationName = 'UpdateStore';

export function updateStore(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(updateStoreRef(dcInstance, inputVars));
}

export const syncUdhaarRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncUdhaar', inputVars);
}
syncUdhaarRef.operationName = 'SyncUdhaar';

export function syncUdhaar(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncUdhaarRef(dcInstance, inputVars));
}

export const softDeleteUdhaarRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteUdhaar', inputVars);
}
softDeleteUdhaarRef.operationName = 'SoftDeleteUdhaar';

export function softDeleteUdhaar(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(softDeleteUdhaarRef(dcInstance, inputVars));
}

export const syncExpenseRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncExpense', inputVars);
}
syncExpenseRef.operationName = 'SyncExpense';

export function syncExpense(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncExpenseRef(dcInstance, inputVars));
}

export const softDeleteExpenseRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteExpense', inputVars);
}
softDeleteExpenseRef.operationName = 'SoftDeleteExpense';

export function softDeleteExpense(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(softDeleteExpenseRef(dcInstance, inputVars));
}

export const syncSupplierRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSupplier', inputVars);
}
syncSupplierRef.operationName = 'SyncSupplier';

export function syncSupplier(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncSupplierRef(dcInstance, inputVars));
}

export const upsertGlobalSettingRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertGlobalSetting', inputVars);
}
upsertGlobalSettingRef.operationName = 'UpsertGlobalSetting';

export function upsertGlobalSetting(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(upsertGlobalSettingRef(dcInstance, inputVars));
}

export const createAdminAuditLogRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'CreateAdminAuditLog', inputVars);
}
createAdminAuditLogRef.operationName = 'CreateAdminAuditLog';

export function createAdminAuditLog(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(createAdminAuditLogRef(dcInstance, inputVars));
}

export const upsertAnnouncementRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertAnnouncement', inputVars);
}
upsertAnnouncementRef.operationName = 'UpsertAnnouncement';

export function upsertAnnouncement(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(upsertAnnouncementRef(dcInstance, inputVars));
}

export const deleteAnnouncementRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'DeleteAnnouncement', inputVars);
}
deleteAnnouncementRef.operationName = 'DeleteAnnouncement';

export function deleteAnnouncement(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(deleteAnnouncementRef(dcInstance, inputVars));
}

export const upsertPromoCodeRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertPromoCode', inputVars);
}
upsertPromoCodeRef.operationName = 'UpsertPromoCode';

export function upsertPromoCode(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(upsertPromoCodeRef(dcInstance, inputVars));
}

export const deletePromoCodeRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'DeletePromoCode', inputVars);
}
deletePromoCodeRef.operationName = 'DeletePromoCode';

export function deletePromoCode(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(deletePromoCodeRef(dcInstance, inputVars));
}

export const toggleStoreStatusRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'ToggleStoreStatus', inputVars);
}
toggleStoreStatusRef.operationName = 'ToggleStoreStatus';

export function toggleStoreStatus(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(toggleStoreStatusRef(dcInstance, inputVars));
}

export const purgeStoreRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'PurgeStore', inputVars);
}
purgeStoreRef.operationName = 'PurgeStore';

export function purgeStore(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(purgeStoreRef(dcInstance, inputVars));
}

export const createUserRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'CreateUser', inputVars);
}
createUserRef.operationName = 'CreateUser';

export function createUser(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(createUserRef(dcInstance, inputVars));
}

export const syncPurchaseRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncPurchase', inputVars);
}
syncPurchaseRef.operationName = 'SyncPurchase';

export function syncPurchase(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncPurchaseRef(dcInstance, inputVars));
}

export const syncPurchaseItemRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncPurchaseItem', inputVars);
}
syncPurchaseItemRef.operationName = 'SyncPurchaseItem';

export function syncPurchaseItem(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncPurchaseItemRef(dcInstance, inputVars));
}

export const syncItemBatchRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncItemBatch', inputVars);
}
syncItemBatchRef.operationName = 'SyncItemBatch';

export function syncItemBatch(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncItemBatchRef(dcInstance, inputVars));
}

export const syncStockAdjustmentRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncStockAdjustment', inputVars);
}
syncStockAdjustmentRef.operationName = 'SyncStockAdjustment';

export function syncStockAdjustment(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncStockAdjustmentRef(dcInstance, inputVars));
}

export const syncBusinessProfileRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncBusinessProfile', inputVars);
}
syncBusinessProfileRef.operationName = 'SyncBusinessProfile';

export function syncBusinessProfile(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncBusinessProfileRef(dcInstance, inputVars));
}

export const getBusinessProfileRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetBusinessProfile', inputVars);
}
getBusinessProfileRef.operationName = 'GetBusinessProfile';

export function getBusinessProfile(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getBusinessProfileRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncItems', inputVars);
}
syncItemsRef.operationName = 'SyncItems';

export function syncItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncSalesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSales', inputVars);
}
syncSalesRef.operationName = 'SyncSales';

export function syncSales(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncSalesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncSaleItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSaleItems', inputVars);
}
syncSaleItemsRef.operationName = 'SyncSaleItems';

export function syncSaleItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncSaleItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getActiveItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveItems', inputVars);
}
getActiveItemsRef.operationName = 'GetActiveItems';

export function getActiveItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncUdhaarsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncUdhaars', inputVars);
}
syncUdhaarsRef.operationName = 'SyncUdhaars';

export function syncUdhaars(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncUdhaarsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncExpensesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncExpenses', inputVars);
}
syncExpensesRef.operationName = 'SyncExpenses';

export function syncExpenses(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncExpensesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getActiveSalesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSales', inputVars);
}
getActiveSalesRef.operationName = 'GetActiveSales';

export function getActiveSales(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveSalesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getActiveSaleItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSaleItems', inputVars);
}
getActiveSaleItemsRef.operationName = 'GetActiveSaleItems';

export function getActiveSaleItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveSaleItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getActiveUdhaarsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveUdhaars', inputVars);
}
getActiveUdhaarsRef.operationName = 'GetActiveUdhaars';

export function getActiveUdhaars(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveUdhaarsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getActiveExpensesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveExpenses', inputVars);
}
getActiveExpensesRef.operationName = 'GetActiveExpenses';

export function getActiveExpenses(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveExpensesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getActiveSuppliersRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSuppliers', inputVars);
}
getActiveSuppliersRef.operationName = 'GetActiveSuppliers';

export function getActiveSuppliers(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveSuppliersRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getUserRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUser', inputVars);
}
getUserRef.operationName = 'GetUser';

export function getUser(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getUserRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getStoresPaginatedRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStoresPaginated');
}
getStoresPaginatedRef.operationName = 'GetStoresPaginated';

export function getStoresPaginated(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getStoresPaginatedRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getUsersPaginatedRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUsersPaginated');
}
getUsersPaginatedRef.operationName = 'GetUsersPaginated';

export function getUsersPaginated(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getUsersPaginatedRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getGlobalSettingsRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetGlobalSettings');
}
getGlobalSettingsRef.operationName = 'GetGlobalSettings';

export function getGlobalSettings(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getGlobalSettingsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getAdminAuditLogsRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetAdminAuditLogs');
}
getAdminAuditLogsRef.operationName = 'GetAdminAuditLogs';

export function getAdminAuditLogs(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getAdminAuditLogsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getAnnouncementsRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetAnnouncements');
}
getAnnouncementsRef.operationName = 'GetAnnouncements';

export function getAnnouncements(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getAnnouncementsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getPromoCodesRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetPromoCodes');
}
getPromoCodesRef.operationName = 'GetPromoCodes';

export function getPromoCodes(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getPromoCodesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncSuppliersRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSuppliers', inputVars);
}
syncSuppliersRef.operationName = 'SyncSuppliers';

export function syncSuppliers(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncSuppliersRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncPurchasesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncPurchases', inputVars);
}
syncPurchasesRef.operationName = 'SyncPurchases';

export function syncPurchases(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncPurchasesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncPurchaseItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncPurchaseItems', inputVars);
}
syncPurchaseItemsRef.operationName = 'SyncPurchaseItems';

export function syncPurchaseItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncPurchaseItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const syncItemBatchesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncItemBatches', inputVars);
}
syncItemBatchesRef.operationName = 'SyncItemBatches';

export function syncItemBatches(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncItemBatchesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getItemsCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetItemsCount', inputVars);
}
getItemsCountRef.operationName = 'GetItemsCount';

export function getItemsCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getItemsCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getSalesCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetSalesCount', inputVars);
}
getSalesCountRef.operationName = 'GetSalesCount';

export function getSalesCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getSalesCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getUdhaarEntriesCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUdhaarEntriesCount', inputVars);
}
getUdhaarEntriesCountRef.operationName = 'GetUdhaarEntriesCount';

export function getUdhaarEntriesCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getUdhaarEntriesCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getExpenseEntriesCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetExpenseEntriesCount', inputVars);
}
getExpenseEntriesCountRef.operationName = 'GetExpenseEntriesCount';

export function getExpenseEntriesCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getExpenseEntriesCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getStoreRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStore', inputVars);
}
getStoreRef.operationName = 'GetStore';

export function getStore(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getStoreRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getStockAdjustmentsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStockAdjustments', inputVars);
}
getStockAdjustmentsRef.operationName = 'GetStockAdjustments';

export function getStockAdjustments(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getStockAdjustmentsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getStockAdjustmentsCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStockAdjustmentsCount', inputVars);
}
getStockAdjustmentsCountRef.operationName = 'GetStockAdjustmentsCount';

export function getStockAdjustmentsCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getStockAdjustmentsCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const listPurchasesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'ListPurchases', inputVars);
}
listPurchasesRef.operationName = 'ListPurchases';

export function listPurchases(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(listPurchasesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getPurchasesCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetPurchasesCount', inputVars);
}
getPurchasesCountRef.operationName = 'GetPurchasesCount';

export function getPurchasesCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getPurchasesCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

export const getPurchaseDetailsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetPurchaseDetails', inputVars);
}
getPurchaseDetailsRef.operationName = 'GetPurchaseDetails';

export function getPurchaseDetails(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getPurchaseDetailsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}

