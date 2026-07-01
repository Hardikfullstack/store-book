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

export function getUserRef(dcOrVars, vars) {
  const { dc: dcInstance, vars: inputVars} = validateArgs(connectorConfig, dcOrVars, vars, true);
  dcInstance._useGeneratedSdk();
  return queryRef(dcInstance, 'GetUser', inputVars);
}

export function getUser(dcOrVars, vars) {
  return executeQuery(getUserRef(dcOrVars, vars));
}

