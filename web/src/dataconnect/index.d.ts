import { ConnectorConfig, DataConnect, QueryRef, QueryPromise, MutationRef, MutationPromise } from 'firebase/data-connect';

export const connectorConfig: ConnectorConfig;

export type TimestampString = string;
export type UUIDString = string;
export type Int64String = string;
export type DateString = string;


export interface ExpenseEntry_Key {
  id: string;
  __typename?: 'ExpenseEntry_Key';
}

export interface GetActiveExpensesData {
  expenseEntries: ({
    id: string;
    type: string;
    description: string;
    amount: number;
    timestamp: number;
    supplierName?: string | null;
    updatedAt: number;
  } & ExpenseEntry_Key)[];
}

export interface GetActiveExpensesVariables {
  storeId: string;
}

export interface GetActiveItemsData {
  items: ({
    id: string;
    name: string;
    quantity: number;
    sellPrice: number;
    buyPrice: number;
    category: string;
  } & Item_Key)[];
}

export interface GetActiveItemsVariables {
  storeId: string;
}

export interface GetActiveSaleItemsData {
  saleItemDetails: ({
    id: string;
    saleId: string;
    itemId: string;
    itemName: string;
    quantity: number;
    sellPrice: number;
    buyPrice: number;
  } & SaleItemDetail_Key)[];
}

export interface GetActiveSaleItemsVariables {
  storeId: string;
}

export interface GetActiveSalesData {
  sales: ({
    id: string;
    timestamp: number;
    totalAmount: number;
    discountAmount: number;
    customerName?: string | null;
    type: string;
    notes?: string | null;
    updatedAt: number;
  } & Sale_Key)[];
}

export interface GetActiveSalesVariables {
  storeId: string;
}

export interface GetActiveSuppliersData {
  suppliers: ({
    id: string;
    name: string;
    phone?: string | null;
    gstin?: string | null;
    address?: string | null;
    updatedAt: number;
  } & Supplier_Key)[];
}

export interface GetActiveSuppliersVariables {
  storeId: string;
}

export interface GetActiveUdhaarsData {
  udhaarEntries: ({
    id: string;
    customerName: string;
    amount: number;
    type: string;
    timestamp: number;
    notes?: string | null;
    updatedAt: number;
  } & UdhaarEntry_Key)[];
}

export interface GetActiveUdhaarsVariables {
  storeId: string;
}

export interface GetUserData {
  user?: {
    id: string;
    phoneNumber?: string | null;
    role: string;
    stores?: string[] | null;
    storeId?: string | null;
  } & User_Key;
}

export interface GetUserVariables {
  id: string;
}

export interface ItemBatch_Key {
  id: string;
  __typename?: 'ItemBatch_Key';
}

export interface Item_Key {
  id: string;
  __typename?: 'Item_Key';
}

export interface PurchaseItemDetail_Key {
  id: string;
  __typename?: 'PurchaseItemDetail_Key';
}

export interface Purchase_Key {
  id: string;
  __typename?: 'Purchase_Key';
}

export interface SaleItemDetail_Key {
  id: string;
  __typename?: 'SaleItemDetail_Key';
}

export interface Sale_Key {
  id: string;
  __typename?: 'Sale_Key';
}

export interface SoftDeleteExpenseData {
  expenseEntry_update?: ExpenseEntry_Key | null;
}

export interface SoftDeleteExpenseVariables {
  id: string;
  updatedAt: number;
}

export interface SoftDeleteItemData {
  item_update?: Item_Key | null;
}

export interface SoftDeleteItemVariables {
  id: string;
  updatedAt: number;
}

export interface SoftDeleteSaleData {
  sale_update?: Sale_Key | null;
}

export interface SoftDeleteSaleVariables {
  id: string;
  updatedAt: number;
}

export interface SoftDeleteUdhaarData {
  udhaarEntry_update?: UdhaarEntry_Key | null;
}

export interface SoftDeleteUdhaarVariables {
  id: string;
  updatedAt: number;
}

export interface Store_Key {
  id: string;
  __typename?: 'Store_Key';
}

export interface Supplier_Key {
  id: string;
  __typename?: 'Supplier_Key';
}

export interface SyncExpenseData {
  expenseEntry_upsert: ExpenseEntry_Key;
}

export interface SyncExpenseVariables {
  id: string;
  storeId: string;
  type: string;
  description: string;
  amount: number;
  timestamp: number;
  supplierName?: string | null;
  supplierPhone?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncExpensesData {
  expenseEntries: ({
    id: string;
    storeId: string;
    type: string;
    description: string;
    amount: number;
    timestamp: number;
    supplierName?: string | null;
    supplierPhone?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & ExpenseEntry_Key)[];
}

export interface SyncExpensesVariables {
  storeId: string;
  lastSync: number;
}

export interface SyncItemData {
  item_upsert: Item_Key;
}

export interface SyncItemVariables {
  id: string;
  storeId: string;
  name: string;
  quantity: number;
  unit: string;
  buyPrice: number;
  sellPrice: number;
  lowStockThreshold: number;
  category: string;
  photoPath?: string | null;
  hsnCode?: string | null;
  taxRate?: number | null;
  batchLotNumber?: string | null;
  expiryDate?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncItemsData {
  items: ({
    id: string;
    name: string;
    quantity: number;
    unit: string;
    buyPrice: number;
    sellPrice: number;
    lowStockThreshold: number;
    category: string;
    photoPath?: string | null;
    hsnCode?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & Item_Key)[];
}

export interface SyncItemsVariables {
  storeId: string;
  lastSync: number;
}

export interface SyncSaleData {
  sale_upsert: Sale_Key;
}

export interface SyncSaleItemData {
  saleItemDetail_upsert: SaleItemDetail_Key;
}

export interface SyncSaleItemVariables {
  id: string;
  storeId: string;
  saleId: string;
  itemId: string;
  itemName: string;
  unit: string;
  quantity: number;
  sellPrice: number;
  buyPrice: number;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncSaleItemsData {
  saleItemDetails: ({
    id: string;
    saleId: string;
    itemId: string;
    itemName: string;
    quantity: number;
    unit: string;
    sellPrice: number;
    buyPrice: number;
    isDeleted: boolean;
    updatedAt: number;
  } & SaleItemDetail_Key)[];
}

export interface SyncSaleItemsVariables {
  storeId: string;
  lastSync: number;
}

export interface SyncSaleVariables {
  id: string;
  storeId: string;
  timestamp: number;
  totalAmount: number;
  discountAmount: number;
  customerName?: string | null;
  customerGstin?: string | null;
  businessGstin?: string | null;
  customerAddress?: string | null;
  businessAddress?: string | null;
  type: string;
  notes?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncSalesData {
  sales: ({
    id: string;
    timestamp: number;
    totalAmount: number;
    discountAmount: number;
    customerName?: string | null;
    customerGstin?: string | null;
    businessGstin?: string | null;
    customerAddress?: string | null;
    businessAddress?: string | null;
    type: string;
    isDeleted: boolean;
    updatedAt: number;
  } & Sale_Key)[];
}

export interface SyncSalesVariables {
  storeId: string;
  lastSync: number;
}

export interface SyncStoreData {
  store_upsert: Store_Key;
}

export interface SyncStoreVariables {
  id: string;
  name?: string | null;
  isActive?: boolean | null;
  isPremium?: boolean | null;
  subscriptionExpiresAt?: number | null;
  subscriptionPlatform?: string | null;
  subscriptionId?: string | null;
  subscriptionStatus?: string | null;
}

export interface SyncSupplierData {
  supplier_upsert: Supplier_Key;
}

export interface SyncSupplierVariables {
  id: string;
  storeId: string;
  name: string;
  phone?: string | null;
  gstin?: string | null;
  address?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncUdhaarData {
  udhaarEntry_upsert: UdhaarEntry_Key;
}

export interface SyncUdhaarVariables {
  id: string;
  storeId: string;
  customerName: string;
  amount: number;
  type: string;
  timestamp: number;
  notes?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncUdhaarsData {
  udhaarEntries: ({
    id: string;
    storeId: string;
    customerName: string;
    amount: number;
    type: string;
    timestamp: number;
    notes?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & UdhaarEntry_Key)[];
}

export interface SyncUdhaarsVariables {
  storeId: string;
  lastSync: number;
}

export interface SyncUserData {
  user_upsert: User_Key;
}

export interface SyncUserVariables {
  id: string;
  phoneNumber?: string | null;
  username?: string | null;
  createdAt: number;
  role: string;
  stores?: string[] | null;
  storeId?: string | null;
  ownerId?: string | null;
  subscriptionStatus?: string | null;
  subscriptionPlan?: string | null;
  subscriptionExpiresAt?: number | null;
  subscriptionPlatform?: string | null;
  subscriptionId?: string | null;
}

export interface UdhaarEntry_Key {
  id: string;
  __typename?: 'UdhaarEntry_Key';
}

export interface UpdateStoreData {
  store_update?: Store_Key | null;
}

export interface UpdateStoreVariables {
  id: string;
  isPremium?: boolean | null;
  subscriptionExpiresAt?: number | null;
  subscriptionPlatform?: string | null;
  subscriptionId?: string | null;
  subscriptionStatus?: string | null;
}

export interface UpdateUserData {
  user_update?: User_Key | null;
}

export interface UpdateUserVariables {
  id: string;
  subscriptionStatus?: string | null;
  subscriptionPlan?: string | null;
  subscriptionExpiresAt?: number | null;
  subscriptionPlatform?: string | null;
  subscriptionId?: string | null;
}

export interface User_Key {
  id: string;
  __typename?: 'User_Key';
}

/* Allow users to create refs without passing in DataConnect */
export function syncItemRef(vars: SyncItemVariables): MutationRef<SyncItemData, SyncItemVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncItemRef(dc: DataConnect, vars: SyncItemVariables): MutationRef<SyncItemData, SyncItemVariables>;

export function syncItem(vars: SyncItemVariables): MutationPromise<SyncItemData, SyncItemVariables>;
export function syncItem(dc: DataConnect, vars: SyncItemVariables): MutationPromise<SyncItemData, SyncItemVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncSaleRef(vars: SyncSaleVariables): MutationRef<SyncSaleData, SyncSaleVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncSaleRef(dc: DataConnect, vars: SyncSaleVariables): MutationRef<SyncSaleData, SyncSaleVariables>;

export function syncSale(vars: SyncSaleVariables): MutationPromise<SyncSaleData, SyncSaleVariables>;
export function syncSale(dc: DataConnect, vars: SyncSaleVariables): MutationPromise<SyncSaleData, SyncSaleVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncSaleItemRef(vars: SyncSaleItemVariables): MutationRef<SyncSaleItemData, SyncSaleItemVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncSaleItemRef(dc: DataConnect, vars: SyncSaleItemVariables): MutationRef<SyncSaleItemData, SyncSaleItemVariables>;

export function syncSaleItem(vars: SyncSaleItemVariables): MutationPromise<SyncSaleItemData, SyncSaleItemVariables>;
export function syncSaleItem(dc: DataConnect, vars: SyncSaleItemVariables): MutationPromise<SyncSaleItemData, SyncSaleItemVariables>;

/* Allow users to create refs without passing in DataConnect */
export function softDeleteItemRef(vars: SoftDeleteItemVariables): MutationRef<SoftDeleteItemData, SoftDeleteItemVariables>;
/* Allow users to pass in custom DataConnect instances */
export function softDeleteItemRef(dc: DataConnect, vars: SoftDeleteItemVariables): MutationRef<SoftDeleteItemData, SoftDeleteItemVariables>;

export function softDeleteItem(vars: SoftDeleteItemVariables): MutationPromise<SoftDeleteItemData, SoftDeleteItemVariables>;
export function softDeleteItem(dc: DataConnect, vars: SoftDeleteItemVariables): MutationPromise<SoftDeleteItemData, SoftDeleteItemVariables>;

/* Allow users to create refs without passing in DataConnect */
export function softDeleteSaleRef(vars: SoftDeleteSaleVariables): MutationRef<SoftDeleteSaleData, SoftDeleteSaleVariables>;
/* Allow users to pass in custom DataConnect instances */
export function softDeleteSaleRef(dc: DataConnect, vars: SoftDeleteSaleVariables): MutationRef<SoftDeleteSaleData, SoftDeleteSaleVariables>;

export function softDeleteSale(vars: SoftDeleteSaleVariables): MutationPromise<SoftDeleteSaleData, SoftDeleteSaleVariables>;
export function softDeleteSale(dc: DataConnect, vars: SoftDeleteSaleVariables): MutationPromise<SoftDeleteSaleData, SoftDeleteSaleVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncUserRef(vars: SyncUserVariables): MutationRef<SyncUserData, SyncUserVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncUserRef(dc: DataConnect, vars: SyncUserVariables): MutationRef<SyncUserData, SyncUserVariables>;

export function syncUser(vars: SyncUserVariables): MutationPromise<SyncUserData, SyncUserVariables>;
export function syncUser(dc: DataConnect, vars: SyncUserVariables): MutationPromise<SyncUserData, SyncUserVariables>;

/* Allow users to create refs without passing in DataConnect */
export function updateUserRef(vars: UpdateUserVariables): MutationRef<UpdateUserData, UpdateUserVariables>;
/* Allow users to pass in custom DataConnect instances */
export function updateUserRef(dc: DataConnect, vars: UpdateUserVariables): MutationRef<UpdateUserData, UpdateUserVariables>;

export function updateUser(vars: UpdateUserVariables): MutationPromise<UpdateUserData, UpdateUserVariables>;
export function updateUser(dc: DataConnect, vars: UpdateUserVariables): MutationPromise<UpdateUserData, UpdateUserVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncStoreRef(vars: SyncStoreVariables): MutationRef<SyncStoreData, SyncStoreVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncStoreRef(dc: DataConnect, vars: SyncStoreVariables): MutationRef<SyncStoreData, SyncStoreVariables>;

export function syncStore(vars: SyncStoreVariables): MutationPromise<SyncStoreData, SyncStoreVariables>;
export function syncStore(dc: DataConnect, vars: SyncStoreVariables): MutationPromise<SyncStoreData, SyncStoreVariables>;

/* Allow users to create refs without passing in DataConnect */
export function updateStoreRef(vars: UpdateStoreVariables): MutationRef<UpdateStoreData, UpdateStoreVariables>;
/* Allow users to pass in custom DataConnect instances */
export function updateStoreRef(dc: DataConnect, vars: UpdateStoreVariables): MutationRef<UpdateStoreData, UpdateStoreVariables>;

export function updateStore(vars: UpdateStoreVariables): MutationPromise<UpdateStoreData, UpdateStoreVariables>;
export function updateStore(dc: DataConnect, vars: UpdateStoreVariables): MutationPromise<UpdateStoreData, UpdateStoreVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncUdhaarRef(vars: SyncUdhaarVariables): MutationRef<SyncUdhaarData, SyncUdhaarVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncUdhaarRef(dc: DataConnect, vars: SyncUdhaarVariables): MutationRef<SyncUdhaarData, SyncUdhaarVariables>;

export function syncUdhaar(vars: SyncUdhaarVariables): MutationPromise<SyncUdhaarData, SyncUdhaarVariables>;
export function syncUdhaar(dc: DataConnect, vars: SyncUdhaarVariables): MutationPromise<SyncUdhaarData, SyncUdhaarVariables>;

/* Allow users to create refs without passing in DataConnect */
export function softDeleteUdhaarRef(vars: SoftDeleteUdhaarVariables): MutationRef<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;
/* Allow users to pass in custom DataConnect instances */
export function softDeleteUdhaarRef(dc: DataConnect, vars: SoftDeleteUdhaarVariables): MutationRef<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;

export function softDeleteUdhaar(vars: SoftDeleteUdhaarVariables): MutationPromise<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;
export function softDeleteUdhaar(dc: DataConnect, vars: SoftDeleteUdhaarVariables): MutationPromise<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncExpenseRef(vars: SyncExpenseVariables): MutationRef<SyncExpenseData, SyncExpenseVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncExpenseRef(dc: DataConnect, vars: SyncExpenseVariables): MutationRef<SyncExpenseData, SyncExpenseVariables>;

export function syncExpense(vars: SyncExpenseVariables): MutationPromise<SyncExpenseData, SyncExpenseVariables>;
export function syncExpense(dc: DataConnect, vars: SyncExpenseVariables): MutationPromise<SyncExpenseData, SyncExpenseVariables>;

/* Allow users to create refs without passing in DataConnect */
export function softDeleteExpenseRef(vars: SoftDeleteExpenseVariables): MutationRef<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;
/* Allow users to pass in custom DataConnect instances */
export function softDeleteExpenseRef(dc: DataConnect, vars: SoftDeleteExpenseVariables): MutationRef<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;

export function softDeleteExpense(vars: SoftDeleteExpenseVariables): MutationPromise<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;
export function softDeleteExpense(dc: DataConnect, vars: SoftDeleteExpenseVariables): MutationPromise<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncSupplierRef(vars: SyncSupplierVariables): MutationRef<SyncSupplierData, SyncSupplierVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncSupplierRef(dc: DataConnect, vars: SyncSupplierVariables): MutationRef<SyncSupplierData, SyncSupplierVariables>;

export function syncSupplier(vars: SyncSupplierVariables): MutationPromise<SyncSupplierData, SyncSupplierVariables>;
export function syncSupplier(dc: DataConnect, vars: SyncSupplierVariables): MutationPromise<SyncSupplierData, SyncSupplierVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncItemsRef(vars: SyncItemsVariables): QueryRef<SyncItemsData, SyncItemsVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncItemsRef(dc: DataConnect, vars: SyncItemsVariables): QueryRef<SyncItemsData, SyncItemsVariables>;

export function syncItems(vars: SyncItemsVariables): QueryPromise<SyncItemsData, SyncItemsVariables>;
export function syncItems(dc: DataConnect, vars: SyncItemsVariables): QueryPromise<SyncItemsData, SyncItemsVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncSalesRef(vars: SyncSalesVariables): QueryRef<SyncSalesData, SyncSalesVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncSalesRef(dc: DataConnect, vars: SyncSalesVariables): QueryRef<SyncSalesData, SyncSalesVariables>;

export function syncSales(vars: SyncSalesVariables): QueryPromise<SyncSalesData, SyncSalesVariables>;
export function syncSales(dc: DataConnect, vars: SyncSalesVariables): QueryPromise<SyncSalesData, SyncSalesVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncSaleItemsRef(vars: SyncSaleItemsVariables): QueryRef<SyncSaleItemsData, SyncSaleItemsVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncSaleItemsRef(dc: DataConnect, vars: SyncSaleItemsVariables): QueryRef<SyncSaleItemsData, SyncSaleItemsVariables>;

export function syncSaleItems(vars: SyncSaleItemsVariables): QueryPromise<SyncSaleItemsData, SyncSaleItemsVariables>;
export function syncSaleItems(dc: DataConnect, vars: SyncSaleItemsVariables): QueryPromise<SyncSaleItemsData, SyncSaleItemsVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getActiveItemsRef(vars: GetActiveItemsVariables): QueryRef<GetActiveItemsData, GetActiveItemsVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getActiveItemsRef(dc: DataConnect, vars: GetActiveItemsVariables): QueryRef<GetActiveItemsData, GetActiveItemsVariables>;

export function getActiveItems(vars: GetActiveItemsVariables): QueryPromise<GetActiveItemsData, GetActiveItemsVariables>;
export function getActiveItems(dc: DataConnect, vars: GetActiveItemsVariables): QueryPromise<GetActiveItemsData, GetActiveItemsVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncUdhaarsRef(vars: SyncUdhaarsVariables): QueryRef<SyncUdhaarsData, SyncUdhaarsVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncUdhaarsRef(dc: DataConnect, vars: SyncUdhaarsVariables): QueryRef<SyncUdhaarsData, SyncUdhaarsVariables>;

export function syncUdhaars(vars: SyncUdhaarsVariables): QueryPromise<SyncUdhaarsData, SyncUdhaarsVariables>;
export function syncUdhaars(dc: DataConnect, vars: SyncUdhaarsVariables): QueryPromise<SyncUdhaarsData, SyncUdhaarsVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncExpensesRef(vars: SyncExpensesVariables): QueryRef<SyncExpensesData, SyncExpensesVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncExpensesRef(dc: DataConnect, vars: SyncExpensesVariables): QueryRef<SyncExpensesData, SyncExpensesVariables>;

export function syncExpenses(vars: SyncExpensesVariables): QueryPromise<SyncExpensesData, SyncExpensesVariables>;
export function syncExpenses(dc: DataConnect, vars: SyncExpensesVariables): QueryPromise<SyncExpensesData, SyncExpensesVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getActiveSalesRef(vars: GetActiveSalesVariables): QueryRef<GetActiveSalesData, GetActiveSalesVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getActiveSalesRef(dc: DataConnect, vars: GetActiveSalesVariables): QueryRef<GetActiveSalesData, GetActiveSalesVariables>;

export function getActiveSales(vars: GetActiveSalesVariables): QueryPromise<GetActiveSalesData, GetActiveSalesVariables>;
export function getActiveSales(dc: DataConnect, vars: GetActiveSalesVariables): QueryPromise<GetActiveSalesData, GetActiveSalesVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getActiveSaleItemsRef(vars: GetActiveSaleItemsVariables): QueryRef<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getActiveSaleItemsRef(dc: DataConnect, vars: GetActiveSaleItemsVariables): QueryRef<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;

export function getActiveSaleItems(vars: GetActiveSaleItemsVariables): QueryPromise<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;
export function getActiveSaleItems(dc: DataConnect, vars: GetActiveSaleItemsVariables): QueryPromise<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getActiveUdhaarsRef(vars: GetActiveUdhaarsVariables): QueryRef<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getActiveUdhaarsRef(dc: DataConnect, vars: GetActiveUdhaarsVariables): QueryRef<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;

export function getActiveUdhaars(vars: GetActiveUdhaarsVariables): QueryPromise<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;
export function getActiveUdhaars(dc: DataConnect, vars: GetActiveUdhaarsVariables): QueryPromise<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getActiveExpensesRef(vars: GetActiveExpensesVariables): QueryRef<GetActiveExpensesData, GetActiveExpensesVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getActiveExpensesRef(dc: DataConnect, vars: GetActiveExpensesVariables): QueryRef<GetActiveExpensesData, GetActiveExpensesVariables>;

export function getActiveExpenses(vars: GetActiveExpensesVariables): QueryPromise<GetActiveExpensesData, GetActiveExpensesVariables>;
export function getActiveExpenses(dc: DataConnect, vars: GetActiveExpensesVariables): QueryPromise<GetActiveExpensesData, GetActiveExpensesVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getActiveSuppliersRef(vars: GetActiveSuppliersVariables): QueryRef<GetActiveSuppliersData, GetActiveSuppliersVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getActiveSuppliersRef(dc: DataConnect, vars: GetActiveSuppliersVariables): QueryRef<GetActiveSuppliersData, GetActiveSuppliersVariables>;

export function getActiveSuppliers(vars: GetActiveSuppliersVariables): QueryPromise<GetActiveSuppliersData, GetActiveSuppliersVariables>;
export function getActiveSuppliers(dc: DataConnect, vars: GetActiveSuppliersVariables): QueryPromise<GetActiveSuppliersData, GetActiveSuppliersVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getUserRef(vars: GetUserVariables): QueryRef<GetUserData, GetUserVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getUserRef(dc: DataConnect, vars: GetUserVariables): QueryRef<GetUserData, GetUserVariables>;

export function getUser(vars: GetUserVariables): QueryPromise<GetUserData, GetUserVariables>;
export function getUser(dc: DataConnect, vars: GetUserVariables): QueryPromise<GetUserData, GetUserVariables>;

