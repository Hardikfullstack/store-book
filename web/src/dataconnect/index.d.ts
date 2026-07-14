import { ConnectorConfig, DataConnect, QueryRef, QueryPromise, MutationRef, MutationPromise } from 'firebase/data-connect';

export const connectorConfig: ConnectorConfig;

export type TimestampString = string;
export type UUIDString = string;
export type Int64String = string;
export type DateString = string;

export enum OrderDirection {

  ASC = "ASC",

  DESC = "DESC",

}
export interface AdminAuditLog_Key {
  id: string;
  __typename?: 'AdminAuditLog_Key';
}

export interface Announcement_Key {
  id: string;
  __typename?: 'Announcement_Key';
}

export interface CreateAdminAuditLogData {
  adminAuditLog_insert: AdminAuditLog_Key;
}

export interface CreateAdminAuditLogVariables {
  adminId: string;
  adminUsername?: string | null;
  action: string;
  targetId?: string | null;
  details?: string | null;
  timestamp: number;
}

export interface CreateUserData {
  user_upsert: User_Key;
}

export interface CreateUserVariables {
  id: string;
  role: string;
  createdAt: number;
  storeId: string;
  canViewProfit?: boolean | null;
  canDelete?: boolean | null;
}

export interface DeleteAnnouncementData {
  announcement_delete?: Announcement_Key | null;
}

export interface DeleteAnnouncementVariables {
  id: string;
}

export interface DeletePromoCodeData {
  promoCode_delete?: PromoCode_Key | null;
}

export interface DeletePromoCodeVariables {
  id: string;
}

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
  limit?: number | null;
  offset?: number | null;
  searchTerm?: string | null;
  orderByTimestamp?: OrderDirection | null;
  orderByType?: OrderDirection | null;
  orderBySupplierName?: OrderDirection | null;
  orderByAmount?: OrderDirection | null;
}

export interface GetActiveItemsData {
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

export interface GetActiveItemsVariables {
  storeId: string;
  limit?: number | null;
  offset?: number | null;
  searchTerm?: string | null;
  orderByName?: OrderDirection | null;
  orderByQuantity?: OrderDirection | null;
  orderByBuyPrice?: OrderDirection | null;
  orderBySellPrice?: OrderDirection | null;
  orderByCategory?: OrderDirection | null;
  orderByUpdatedAt?: OrderDirection | null;
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
  type?: string | null;
  limit?: number | null;
  offset?: number | null;
  searchTerm?: string | null;
  orderByTimestamp?: OrderDirection | null;
  orderByCustomerName?: OrderDirection | null;
  orderByTotalAmount?: OrderDirection | null;
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
  limit?: number | null;
  offset?: number | null;
  searchTerm?: string | null;
  orderByTimestamp?: OrderDirection | null;
  orderByCustomerName?: OrderDirection | null;
  orderByType?: OrderDirection | null;
  orderByAmount?: OrderDirection | null;
}

export interface GetAdminAuditLogsData {
  adminAuditLogs: ({
    id: string;
    adminId: string;
    adminUsername?: string | null;
    action: string;
    targetId?: string | null;
    details?: string | null;
    timestamp: number;
  } & AdminAuditLog_Key)[];
}

export interface GetAnnouncementsData {
  announcements: ({
    id: string;
    title: string;
    message: string;
    type: string;
    isActive: boolean;
    createdAt: number;
  } & Announcement_Key)[];
}

export interface GetExpenseEntriesCountData {
  expenseEntries: ({
    id: string;
  } & ExpenseEntry_Key)[];
}

export interface GetExpenseEntriesCountVariables {
  storeId: string;
}

export interface GetGlobalSettingsData {
  globalSettings: ({
    id: string;
    key: string;
    value: string;
    description?: string | null;
    updatedAt: number;
    updatedBy?: string | null;
  } & GlobalSetting_Key)[];
}

export interface GetItemsCountData {
  items: ({
    id: string;
  } & Item_Key)[];
}

export interface GetItemsCountVariables {
  storeId: string;
}

export interface GetPromoCodesData {
  promoCodes: ({
    id: string;
    code: string;
    discountPercent?: number | null;
    discountAmount?: number | null;
    maxUses?: number | null;
    currentUses?: number | null;
    expiresAt?: number | null;
    isActive: boolean;
  } & PromoCode_Key)[];
}

export interface GetSalesCountData {
  sales: ({
    id: string;
  } & Sale_Key)[];
}

export interface GetSalesCountVariables {
  storeId: string;
  type?: string | null;
}

export interface GetStoreData {
  store?: {
    id: string;
    name?: string | null;
    isActive?: boolean | null;
    isPremium?: boolean | null;
  } & Store_Key;
}

export interface GetStoreVariables {
  id: string;
}

export interface GetStoresPaginatedData {
  stores: ({
    id: string;
    name?: string | null;
    isActive?: boolean | null;
    isPremium?: boolean | null;
    subscriptionPlatform?: string | null;
    subscriptionStatus?: string | null;
    subscriptionExpiresAt?: number | null;
  } & Store_Key)[];
}

export interface GetUdhaarEntriesCountData {
  udhaarEntries: ({
    id: string;
  } & UdhaarEntry_Key)[];
}

export interface GetUdhaarEntriesCountVariables {
  storeId: string;
}

export interface GetUserData {
  user?: {
    id: string;
    phoneNumber?: string | null;
    role: string;
    stores?: string[] | null;
    storeId?: string | null;
    subscriptionPlan?: string | null;
    subscriptionStatus?: string | null;
  } & User_Key;
}

export interface GetUserVariables {
  id: string;
}

export interface GetUsersPaginatedData {
  users: ({
    id: string;
    phoneNumber?: string | null;
    username?: string | null;
    role: string;
    createdAt: number;
    storeId?: string | null;
  } & User_Key)[];
}

export interface GlobalSetting_Key {
  id: string;
  __typename?: 'GlobalSetting_Key';
}

export interface ItemBatch_Key {
  id: string;
  __typename?: 'ItemBatch_Key';
}

export interface Item_Key {
  id: string;
  __typename?: 'Item_Key';
}

export interface PromoCode_Key {
  id: string;
  __typename?: 'PromoCode_Key';
}

export interface PurchaseItemDetail_Key {
  id: string;
  __typename?: 'PurchaseItemDetail_Key';
}

export interface Purchase_Key {
  id: string;
  __typename?: 'Purchase_Key';
}

export interface PurgeStoreData {
  store_update?: Store_Key | null;
}

export interface PurgeStoreVariables {
  id: string;
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

export interface SyncItemBatchData {
  itemBatch_upsert: ItemBatch_Key;
}

export interface SyncItemBatchVariables {
  id: string;
  storeId: string;
  itemId: string;
  batchNumber?: string | null;
  expiryDate?: number | null;
  quantity: number;
  costPrice: number;
  timestamp: number;
  notes?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncItemBatchesData {
  itemBatches: ({
    id: string;
    storeId: string;
    itemId: string;
    batchNumber?: string | null;
    expiryDate?: number | null;
    quantity: number;
    costPrice: number;
    timestamp: number;
    notes?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & ItemBatch_Key)[];
}

export interface SyncItemBatchesVariables {
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

export interface SyncPurchaseData {
  purchase_upsert: Purchase_Key;
}

export interface SyncPurchaseItemData {
  purchaseItemDetail_upsert: PurchaseItemDetail_Key;
}

export interface SyncPurchaseItemVariables {
  id: string;
  storeId: string;
  purchaseId: string;
  itemId: string;
  itemName: string;
  quantity: number;
  unit: string;
  buyPrice: number;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncPurchaseItemsData {
  purchaseItemDetails: ({
    id: string;
    storeId: string;
    purchaseId: string;
    itemId: string;
    itemName: string;
    quantity: number;
    unit: string;
    buyPrice: number;
    isDeleted: boolean;
    updatedAt: number;
  } & PurchaseItemDetail_Key)[];
}

export interface SyncPurchaseItemsVariables {
  storeId: string;
  lastSync: number;
}

export interface SyncPurchaseVariables {
  id: string;
  storeId: string;
  supplierId: string;
  supplierName: string;
  totalAmount: number;
  taxAmount: number;
  type: string;
  timestamp: number;
  notes?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

export interface SyncPurchasesData {
  purchases: ({
    id: string;
    storeId: string;
    supplierId: string;
    supplierName: string;
    totalAmount: number;
    taxAmount: number;
    type: string;
    timestamp: number;
    notes?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & Purchase_Key)[];
}

export interface SyncPurchasesVariables {
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
    notes?: string | null;
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

export interface SyncSuppliersData {
  suppliers: ({
    id: string;
    storeId: string;
    name: string;
    phone?: string | null;
    gstin?: string | null;
    address?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & Supplier_Key)[];
}

export interface SyncSuppliersVariables {
  storeId: string;
  lastSync: number;
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

export interface ToggleStoreStatusData {
  store_update?: Store_Key | null;
}

export interface ToggleStoreStatusVariables {
  id: string;
  isActive?: boolean | null;
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

export interface UpsertAnnouncementData {
  announcement_upsert: Announcement_Key;
}

export interface UpsertAnnouncementVariables {
  id: string;
  title: string;
  message: string;
  type: string;
  isActive: boolean;
  createdAt: number;
}

export interface UpsertGlobalSettingData {
  globalSetting_upsert: GlobalSetting_Key;
}

export interface UpsertGlobalSettingVariables {
  id: string;
  key: string;
  value: string;
  description?: string | null;
  updatedAt: number;
  updatedBy?: string | null;
}

export interface UpsertPromoCodeData {
  promoCode_upsert: PromoCode_Key;
}

export interface UpsertPromoCodeVariables {
  id: string;
  code: string;
  discountPercent?: number | null;
  discountAmount?: number | null;
  maxUses?: number | null;
  expiresAt?: number | null;
  isActive: boolean;
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
export function upsertGlobalSettingRef(vars: UpsertGlobalSettingVariables): MutationRef<UpsertGlobalSettingData, UpsertGlobalSettingVariables>;
/* Allow users to pass in custom DataConnect instances */
export function upsertGlobalSettingRef(dc: DataConnect, vars: UpsertGlobalSettingVariables): MutationRef<UpsertGlobalSettingData, UpsertGlobalSettingVariables>;

export function upsertGlobalSetting(vars: UpsertGlobalSettingVariables): MutationPromise<UpsertGlobalSettingData, UpsertGlobalSettingVariables>;
export function upsertGlobalSetting(dc: DataConnect, vars: UpsertGlobalSettingVariables): MutationPromise<UpsertGlobalSettingData, UpsertGlobalSettingVariables>;

/* Allow users to create refs without passing in DataConnect */
export function createAdminAuditLogRef(vars: CreateAdminAuditLogVariables): MutationRef<CreateAdminAuditLogData, CreateAdminAuditLogVariables>;
/* Allow users to pass in custom DataConnect instances */
export function createAdminAuditLogRef(dc: DataConnect, vars: CreateAdminAuditLogVariables): MutationRef<CreateAdminAuditLogData, CreateAdminAuditLogVariables>;

export function createAdminAuditLog(vars: CreateAdminAuditLogVariables): MutationPromise<CreateAdminAuditLogData, CreateAdminAuditLogVariables>;
export function createAdminAuditLog(dc: DataConnect, vars: CreateAdminAuditLogVariables): MutationPromise<CreateAdminAuditLogData, CreateAdminAuditLogVariables>;

/* Allow users to create refs without passing in DataConnect */
export function upsertAnnouncementRef(vars: UpsertAnnouncementVariables): MutationRef<UpsertAnnouncementData, UpsertAnnouncementVariables>;
/* Allow users to pass in custom DataConnect instances */
export function upsertAnnouncementRef(dc: DataConnect, vars: UpsertAnnouncementVariables): MutationRef<UpsertAnnouncementData, UpsertAnnouncementVariables>;

export function upsertAnnouncement(vars: UpsertAnnouncementVariables): MutationPromise<UpsertAnnouncementData, UpsertAnnouncementVariables>;
export function upsertAnnouncement(dc: DataConnect, vars: UpsertAnnouncementVariables): MutationPromise<UpsertAnnouncementData, UpsertAnnouncementVariables>;

/* Allow users to create refs without passing in DataConnect */
export function deleteAnnouncementRef(vars: DeleteAnnouncementVariables): MutationRef<DeleteAnnouncementData, DeleteAnnouncementVariables>;
/* Allow users to pass in custom DataConnect instances */
export function deleteAnnouncementRef(dc: DataConnect, vars: DeleteAnnouncementVariables): MutationRef<DeleteAnnouncementData, DeleteAnnouncementVariables>;

export function deleteAnnouncement(vars: DeleteAnnouncementVariables): MutationPromise<DeleteAnnouncementData, DeleteAnnouncementVariables>;
export function deleteAnnouncement(dc: DataConnect, vars: DeleteAnnouncementVariables): MutationPromise<DeleteAnnouncementData, DeleteAnnouncementVariables>;

/* Allow users to create refs without passing in DataConnect */
export function upsertPromoCodeRef(vars: UpsertPromoCodeVariables): MutationRef<UpsertPromoCodeData, UpsertPromoCodeVariables>;
/* Allow users to pass in custom DataConnect instances */
export function upsertPromoCodeRef(dc: DataConnect, vars: UpsertPromoCodeVariables): MutationRef<UpsertPromoCodeData, UpsertPromoCodeVariables>;

export function upsertPromoCode(vars: UpsertPromoCodeVariables): MutationPromise<UpsertPromoCodeData, UpsertPromoCodeVariables>;
export function upsertPromoCode(dc: DataConnect, vars: UpsertPromoCodeVariables): MutationPromise<UpsertPromoCodeData, UpsertPromoCodeVariables>;

/* Allow users to create refs without passing in DataConnect */
export function deletePromoCodeRef(vars: DeletePromoCodeVariables): MutationRef<DeletePromoCodeData, DeletePromoCodeVariables>;
/* Allow users to pass in custom DataConnect instances */
export function deletePromoCodeRef(dc: DataConnect, vars: DeletePromoCodeVariables): MutationRef<DeletePromoCodeData, DeletePromoCodeVariables>;

export function deletePromoCode(vars: DeletePromoCodeVariables): MutationPromise<DeletePromoCodeData, DeletePromoCodeVariables>;
export function deletePromoCode(dc: DataConnect, vars: DeletePromoCodeVariables): MutationPromise<DeletePromoCodeData, DeletePromoCodeVariables>;

/* Allow users to create refs without passing in DataConnect */
export function toggleStoreStatusRef(vars: ToggleStoreStatusVariables): MutationRef<ToggleStoreStatusData, ToggleStoreStatusVariables>;
/* Allow users to pass in custom DataConnect instances */
export function toggleStoreStatusRef(dc: DataConnect, vars: ToggleStoreStatusVariables): MutationRef<ToggleStoreStatusData, ToggleStoreStatusVariables>;

export function toggleStoreStatus(vars: ToggleStoreStatusVariables): MutationPromise<ToggleStoreStatusData, ToggleStoreStatusVariables>;
export function toggleStoreStatus(dc: DataConnect, vars: ToggleStoreStatusVariables): MutationPromise<ToggleStoreStatusData, ToggleStoreStatusVariables>;

/* Allow users to create refs without passing in DataConnect */
export function purgeStoreRef(vars: PurgeStoreVariables): MutationRef<PurgeStoreData, PurgeStoreVariables>;
/* Allow users to pass in custom DataConnect instances */
export function purgeStoreRef(dc: DataConnect, vars: PurgeStoreVariables): MutationRef<PurgeStoreData, PurgeStoreVariables>;

export function purgeStore(vars: PurgeStoreVariables): MutationPromise<PurgeStoreData, PurgeStoreVariables>;
export function purgeStore(dc: DataConnect, vars: PurgeStoreVariables): MutationPromise<PurgeStoreData, PurgeStoreVariables>;

/* Allow users to create refs without passing in DataConnect */
export function createUserRef(vars: CreateUserVariables): MutationRef<CreateUserData, CreateUserVariables>;
/* Allow users to pass in custom DataConnect instances */
export function createUserRef(dc: DataConnect, vars: CreateUserVariables): MutationRef<CreateUserData, CreateUserVariables>;

export function createUser(vars: CreateUserVariables): MutationPromise<CreateUserData, CreateUserVariables>;
export function createUser(dc: DataConnect, vars: CreateUserVariables): MutationPromise<CreateUserData, CreateUserVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncPurchaseRef(vars: SyncPurchaseVariables): MutationRef<SyncPurchaseData, SyncPurchaseVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncPurchaseRef(dc: DataConnect, vars: SyncPurchaseVariables): MutationRef<SyncPurchaseData, SyncPurchaseVariables>;

export function syncPurchase(vars: SyncPurchaseVariables): MutationPromise<SyncPurchaseData, SyncPurchaseVariables>;
export function syncPurchase(dc: DataConnect, vars: SyncPurchaseVariables): MutationPromise<SyncPurchaseData, SyncPurchaseVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncPurchaseItemRef(vars: SyncPurchaseItemVariables): MutationRef<SyncPurchaseItemData, SyncPurchaseItemVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncPurchaseItemRef(dc: DataConnect, vars: SyncPurchaseItemVariables): MutationRef<SyncPurchaseItemData, SyncPurchaseItemVariables>;

export function syncPurchaseItem(vars: SyncPurchaseItemVariables): MutationPromise<SyncPurchaseItemData, SyncPurchaseItemVariables>;
export function syncPurchaseItem(dc: DataConnect, vars: SyncPurchaseItemVariables): MutationPromise<SyncPurchaseItemData, SyncPurchaseItemVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncItemBatchRef(vars: SyncItemBatchVariables): MutationRef<SyncItemBatchData, SyncItemBatchVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncItemBatchRef(dc: DataConnect, vars: SyncItemBatchVariables): MutationRef<SyncItemBatchData, SyncItemBatchVariables>;

export function syncItemBatch(vars: SyncItemBatchVariables): MutationPromise<SyncItemBatchData, SyncItemBatchVariables>;
export function syncItemBatch(dc: DataConnect, vars: SyncItemBatchVariables): MutationPromise<SyncItemBatchData, SyncItemBatchVariables>;

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

/* Allow users to create refs without passing in DataConnect */
export function getStoresPaginatedRef(): QueryRef<GetStoresPaginatedData, undefined>;
/* Allow users to pass in custom DataConnect instances */
export function getStoresPaginatedRef(dc: DataConnect): QueryRef<GetStoresPaginatedData, undefined>;

export function getStoresPaginated(): QueryPromise<GetStoresPaginatedData, undefined>;
export function getStoresPaginated(dc: DataConnect): QueryPromise<GetStoresPaginatedData, undefined>;

/* Allow users to create refs without passing in DataConnect */
export function getUsersPaginatedRef(): QueryRef<GetUsersPaginatedData, undefined>;
/* Allow users to pass in custom DataConnect instances */
export function getUsersPaginatedRef(dc: DataConnect): QueryRef<GetUsersPaginatedData, undefined>;

export function getUsersPaginated(): QueryPromise<GetUsersPaginatedData, undefined>;
export function getUsersPaginated(dc: DataConnect): QueryPromise<GetUsersPaginatedData, undefined>;

/* Allow users to create refs without passing in DataConnect */
export function getGlobalSettingsRef(): QueryRef<GetGlobalSettingsData, undefined>;
/* Allow users to pass in custom DataConnect instances */
export function getGlobalSettingsRef(dc: DataConnect): QueryRef<GetGlobalSettingsData, undefined>;

export function getGlobalSettings(): QueryPromise<GetGlobalSettingsData, undefined>;
export function getGlobalSettings(dc: DataConnect): QueryPromise<GetGlobalSettingsData, undefined>;

/* Allow users to create refs without passing in DataConnect */
export function getAdminAuditLogsRef(): QueryRef<GetAdminAuditLogsData, undefined>;
/* Allow users to pass in custom DataConnect instances */
export function getAdminAuditLogsRef(dc: DataConnect): QueryRef<GetAdminAuditLogsData, undefined>;

export function getAdminAuditLogs(): QueryPromise<GetAdminAuditLogsData, undefined>;
export function getAdminAuditLogs(dc: DataConnect): QueryPromise<GetAdminAuditLogsData, undefined>;

/* Allow users to create refs without passing in DataConnect */
export function getAnnouncementsRef(): QueryRef<GetAnnouncementsData, undefined>;
/* Allow users to pass in custom DataConnect instances */
export function getAnnouncementsRef(dc: DataConnect): QueryRef<GetAnnouncementsData, undefined>;

export function getAnnouncements(): QueryPromise<GetAnnouncementsData, undefined>;
export function getAnnouncements(dc: DataConnect): QueryPromise<GetAnnouncementsData, undefined>;

/* Allow users to create refs without passing in DataConnect */
export function getPromoCodesRef(): QueryRef<GetPromoCodesData, undefined>;
/* Allow users to pass in custom DataConnect instances */
export function getPromoCodesRef(dc: DataConnect): QueryRef<GetPromoCodesData, undefined>;

export function getPromoCodes(): QueryPromise<GetPromoCodesData, undefined>;
export function getPromoCodes(dc: DataConnect): QueryPromise<GetPromoCodesData, undefined>;

/* Allow users to create refs without passing in DataConnect */
export function syncSuppliersRef(vars: SyncSuppliersVariables): QueryRef<SyncSuppliersData, SyncSuppliersVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncSuppliersRef(dc: DataConnect, vars: SyncSuppliersVariables): QueryRef<SyncSuppliersData, SyncSuppliersVariables>;

export function syncSuppliers(vars: SyncSuppliersVariables): QueryPromise<SyncSuppliersData, SyncSuppliersVariables>;
export function syncSuppliers(dc: DataConnect, vars: SyncSuppliersVariables): QueryPromise<SyncSuppliersData, SyncSuppliersVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncPurchasesRef(vars: SyncPurchasesVariables): QueryRef<SyncPurchasesData, SyncPurchasesVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncPurchasesRef(dc: DataConnect, vars: SyncPurchasesVariables): QueryRef<SyncPurchasesData, SyncPurchasesVariables>;

export function syncPurchases(vars: SyncPurchasesVariables): QueryPromise<SyncPurchasesData, SyncPurchasesVariables>;
export function syncPurchases(dc: DataConnect, vars: SyncPurchasesVariables): QueryPromise<SyncPurchasesData, SyncPurchasesVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncPurchaseItemsRef(vars: SyncPurchaseItemsVariables): QueryRef<SyncPurchaseItemsData, SyncPurchaseItemsVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncPurchaseItemsRef(dc: DataConnect, vars: SyncPurchaseItemsVariables): QueryRef<SyncPurchaseItemsData, SyncPurchaseItemsVariables>;

export function syncPurchaseItems(vars: SyncPurchaseItemsVariables): QueryPromise<SyncPurchaseItemsData, SyncPurchaseItemsVariables>;
export function syncPurchaseItems(dc: DataConnect, vars: SyncPurchaseItemsVariables): QueryPromise<SyncPurchaseItemsData, SyncPurchaseItemsVariables>;

/* Allow users to create refs without passing in DataConnect */
export function syncItemBatchesRef(vars: SyncItemBatchesVariables): QueryRef<SyncItemBatchesData, SyncItemBatchesVariables>;
/* Allow users to pass in custom DataConnect instances */
export function syncItemBatchesRef(dc: DataConnect, vars: SyncItemBatchesVariables): QueryRef<SyncItemBatchesData, SyncItemBatchesVariables>;

export function syncItemBatches(vars: SyncItemBatchesVariables): QueryPromise<SyncItemBatchesData, SyncItemBatchesVariables>;
export function syncItemBatches(dc: DataConnect, vars: SyncItemBatchesVariables): QueryPromise<SyncItemBatchesData, SyncItemBatchesVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getItemsCountRef(vars: GetItemsCountVariables): QueryRef<GetItemsCountData, GetItemsCountVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getItemsCountRef(dc: DataConnect, vars: GetItemsCountVariables): QueryRef<GetItemsCountData, GetItemsCountVariables>;

export function getItemsCount(vars: GetItemsCountVariables): QueryPromise<GetItemsCountData, GetItemsCountVariables>;
export function getItemsCount(dc: DataConnect, vars: GetItemsCountVariables): QueryPromise<GetItemsCountData, GetItemsCountVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getSalesCountRef(vars: GetSalesCountVariables): QueryRef<GetSalesCountData, GetSalesCountVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getSalesCountRef(dc: DataConnect, vars: GetSalesCountVariables): QueryRef<GetSalesCountData, GetSalesCountVariables>;

export function getSalesCount(vars: GetSalesCountVariables): QueryPromise<GetSalesCountData, GetSalesCountVariables>;
export function getSalesCount(dc: DataConnect, vars: GetSalesCountVariables): QueryPromise<GetSalesCountData, GetSalesCountVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getUdhaarEntriesCountRef(vars: GetUdhaarEntriesCountVariables): QueryRef<GetUdhaarEntriesCountData, GetUdhaarEntriesCountVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getUdhaarEntriesCountRef(dc: DataConnect, vars: GetUdhaarEntriesCountVariables): QueryRef<GetUdhaarEntriesCountData, GetUdhaarEntriesCountVariables>;

export function getUdhaarEntriesCount(vars: GetUdhaarEntriesCountVariables): QueryPromise<GetUdhaarEntriesCountData, GetUdhaarEntriesCountVariables>;
export function getUdhaarEntriesCount(dc: DataConnect, vars: GetUdhaarEntriesCountVariables): QueryPromise<GetUdhaarEntriesCountData, GetUdhaarEntriesCountVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getExpenseEntriesCountRef(vars: GetExpenseEntriesCountVariables): QueryRef<GetExpenseEntriesCountData, GetExpenseEntriesCountVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getExpenseEntriesCountRef(dc: DataConnect, vars: GetExpenseEntriesCountVariables): QueryRef<GetExpenseEntriesCountData, GetExpenseEntriesCountVariables>;

export function getExpenseEntriesCount(vars: GetExpenseEntriesCountVariables): QueryPromise<GetExpenseEntriesCountData, GetExpenseEntriesCountVariables>;
export function getExpenseEntriesCount(dc: DataConnect, vars: GetExpenseEntriesCountVariables): QueryPromise<GetExpenseEntriesCountData, GetExpenseEntriesCountVariables>;

/* Allow users to create refs without passing in DataConnect */
export function getStoreRef(vars: GetStoreVariables): QueryRef<GetStoreData, GetStoreVariables>;
/* Allow users to pass in custom DataConnect instances */
export function getStoreRef(dc: DataConnect, vars: GetStoreVariables): QueryRef<GetStoreData, GetStoreVariables>;

export function getStore(vars: GetStoreVariables): QueryPromise<GetStoreData, GetStoreVariables>;
export function getStore(dc: DataConnect, vars: GetStoreVariables): QueryPromise<GetStoreData, GetStoreVariables>;

