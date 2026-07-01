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
