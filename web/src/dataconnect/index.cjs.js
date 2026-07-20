const { queryRef, executeQuery, validateArgsWithOptions, mutationRef, executeMutation, validateArgs } = require('firebase/data-connect');

const OrderDirection = {
  ASC: "ASC",
  DESC: "DESC",
}
exports.OrderDirection = OrderDirection;

const connectorConfig = {
  connector: 'storebook-connector',
  service: 'store-book',
  location: 'us-central1'
};
exports.connectorConfig = connectorConfig;

const syncItemRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncItem', inputVars);
}
syncItemRef.operationName = 'SyncItem';
exports.syncItemRef = syncItemRef;

exports.syncItem = function syncItem(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncItemRef(dcInstance, inputVars));
}
  ;

const syncSaleRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSale', inputVars);
}
syncSaleRef.operationName = 'SyncSale';
exports.syncSaleRef = syncSaleRef;

exports.syncSale = function syncSale(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncSaleRef(dcInstance, inputVars));
}
  ;

const syncSaleItemRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSaleItem', inputVars);
}
syncSaleItemRef.operationName = 'SyncSaleItem';
exports.syncSaleItemRef = syncSaleItemRef;

exports.syncSaleItem = function syncSaleItem(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncSaleItemRef(dcInstance, inputVars));
}
  ;

const softDeleteItemRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteItem', inputVars);
}
softDeleteItemRef.operationName = 'SoftDeleteItem';
exports.softDeleteItemRef = softDeleteItemRef;

exports.softDeleteItem = function softDeleteItem(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(softDeleteItemRef(dcInstance, inputVars));
}
  ;

const softDeleteSaleRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteSale', inputVars);
}
softDeleteSaleRef.operationName = 'SoftDeleteSale';
exports.softDeleteSaleRef = softDeleteSaleRef;

exports.softDeleteSale = function softDeleteSale(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(softDeleteSaleRef(dcInstance, inputVars));
}
  ;

const syncUserRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncUser', inputVars);
}
syncUserRef.operationName = 'SyncUser';
exports.syncUserRef = syncUserRef;

exports.syncUser = function syncUser(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncUserRef(dcInstance, inputVars));
}
  ;

const updateUserRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpdateUser', inputVars);
}
updateUserRef.operationName = 'UpdateUser';
exports.updateUserRef = updateUserRef;

exports.updateUser = function updateUser(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(updateUserRef(dcInstance, inputVars));
}
  ;

const syncStoreRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncStore', inputVars);
}
syncStoreRef.operationName = 'SyncStore';
exports.syncStoreRef = syncStoreRef;

exports.syncStore = function syncStore(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncStoreRef(dcInstance, inputVars));
}
  ;

const updateStoreRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpdateStore', inputVars);
}
updateStoreRef.operationName = 'UpdateStore';
exports.updateStoreRef = updateStoreRef;

exports.updateStore = function updateStore(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(updateStoreRef(dcInstance, inputVars));
}
  ;

const syncUdhaarRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncUdhaar', inputVars);
}
syncUdhaarRef.operationName = 'SyncUdhaar';
exports.syncUdhaarRef = syncUdhaarRef;

exports.syncUdhaar = function syncUdhaar(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncUdhaarRef(dcInstance, inputVars));
}
  ;

const softDeleteUdhaarRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteUdhaar', inputVars);
}
softDeleteUdhaarRef.operationName = 'SoftDeleteUdhaar';
exports.softDeleteUdhaarRef = softDeleteUdhaarRef;

exports.softDeleteUdhaar = function softDeleteUdhaar(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(softDeleteUdhaarRef(dcInstance, inputVars));
}
  ;

const syncExpenseRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncExpense', inputVars);
}
syncExpenseRef.operationName = 'SyncExpense';
exports.syncExpenseRef = syncExpenseRef;

exports.syncExpense = function syncExpense(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncExpenseRef(dcInstance, inputVars));
}
  ;

const softDeleteExpenseRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SoftDeleteExpense', inputVars);
}
softDeleteExpenseRef.operationName = 'SoftDeleteExpense';
exports.softDeleteExpenseRef = softDeleteExpenseRef;

exports.softDeleteExpense = function softDeleteExpense(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(softDeleteExpenseRef(dcInstance, inputVars));
}
  ;

const syncSupplierRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncSupplier', inputVars);
}
syncSupplierRef.operationName = 'SyncSupplier';
exports.syncSupplierRef = syncSupplierRef;

exports.syncSupplier = function syncSupplier(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncSupplierRef(dcInstance, inputVars));
}
  ;

const upsertGlobalSettingRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertGlobalSetting', inputVars);
}
upsertGlobalSettingRef.operationName = 'UpsertGlobalSetting';
exports.upsertGlobalSettingRef = upsertGlobalSettingRef;

exports.upsertGlobalSetting = function upsertGlobalSetting(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(upsertGlobalSettingRef(dcInstance, inputVars));
}
  ;

const createAdminAuditLogRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'CreateAdminAuditLog', inputVars);
}
createAdminAuditLogRef.operationName = 'CreateAdminAuditLog';
exports.createAdminAuditLogRef = createAdminAuditLogRef;

exports.createAdminAuditLog = function createAdminAuditLog(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(createAdminAuditLogRef(dcInstance, inputVars));
}
  ;

const upsertAnnouncementRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertAnnouncement', inputVars);
}
upsertAnnouncementRef.operationName = 'UpsertAnnouncement';
exports.upsertAnnouncementRef = upsertAnnouncementRef;

exports.upsertAnnouncement = function upsertAnnouncement(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(upsertAnnouncementRef(dcInstance, inputVars));
}
  ;

const deleteAnnouncementRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'DeleteAnnouncement', inputVars);
}
deleteAnnouncementRef.operationName = 'DeleteAnnouncement';
exports.deleteAnnouncementRef = deleteAnnouncementRef;

exports.deleteAnnouncement = function deleteAnnouncement(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(deleteAnnouncementRef(dcInstance, inputVars));
}
  ;

const upsertPromoCodeRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'UpsertPromoCode', inputVars);
}
upsertPromoCodeRef.operationName = 'UpsertPromoCode';
exports.upsertPromoCodeRef = upsertPromoCodeRef;

exports.upsertPromoCode = function upsertPromoCode(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(upsertPromoCodeRef(dcInstance, inputVars));
}
  ;

const deletePromoCodeRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'DeletePromoCode', inputVars);
}
deletePromoCodeRef.operationName = 'DeletePromoCode';
exports.deletePromoCodeRef = deletePromoCodeRef;

exports.deletePromoCode = function deletePromoCode(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(deletePromoCodeRef(dcInstance, inputVars));
}
  ;

const toggleStoreStatusRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'ToggleStoreStatus', inputVars);
}
toggleStoreStatusRef.operationName = 'ToggleStoreStatus';
exports.toggleStoreStatusRef = toggleStoreStatusRef;

exports.toggleStoreStatus = function toggleStoreStatus(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(toggleStoreStatusRef(dcInstance, inputVars));
}
  ;

const purgeStoreRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'PurgeStore', inputVars);
}
purgeStoreRef.operationName = 'PurgeStore';
exports.purgeStoreRef = purgeStoreRef;

exports.purgeStore = function purgeStore(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(purgeStoreRef(dcInstance, inputVars));
}
  ;

const createUserRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'CreateUser', inputVars);
}
createUserRef.operationName = 'CreateUser';
exports.createUserRef = createUserRef;

exports.createUser = function createUser(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(createUserRef(dcInstance, inputVars));
}
  ;

const syncPurchaseRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncPurchase', inputVars);
}
syncPurchaseRef.operationName = 'SyncPurchase';
exports.syncPurchaseRef = syncPurchaseRef;

exports.syncPurchase = function syncPurchase(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncPurchaseRef(dcInstance, inputVars));
}
  ;

const syncPurchaseItemRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncPurchaseItem', inputVars);
}
syncPurchaseItemRef.operationName = 'SyncPurchaseItem';
exports.syncPurchaseItemRef = syncPurchaseItemRef;

exports.syncPurchaseItem = function syncPurchaseItem(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncPurchaseItemRef(dcInstance, inputVars));
}
  ;

const syncItemBatchRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncItemBatch', inputVars);
}
syncItemBatchRef.operationName = 'SyncItemBatch';
exports.syncItemBatchRef = syncItemBatchRef;

exports.syncItemBatch = function syncItemBatch(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncItemBatchRef(dcInstance, inputVars));
}
  ;

const syncStockAdjustmentRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return mutationRef(dcInstance, 'SyncStockAdjustment', inputVars);
}
syncStockAdjustmentRef.operationName = 'SyncStockAdjustment';
exports.syncStockAdjustmentRef = syncStockAdjustmentRef;

exports.syncStockAdjustment = function syncStockAdjustment(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  return executeMutation(syncStockAdjustmentRef(dcInstance, inputVars));
}
  ;

const syncItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncItems', inputVars);
}
syncItemsRef.operationName = 'SyncItems';
exports.syncItemsRef = syncItemsRef;

exports.syncItems = function syncItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const syncSalesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSales', inputVars);
}
syncSalesRef.operationName = 'SyncSales';
exports.syncSalesRef = syncSalesRef;

exports.syncSales = function syncSales(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncSalesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const syncSaleItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSaleItems', inputVars);
}
syncSaleItemsRef.operationName = 'SyncSaleItems';
exports.syncSaleItemsRef = syncSaleItemsRef;

exports.syncSaleItems = function syncSaleItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncSaleItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getActiveItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveItems', inputVars);
}
getActiveItemsRef.operationName = 'GetActiveItems';
exports.getActiveItemsRef = getActiveItemsRef;

exports.getActiveItems = function getActiveItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const syncUdhaarsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncUdhaars', inputVars);
}
syncUdhaarsRef.operationName = 'SyncUdhaars';
exports.syncUdhaarsRef = syncUdhaarsRef;

exports.syncUdhaars = function syncUdhaars(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncUdhaarsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const syncExpensesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncExpenses', inputVars);
}
syncExpensesRef.operationName = 'SyncExpenses';
exports.syncExpensesRef = syncExpensesRef;

exports.syncExpenses = function syncExpenses(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncExpensesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getActiveSalesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSales', inputVars);
}
getActiveSalesRef.operationName = 'GetActiveSales';
exports.getActiveSalesRef = getActiveSalesRef;

exports.getActiveSales = function getActiveSales(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveSalesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getActiveSaleItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSaleItems', inputVars);
}
getActiveSaleItemsRef.operationName = 'GetActiveSaleItems';
exports.getActiveSaleItemsRef = getActiveSaleItemsRef;

exports.getActiveSaleItems = function getActiveSaleItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveSaleItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getActiveUdhaarsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveUdhaars', inputVars);
}
getActiveUdhaarsRef.operationName = 'GetActiveUdhaars';
exports.getActiveUdhaarsRef = getActiveUdhaarsRef;

exports.getActiveUdhaars = function getActiveUdhaars(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveUdhaarsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getActiveExpensesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveExpenses', inputVars);
}
getActiveExpensesRef.operationName = 'GetActiveExpenses';
exports.getActiveExpensesRef = getActiveExpensesRef;

exports.getActiveExpenses = function getActiveExpenses(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveExpensesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getActiveSuppliersRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetActiveSuppliers', inputVars);
}
getActiveSuppliersRef.operationName = 'GetActiveSuppliers';
exports.getActiveSuppliersRef = getActiveSuppliersRef;

exports.getActiveSuppliers = function getActiveSuppliers(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getActiveSuppliersRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getUserRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUser', inputVars);
}
getUserRef.operationName = 'GetUser';
exports.getUserRef = getUserRef;

exports.getUser = function getUser(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getUserRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getStoresPaginatedRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStoresPaginated');
}
getStoresPaginatedRef.operationName = 'GetStoresPaginated';
exports.getStoresPaginatedRef = getStoresPaginatedRef;

exports.getStoresPaginated = function getStoresPaginated(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getStoresPaginatedRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getUsersPaginatedRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUsersPaginated');
}
getUsersPaginatedRef.operationName = 'GetUsersPaginated';
exports.getUsersPaginatedRef = getUsersPaginatedRef;

exports.getUsersPaginated = function getUsersPaginated(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getUsersPaginatedRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getGlobalSettingsRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetGlobalSettings');
}
getGlobalSettingsRef.operationName = 'GetGlobalSettings';
exports.getGlobalSettingsRef = getGlobalSettingsRef;

exports.getGlobalSettings = function getGlobalSettings(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getGlobalSettingsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getAdminAuditLogsRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetAdminAuditLogs');
}
getAdminAuditLogsRef.operationName = 'GetAdminAuditLogs';
exports.getAdminAuditLogsRef = getAdminAuditLogsRef;

exports.getAdminAuditLogs = function getAdminAuditLogs(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getAdminAuditLogsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getAnnouncementsRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetAnnouncements');
}
getAnnouncementsRef.operationName = 'GetAnnouncements';
exports.getAnnouncementsRef = getAnnouncementsRef;

exports.getAnnouncements = function getAnnouncements(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getAnnouncementsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getPromoCodesRef = (dc) => {
  const { dc: dcInstance } = validateArgs(connectorConfig, dc, undefined);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetPromoCodes');
}
getPromoCodesRef.operationName = 'GetPromoCodes';
exports.getPromoCodesRef = getPromoCodesRef;

exports.getPromoCodes = function getPromoCodes(dcOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrOptions, options, undefined, false, false);
  return executeQuery(getPromoCodesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const syncSuppliersRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncSuppliers', inputVars);
}
syncSuppliersRef.operationName = 'SyncSuppliers';
exports.syncSuppliersRef = syncSuppliersRef;

exports.syncSuppliers = function syncSuppliers(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncSuppliersRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const syncPurchasesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncPurchases', inputVars);
}
syncPurchasesRef.operationName = 'SyncPurchases';
exports.syncPurchasesRef = syncPurchasesRef;

exports.syncPurchases = function syncPurchases(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncPurchasesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const syncPurchaseItemsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncPurchaseItems', inputVars);
}
syncPurchaseItemsRef.operationName = 'SyncPurchaseItems';
exports.syncPurchaseItemsRef = syncPurchaseItemsRef;

exports.syncPurchaseItems = function syncPurchaseItems(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncPurchaseItemsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const syncItemBatchesRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'SyncItemBatches', inputVars);
}
syncItemBatchesRef.operationName = 'SyncItemBatches';
exports.syncItemBatchesRef = syncItemBatchesRef;

exports.syncItemBatches = function syncItemBatches(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(syncItemBatchesRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getItemsCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetItemsCount', inputVars);
}
getItemsCountRef.operationName = 'GetItemsCount';
exports.getItemsCountRef = getItemsCountRef;

exports.getItemsCount = function getItemsCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getItemsCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getSalesCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetSalesCount', inputVars);
}
getSalesCountRef.operationName = 'GetSalesCount';
exports.getSalesCountRef = getSalesCountRef;

exports.getSalesCount = function getSalesCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getSalesCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getUdhaarEntriesCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUdhaarEntriesCount', inputVars);
}
getUdhaarEntriesCountRef.operationName = 'GetUdhaarEntriesCount';
exports.getUdhaarEntriesCountRef = getUdhaarEntriesCountRef;

exports.getUdhaarEntriesCount = function getUdhaarEntriesCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getUdhaarEntriesCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getExpenseEntriesCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetExpenseEntriesCount', inputVars);
}
getExpenseEntriesCountRef.operationName = 'GetExpenseEntriesCount';
exports.getExpenseEntriesCountRef = getExpenseEntriesCountRef;

exports.getExpenseEntriesCount = function getExpenseEntriesCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getExpenseEntriesCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getStoreRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStore', inputVars);
}
exports.getStore = function getStore(dcOrVars, vars) {
  return executeQuery(getStoreRef(dcOrVars, vars));
};

const getStockAdjustmentsRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStockAdjustments', inputVars);
}
getStockAdjustmentsRef.operationName = 'GetStockAdjustments';
exports.getStockAdjustmentsRef = getStockAdjustmentsRef;

exports.getStockAdjustments = function getStockAdjustments(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getStockAdjustmentsRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;

const getStockAdjustmentsCountRef = (dcOrVars, vars) => {
  const { dc: dcInstance, vars: inputVars } = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetStockAdjustmentsCount', inputVars);
}
getStockAdjustmentsCountRef.operationName = 'GetStockAdjustmentsCount';
exports.getStockAdjustmentsCountRef = getStockAdjustmentsCountRef;

exports.getStockAdjustmentsCount = function getStockAdjustmentsCount(dcOrVars, varsOrOptions, options) {

  const { dc: dcInstance, vars: inputVars, options: inputOpts } = validateArgsWithOptions(connectorConfig, dcOrVars, varsOrOptions, options, true, true);
  return executeQuery(getStockAdjustmentsCountRef(dcInstance, inputVars), inputOpts && { fetchPolicy: inputOpts.fetchPolicy });
}
  ;
