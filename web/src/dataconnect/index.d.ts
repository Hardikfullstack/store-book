import { ConnectorConfig, DataConnect, QueryRef, QueryPromise, ExecuteQueryOptions, MutationRef, MutationPromise } from 'firebase/data-connect';

export const connectorConfig: ConnectorConfig;

export type TimestampString = string;
export type UUIDString = string;
export type Int64String = string;
export type DateString = string;


export enum OrderDirection {
  ASC = "ASC",
  DESC = "DESC",
};



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
  minAmount?: number | null;
  maxAmount?: number | null;
  startDate?: number | null;
  endDate?: number | null;
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
  minPrice?: number | null;
  maxPrice?: number | null;
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
  minAmount?: number | null;
  maxAmount?: number | null;
  startDate?: number | null;
  endDate?: number | null;
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
  minAmount?: number | null;
  maxAmount?: number | null;
  startDate?: number | null;
  endDate?: number | null;
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

export interface GetStockAdjustmentsCountData {
  stockAdjustments: ({
    id: string;
  } & StockAdjustment_Key)[];
}

export interface GetStockAdjustmentsCountVariables {
  storeId: string;
}

export interface GetStockAdjustmentsData {
  stockAdjustments: ({
    id: string;
    storeId: string;
    itemId: string;
    itemName: string;
    reason: string;
    delta: number;
    timestamp: number;
    isDeleted: boolean;
    updatedAt: number;
  } & StockAdjustment_Key)[];
}

export interface GetStockAdjustmentsVariables {
  storeId: string;
  limit?: number | null;
  offset?: number | null;
  searchTerm?: string | null;
  reason?: string | null;
  startDate?: number | null;
  endDate?: number | null;
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

export interface StockAdjustment_Key {
  id: string;
  __typename?: 'StockAdjustment_Key';
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

export interface SyncStockAdjustmentData {
  stockAdjustment_upsert: StockAdjustment_Key;
}

export interface SyncStockAdjustmentVariables {
  id: string;
  storeId: string;
  itemId: string;
  itemName: string;
  reason: string;
  delta: number;
  timestamp: number;
  isDeleted: boolean;
  updatedAt: number;
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

interface SyncItemRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncItemVariables): MutationRef<SyncItemData, SyncItemVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncItemVariables): MutationRef<SyncItemData, SyncItemVariables>;
  operationName: string;
}
export const syncItemRef: SyncItemRef;

export function syncItem(vars: SyncItemVariables): MutationPromise<SyncItemData, SyncItemVariables>;
export function syncItem(dc: DataConnect, vars: SyncItemVariables): MutationPromise<SyncItemData, SyncItemVariables>;

interface SyncSaleRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncSaleVariables): MutationRef<SyncSaleData, SyncSaleVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncSaleVariables): MutationRef<SyncSaleData, SyncSaleVariables>;
  operationName: string;
}
export const syncSaleRef: SyncSaleRef;

export function syncSale(vars: SyncSaleVariables): MutationPromise<SyncSaleData, SyncSaleVariables>;
export function syncSale(dc: DataConnect, vars: SyncSaleVariables): MutationPromise<SyncSaleData, SyncSaleVariables>;

interface SyncSaleItemRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncSaleItemVariables): MutationRef<SyncSaleItemData, SyncSaleItemVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncSaleItemVariables): MutationRef<SyncSaleItemData, SyncSaleItemVariables>;
  operationName: string;
}
export const syncSaleItemRef: SyncSaleItemRef;

export function syncSaleItem(vars: SyncSaleItemVariables): MutationPromise<SyncSaleItemData, SyncSaleItemVariables>;
export function syncSaleItem(dc: DataConnect, vars: SyncSaleItemVariables): MutationPromise<SyncSaleItemData, SyncSaleItemVariables>;

interface SoftDeleteItemRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SoftDeleteItemVariables): MutationRef<SoftDeleteItemData, SoftDeleteItemVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SoftDeleteItemVariables): MutationRef<SoftDeleteItemData, SoftDeleteItemVariables>;
  operationName: string;
}
export const softDeleteItemRef: SoftDeleteItemRef;

export function softDeleteItem(vars: SoftDeleteItemVariables): MutationPromise<SoftDeleteItemData, SoftDeleteItemVariables>;
export function softDeleteItem(dc: DataConnect, vars: SoftDeleteItemVariables): MutationPromise<SoftDeleteItemData, SoftDeleteItemVariables>;

interface SoftDeleteSaleRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SoftDeleteSaleVariables): MutationRef<SoftDeleteSaleData, SoftDeleteSaleVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SoftDeleteSaleVariables): MutationRef<SoftDeleteSaleData, SoftDeleteSaleVariables>;
  operationName: string;
}
export const softDeleteSaleRef: SoftDeleteSaleRef;

export function softDeleteSale(vars: SoftDeleteSaleVariables): MutationPromise<SoftDeleteSaleData, SoftDeleteSaleVariables>;
export function softDeleteSale(dc: DataConnect, vars: SoftDeleteSaleVariables): MutationPromise<SoftDeleteSaleData, SoftDeleteSaleVariables>;

interface SyncUserRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncUserVariables): MutationRef<SyncUserData, SyncUserVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncUserVariables): MutationRef<SyncUserData, SyncUserVariables>;
  operationName: string;
}
export const syncUserRef: SyncUserRef;

export function syncUser(vars: SyncUserVariables): MutationPromise<SyncUserData, SyncUserVariables>;
export function syncUser(dc: DataConnect, vars: SyncUserVariables): MutationPromise<SyncUserData, SyncUserVariables>;

interface UpdateUserRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: UpdateUserVariables): MutationRef<UpdateUserData, UpdateUserVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: UpdateUserVariables): MutationRef<UpdateUserData, UpdateUserVariables>;
  operationName: string;
}
export const updateUserRef: UpdateUserRef;

export function updateUser(vars: UpdateUserVariables): MutationPromise<UpdateUserData, UpdateUserVariables>;
export function updateUser(dc: DataConnect, vars: UpdateUserVariables): MutationPromise<UpdateUserData, UpdateUserVariables>;

interface SyncStoreRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncStoreVariables): MutationRef<SyncStoreData, SyncStoreVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncStoreVariables): MutationRef<SyncStoreData, SyncStoreVariables>;
  operationName: string;
}
export const syncStoreRef: SyncStoreRef;

export function syncStore(vars: SyncStoreVariables): MutationPromise<SyncStoreData, SyncStoreVariables>;
export function syncStore(dc: DataConnect, vars: SyncStoreVariables): MutationPromise<SyncStoreData, SyncStoreVariables>;

interface UpdateStoreRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: UpdateStoreVariables): MutationRef<UpdateStoreData, UpdateStoreVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: UpdateStoreVariables): MutationRef<UpdateStoreData, UpdateStoreVariables>;
  operationName: string;
}
export const updateStoreRef: UpdateStoreRef;

export function updateStore(vars: UpdateStoreVariables): MutationPromise<UpdateStoreData, UpdateStoreVariables>;
export function updateStore(dc: DataConnect, vars: UpdateStoreVariables): MutationPromise<UpdateStoreData, UpdateStoreVariables>;

interface SyncUdhaarRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncUdhaarVariables): MutationRef<SyncUdhaarData, SyncUdhaarVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncUdhaarVariables): MutationRef<SyncUdhaarData, SyncUdhaarVariables>;
  operationName: string;
}
export const syncUdhaarRef: SyncUdhaarRef;

export function syncUdhaar(vars: SyncUdhaarVariables): MutationPromise<SyncUdhaarData, SyncUdhaarVariables>;
export function syncUdhaar(dc: DataConnect, vars: SyncUdhaarVariables): MutationPromise<SyncUdhaarData, SyncUdhaarVariables>;

interface SoftDeleteUdhaarRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SoftDeleteUdhaarVariables): MutationRef<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SoftDeleteUdhaarVariables): MutationRef<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;
  operationName: string;
}
export const softDeleteUdhaarRef: SoftDeleteUdhaarRef;

export function softDeleteUdhaar(vars: SoftDeleteUdhaarVariables): MutationPromise<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;
export function softDeleteUdhaar(dc: DataConnect, vars: SoftDeleteUdhaarVariables): MutationPromise<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;

interface SyncExpenseRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncExpenseVariables): MutationRef<SyncExpenseData, SyncExpenseVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncExpenseVariables): MutationRef<SyncExpenseData, SyncExpenseVariables>;
  operationName: string;
}
export const syncExpenseRef: SyncExpenseRef;

export function syncExpense(vars: SyncExpenseVariables): MutationPromise<SyncExpenseData, SyncExpenseVariables>;
export function syncExpense(dc: DataConnect, vars: SyncExpenseVariables): MutationPromise<SyncExpenseData, SyncExpenseVariables>;

interface SoftDeleteExpenseRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SoftDeleteExpenseVariables): MutationRef<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SoftDeleteExpenseVariables): MutationRef<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;
  operationName: string;
}
export const softDeleteExpenseRef: SoftDeleteExpenseRef;

export function softDeleteExpense(vars: SoftDeleteExpenseVariables): MutationPromise<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;
export function softDeleteExpense(dc: DataConnect, vars: SoftDeleteExpenseVariables): MutationPromise<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;

interface SyncSupplierRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncSupplierVariables): MutationRef<SyncSupplierData, SyncSupplierVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncSupplierVariables): MutationRef<SyncSupplierData, SyncSupplierVariables>;
  operationName: string;
}
export const syncSupplierRef: SyncSupplierRef;

export function syncSupplier(vars: SyncSupplierVariables): MutationPromise<SyncSupplierData, SyncSupplierVariables>;
export function syncSupplier(dc: DataConnect, vars: SyncSupplierVariables): MutationPromise<SyncSupplierData, SyncSupplierVariables>;

interface UpsertGlobalSettingRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: UpsertGlobalSettingVariables): MutationRef<UpsertGlobalSettingData, UpsertGlobalSettingVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: UpsertGlobalSettingVariables): MutationRef<UpsertGlobalSettingData, UpsertGlobalSettingVariables>;
  operationName: string;
}
export const upsertGlobalSettingRef: UpsertGlobalSettingRef;

export function upsertGlobalSetting(vars: UpsertGlobalSettingVariables): MutationPromise<UpsertGlobalSettingData, UpsertGlobalSettingVariables>;
export function upsertGlobalSetting(dc: DataConnect, vars: UpsertGlobalSettingVariables): MutationPromise<UpsertGlobalSettingData, UpsertGlobalSettingVariables>;

interface CreateAdminAuditLogRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: CreateAdminAuditLogVariables): MutationRef<CreateAdminAuditLogData, CreateAdminAuditLogVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: CreateAdminAuditLogVariables): MutationRef<CreateAdminAuditLogData, CreateAdminAuditLogVariables>;
  operationName: string;
}
export const createAdminAuditLogRef: CreateAdminAuditLogRef;

export function createAdminAuditLog(vars: CreateAdminAuditLogVariables): MutationPromise<CreateAdminAuditLogData, CreateAdminAuditLogVariables>;
export function createAdminAuditLog(dc: DataConnect, vars: CreateAdminAuditLogVariables): MutationPromise<CreateAdminAuditLogData, CreateAdminAuditLogVariables>;

interface UpsertAnnouncementRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: UpsertAnnouncementVariables): MutationRef<UpsertAnnouncementData, UpsertAnnouncementVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: UpsertAnnouncementVariables): MutationRef<UpsertAnnouncementData, UpsertAnnouncementVariables>;
  operationName: string;
}
export const upsertAnnouncementRef: UpsertAnnouncementRef;

export function upsertAnnouncement(vars: UpsertAnnouncementVariables): MutationPromise<UpsertAnnouncementData, UpsertAnnouncementVariables>;
export function upsertAnnouncement(dc: DataConnect, vars: UpsertAnnouncementVariables): MutationPromise<UpsertAnnouncementData, UpsertAnnouncementVariables>;

interface DeleteAnnouncementRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: DeleteAnnouncementVariables): MutationRef<DeleteAnnouncementData, DeleteAnnouncementVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: DeleteAnnouncementVariables): MutationRef<DeleteAnnouncementData, DeleteAnnouncementVariables>;
  operationName: string;
}
export const deleteAnnouncementRef: DeleteAnnouncementRef;

export function deleteAnnouncement(vars: DeleteAnnouncementVariables): MutationPromise<DeleteAnnouncementData, DeleteAnnouncementVariables>;
export function deleteAnnouncement(dc: DataConnect, vars: DeleteAnnouncementVariables): MutationPromise<DeleteAnnouncementData, DeleteAnnouncementVariables>;

interface UpsertPromoCodeRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: UpsertPromoCodeVariables): MutationRef<UpsertPromoCodeData, UpsertPromoCodeVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: UpsertPromoCodeVariables): MutationRef<UpsertPromoCodeData, UpsertPromoCodeVariables>;
  operationName: string;
}
export const upsertPromoCodeRef: UpsertPromoCodeRef;

export function upsertPromoCode(vars: UpsertPromoCodeVariables): MutationPromise<UpsertPromoCodeData, UpsertPromoCodeVariables>;
export function upsertPromoCode(dc: DataConnect, vars: UpsertPromoCodeVariables): MutationPromise<UpsertPromoCodeData, UpsertPromoCodeVariables>;

interface DeletePromoCodeRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: DeletePromoCodeVariables): MutationRef<DeletePromoCodeData, DeletePromoCodeVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: DeletePromoCodeVariables): MutationRef<DeletePromoCodeData, DeletePromoCodeVariables>;
  operationName: string;
}
export const deletePromoCodeRef: DeletePromoCodeRef;

export function deletePromoCode(vars: DeletePromoCodeVariables): MutationPromise<DeletePromoCodeData, DeletePromoCodeVariables>;
export function deletePromoCode(dc: DataConnect, vars: DeletePromoCodeVariables): MutationPromise<DeletePromoCodeData, DeletePromoCodeVariables>;

interface ToggleStoreStatusRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: ToggleStoreStatusVariables): MutationRef<ToggleStoreStatusData, ToggleStoreStatusVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: ToggleStoreStatusVariables): MutationRef<ToggleStoreStatusData, ToggleStoreStatusVariables>;
  operationName: string;
}
export const toggleStoreStatusRef: ToggleStoreStatusRef;

export function toggleStoreStatus(vars: ToggleStoreStatusVariables): MutationPromise<ToggleStoreStatusData, ToggleStoreStatusVariables>;
export function toggleStoreStatus(dc: DataConnect, vars: ToggleStoreStatusVariables): MutationPromise<ToggleStoreStatusData, ToggleStoreStatusVariables>;

interface PurgeStoreRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: PurgeStoreVariables): MutationRef<PurgeStoreData, PurgeStoreVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: PurgeStoreVariables): MutationRef<PurgeStoreData, PurgeStoreVariables>;
  operationName: string;
}
export const purgeStoreRef: PurgeStoreRef;

export function purgeStore(vars: PurgeStoreVariables): MutationPromise<PurgeStoreData, PurgeStoreVariables>;
export function purgeStore(dc: DataConnect, vars: PurgeStoreVariables): MutationPromise<PurgeStoreData, PurgeStoreVariables>;

interface CreateUserRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: CreateUserVariables): MutationRef<CreateUserData, CreateUserVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: CreateUserVariables): MutationRef<CreateUserData, CreateUserVariables>;
  operationName: string;
}
export const createUserRef: CreateUserRef;

export function createUser(vars: CreateUserVariables): MutationPromise<CreateUserData, CreateUserVariables>;
export function createUser(dc: DataConnect, vars: CreateUserVariables): MutationPromise<CreateUserData, CreateUserVariables>;

interface SyncPurchaseRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncPurchaseVariables): MutationRef<SyncPurchaseData, SyncPurchaseVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncPurchaseVariables): MutationRef<SyncPurchaseData, SyncPurchaseVariables>;
  operationName: string;
}
export const syncPurchaseRef: SyncPurchaseRef;

export function syncPurchase(vars: SyncPurchaseVariables): MutationPromise<SyncPurchaseData, SyncPurchaseVariables>;
export function syncPurchase(dc: DataConnect, vars: SyncPurchaseVariables): MutationPromise<SyncPurchaseData, SyncPurchaseVariables>;

interface SyncPurchaseItemRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncPurchaseItemVariables): MutationRef<SyncPurchaseItemData, SyncPurchaseItemVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncPurchaseItemVariables): MutationRef<SyncPurchaseItemData, SyncPurchaseItemVariables>;
  operationName: string;
}
export const syncPurchaseItemRef: SyncPurchaseItemRef;

export function syncPurchaseItem(vars: SyncPurchaseItemVariables): MutationPromise<SyncPurchaseItemData, SyncPurchaseItemVariables>;
export function syncPurchaseItem(dc: DataConnect, vars: SyncPurchaseItemVariables): MutationPromise<SyncPurchaseItemData, SyncPurchaseItemVariables>;

interface SyncItemBatchRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncItemBatchVariables): MutationRef<SyncItemBatchData, SyncItemBatchVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncItemBatchVariables): MutationRef<SyncItemBatchData, SyncItemBatchVariables>;
  operationName: string;
}
export const syncItemBatchRef: SyncItemBatchRef;

export function syncItemBatch(vars: SyncItemBatchVariables): MutationPromise<SyncItemBatchData, SyncItemBatchVariables>;
export function syncItemBatch(dc: DataConnect, vars: SyncItemBatchVariables): MutationPromise<SyncItemBatchData, SyncItemBatchVariables>;

interface SyncStockAdjustmentRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncStockAdjustmentVariables): MutationRef<SyncStockAdjustmentData, SyncStockAdjustmentVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncStockAdjustmentVariables): MutationRef<SyncStockAdjustmentData, SyncStockAdjustmentVariables>;
  operationName: string;
}
export const syncStockAdjustmentRef: SyncStockAdjustmentRef;

export function syncStockAdjustment(vars: SyncStockAdjustmentVariables): MutationPromise<SyncStockAdjustmentData, SyncStockAdjustmentVariables>;
export function syncStockAdjustment(dc: DataConnect, vars: SyncStockAdjustmentVariables): MutationPromise<SyncStockAdjustmentData, SyncStockAdjustmentVariables>;

interface SyncItemsRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncItemsVariables): QueryRef<SyncItemsData, SyncItemsVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncItemsVariables): QueryRef<SyncItemsData, SyncItemsVariables>;
  operationName: string;
}
export const syncItemsRef: SyncItemsRef;

export function syncItems(vars: SyncItemsVariables, options?: ExecuteQueryOptions): QueryPromise<SyncItemsData, SyncItemsVariables>;
export function syncItems(dc: DataConnect, vars: SyncItemsVariables, options?: ExecuteQueryOptions): QueryPromise<SyncItemsData, SyncItemsVariables>;

interface SyncSalesRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncSalesVariables): QueryRef<SyncSalesData, SyncSalesVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncSalesVariables): QueryRef<SyncSalesData, SyncSalesVariables>;
  operationName: string;
}
export const syncSalesRef: SyncSalesRef;

export function syncSales(vars: SyncSalesVariables, options?: ExecuteQueryOptions): QueryPromise<SyncSalesData, SyncSalesVariables>;
export function syncSales(dc: DataConnect, vars: SyncSalesVariables, options?: ExecuteQueryOptions): QueryPromise<SyncSalesData, SyncSalesVariables>;

interface SyncSaleItemsRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncSaleItemsVariables): QueryRef<SyncSaleItemsData, SyncSaleItemsVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncSaleItemsVariables): QueryRef<SyncSaleItemsData, SyncSaleItemsVariables>;
  operationName: string;
}
export const syncSaleItemsRef: SyncSaleItemsRef;

export function syncSaleItems(vars: SyncSaleItemsVariables, options?: ExecuteQueryOptions): QueryPromise<SyncSaleItemsData, SyncSaleItemsVariables>;
export function syncSaleItems(dc: DataConnect, vars: SyncSaleItemsVariables, options?: ExecuteQueryOptions): QueryPromise<SyncSaleItemsData, SyncSaleItemsVariables>;

interface GetActiveItemsRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetActiveItemsVariables): QueryRef<GetActiveItemsData, GetActiveItemsVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetActiveItemsVariables): QueryRef<GetActiveItemsData, GetActiveItemsVariables>;
  operationName: string;
}
export const getActiveItemsRef: GetActiveItemsRef;

export function getActiveItems(vars: GetActiveItemsVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveItemsData, GetActiveItemsVariables>;
export function getActiveItems(dc: DataConnect, vars: GetActiveItemsVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveItemsData, GetActiveItemsVariables>;

interface SyncUdhaarsRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncUdhaarsVariables): QueryRef<SyncUdhaarsData, SyncUdhaarsVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncUdhaarsVariables): QueryRef<SyncUdhaarsData, SyncUdhaarsVariables>;
  operationName: string;
}
export const syncUdhaarsRef: SyncUdhaarsRef;

export function syncUdhaars(vars: SyncUdhaarsVariables, options?: ExecuteQueryOptions): QueryPromise<SyncUdhaarsData, SyncUdhaarsVariables>;
export function syncUdhaars(dc: DataConnect, vars: SyncUdhaarsVariables, options?: ExecuteQueryOptions): QueryPromise<SyncUdhaarsData, SyncUdhaarsVariables>;

interface SyncExpensesRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncExpensesVariables): QueryRef<SyncExpensesData, SyncExpensesVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncExpensesVariables): QueryRef<SyncExpensesData, SyncExpensesVariables>;
  operationName: string;
}
export const syncExpensesRef: SyncExpensesRef;

export function syncExpenses(vars: SyncExpensesVariables, options?: ExecuteQueryOptions): QueryPromise<SyncExpensesData, SyncExpensesVariables>;
export function syncExpenses(dc: DataConnect, vars: SyncExpensesVariables, options?: ExecuteQueryOptions): QueryPromise<SyncExpensesData, SyncExpensesVariables>;

interface GetActiveSalesRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetActiveSalesVariables): QueryRef<GetActiveSalesData, GetActiveSalesVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetActiveSalesVariables): QueryRef<GetActiveSalesData, GetActiveSalesVariables>;
  operationName: string;
}
export const getActiveSalesRef: GetActiveSalesRef;

export function getActiveSales(vars: GetActiveSalesVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveSalesData, GetActiveSalesVariables>;
export function getActiveSales(dc: DataConnect, vars: GetActiveSalesVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveSalesData, GetActiveSalesVariables>;

interface GetActiveSaleItemsRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetActiveSaleItemsVariables): QueryRef<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetActiveSaleItemsVariables): QueryRef<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;
  operationName: string;
}
export const getActiveSaleItemsRef: GetActiveSaleItemsRef;

export function getActiveSaleItems(vars: GetActiveSaleItemsVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;
export function getActiveSaleItems(dc: DataConnect, vars: GetActiveSaleItemsVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;

interface GetActiveUdhaarsRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetActiveUdhaarsVariables): QueryRef<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetActiveUdhaarsVariables): QueryRef<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;
  operationName: string;
}
export const getActiveUdhaarsRef: GetActiveUdhaarsRef;

export function getActiveUdhaars(vars: GetActiveUdhaarsVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;
export function getActiveUdhaars(dc: DataConnect, vars: GetActiveUdhaarsVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;

interface GetActiveExpensesRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetActiveExpensesVariables): QueryRef<GetActiveExpensesData, GetActiveExpensesVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetActiveExpensesVariables): QueryRef<GetActiveExpensesData, GetActiveExpensesVariables>;
  operationName: string;
}
export const getActiveExpensesRef: GetActiveExpensesRef;

export function getActiveExpenses(vars: GetActiveExpensesVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveExpensesData, GetActiveExpensesVariables>;
export function getActiveExpenses(dc: DataConnect, vars: GetActiveExpensesVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveExpensesData, GetActiveExpensesVariables>;

interface GetActiveSuppliersRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetActiveSuppliersVariables): QueryRef<GetActiveSuppliersData, GetActiveSuppliersVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetActiveSuppliersVariables): QueryRef<GetActiveSuppliersData, GetActiveSuppliersVariables>;
  operationName: string;
}
export const getActiveSuppliersRef: GetActiveSuppliersRef;

export function getActiveSuppliers(vars: GetActiveSuppliersVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveSuppliersData, GetActiveSuppliersVariables>;
export function getActiveSuppliers(dc: DataConnect, vars: GetActiveSuppliersVariables, options?: ExecuteQueryOptions): QueryPromise<GetActiveSuppliersData, GetActiveSuppliersVariables>;

interface GetUserRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetUserVariables): QueryRef<GetUserData, GetUserVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetUserVariables): QueryRef<GetUserData, GetUserVariables>;
  operationName: string;
}
export const getUserRef: GetUserRef;

export function getUser(vars: GetUserVariables, options?: ExecuteQueryOptions): QueryPromise<GetUserData, GetUserVariables>;
export function getUser(dc: DataConnect, vars: GetUserVariables, options?: ExecuteQueryOptions): QueryPromise<GetUserData, GetUserVariables>;

interface GetStoresPaginatedRef {
  /* Allow users to create refs without passing in DataConnect */
  (): QueryRef<GetStoresPaginatedData, undefined>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect): QueryRef<GetStoresPaginatedData, undefined>;
  operationName: string;
}
export const getStoresPaginatedRef: GetStoresPaginatedRef;

export function getStoresPaginated(options?: ExecuteQueryOptions): QueryPromise<GetStoresPaginatedData, undefined>;
export function getStoresPaginated(dc: DataConnect, options?: ExecuteQueryOptions): QueryPromise<GetStoresPaginatedData, undefined>;

interface GetUsersPaginatedRef {
  /* Allow users to create refs without passing in DataConnect */
  (): QueryRef<GetUsersPaginatedData, undefined>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect): QueryRef<GetUsersPaginatedData, undefined>;
  operationName: string;
}
export const getUsersPaginatedRef: GetUsersPaginatedRef;

export function getUsersPaginated(options?: ExecuteQueryOptions): QueryPromise<GetUsersPaginatedData, undefined>;
export function getUsersPaginated(dc: DataConnect, options?: ExecuteQueryOptions): QueryPromise<GetUsersPaginatedData, undefined>;

interface GetGlobalSettingsRef {
  /* Allow users to create refs without passing in DataConnect */
  (): QueryRef<GetGlobalSettingsData, undefined>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect): QueryRef<GetGlobalSettingsData, undefined>;
  operationName: string;
}
export const getGlobalSettingsRef: GetGlobalSettingsRef;

export function getGlobalSettings(options?: ExecuteQueryOptions): QueryPromise<GetGlobalSettingsData, undefined>;
export function getGlobalSettings(dc: DataConnect, options?: ExecuteQueryOptions): QueryPromise<GetGlobalSettingsData, undefined>;

interface GetAdminAuditLogsRef {
  /* Allow users to create refs without passing in DataConnect */
  (): QueryRef<GetAdminAuditLogsData, undefined>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect): QueryRef<GetAdminAuditLogsData, undefined>;
  operationName: string;
}
export const getAdminAuditLogsRef: GetAdminAuditLogsRef;

export function getAdminAuditLogs(options?: ExecuteQueryOptions): QueryPromise<GetAdminAuditLogsData, undefined>;
export function getAdminAuditLogs(dc: DataConnect, options?: ExecuteQueryOptions): QueryPromise<GetAdminAuditLogsData, undefined>;

interface GetAnnouncementsRef {
  /* Allow users to create refs without passing in DataConnect */
  (): QueryRef<GetAnnouncementsData, undefined>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect): QueryRef<GetAnnouncementsData, undefined>;
  operationName: string;
}
export const getAnnouncementsRef: GetAnnouncementsRef;

export function getAnnouncements(options?: ExecuteQueryOptions): QueryPromise<GetAnnouncementsData, undefined>;
export function getAnnouncements(dc: DataConnect, options?: ExecuteQueryOptions): QueryPromise<GetAnnouncementsData, undefined>;

interface GetPromoCodesRef {
  /* Allow users to create refs without passing in DataConnect */
  (): QueryRef<GetPromoCodesData, undefined>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect): QueryRef<GetPromoCodesData, undefined>;
  operationName: string;
}
export const getPromoCodesRef: GetPromoCodesRef;

export function getPromoCodes(options?: ExecuteQueryOptions): QueryPromise<GetPromoCodesData, undefined>;
export function getPromoCodes(dc: DataConnect, options?: ExecuteQueryOptions): QueryPromise<GetPromoCodesData, undefined>;

interface SyncSuppliersRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncSuppliersVariables): QueryRef<SyncSuppliersData, SyncSuppliersVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncSuppliersVariables): QueryRef<SyncSuppliersData, SyncSuppliersVariables>;
  operationName: string;
}
export const syncSuppliersRef: SyncSuppliersRef;

export function syncSuppliers(vars: SyncSuppliersVariables, options?: ExecuteQueryOptions): QueryPromise<SyncSuppliersData, SyncSuppliersVariables>;
export function syncSuppliers(dc: DataConnect, vars: SyncSuppliersVariables, options?: ExecuteQueryOptions): QueryPromise<SyncSuppliersData, SyncSuppliersVariables>;

interface SyncPurchasesRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncPurchasesVariables): QueryRef<SyncPurchasesData, SyncPurchasesVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncPurchasesVariables): QueryRef<SyncPurchasesData, SyncPurchasesVariables>;
  operationName: string;
}
export const syncPurchasesRef: SyncPurchasesRef;

export function syncPurchases(vars: SyncPurchasesVariables, options?: ExecuteQueryOptions): QueryPromise<SyncPurchasesData, SyncPurchasesVariables>;
export function syncPurchases(dc: DataConnect, vars: SyncPurchasesVariables, options?: ExecuteQueryOptions): QueryPromise<SyncPurchasesData, SyncPurchasesVariables>;

interface SyncPurchaseItemsRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncPurchaseItemsVariables): QueryRef<SyncPurchaseItemsData, SyncPurchaseItemsVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncPurchaseItemsVariables): QueryRef<SyncPurchaseItemsData, SyncPurchaseItemsVariables>;
  operationName: string;
}
export const syncPurchaseItemsRef: SyncPurchaseItemsRef;

export function syncPurchaseItems(vars: SyncPurchaseItemsVariables, options?: ExecuteQueryOptions): QueryPromise<SyncPurchaseItemsData, SyncPurchaseItemsVariables>;
export function syncPurchaseItems(dc: DataConnect, vars: SyncPurchaseItemsVariables, options?: ExecuteQueryOptions): QueryPromise<SyncPurchaseItemsData, SyncPurchaseItemsVariables>;

interface SyncItemBatchesRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: SyncItemBatchesVariables): QueryRef<SyncItemBatchesData, SyncItemBatchesVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: SyncItemBatchesVariables): QueryRef<SyncItemBatchesData, SyncItemBatchesVariables>;
  operationName: string;
}
export const syncItemBatchesRef: SyncItemBatchesRef;

export function syncItemBatches(vars: SyncItemBatchesVariables, options?: ExecuteQueryOptions): QueryPromise<SyncItemBatchesData, SyncItemBatchesVariables>;
export function syncItemBatches(dc: DataConnect, vars: SyncItemBatchesVariables, options?: ExecuteQueryOptions): QueryPromise<SyncItemBatchesData, SyncItemBatchesVariables>;

interface GetItemsCountRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetItemsCountVariables): QueryRef<GetItemsCountData, GetItemsCountVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetItemsCountVariables): QueryRef<GetItemsCountData, GetItemsCountVariables>;
  operationName: string;
}
export const getItemsCountRef: GetItemsCountRef;

export function getItemsCount(vars: GetItemsCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetItemsCountData, GetItemsCountVariables>;
export function getItemsCount(dc: DataConnect, vars: GetItemsCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetItemsCountData, GetItemsCountVariables>;

interface GetSalesCountRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetSalesCountVariables): QueryRef<GetSalesCountData, GetSalesCountVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetSalesCountVariables): QueryRef<GetSalesCountData, GetSalesCountVariables>;
  operationName: string;
}
export const getSalesCountRef: GetSalesCountRef;

export function getSalesCount(vars: GetSalesCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetSalesCountData, GetSalesCountVariables>;
export function getSalesCount(dc: DataConnect, vars: GetSalesCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetSalesCountData, GetSalesCountVariables>;

interface GetUdhaarEntriesCountRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetUdhaarEntriesCountVariables): QueryRef<GetUdhaarEntriesCountData, GetUdhaarEntriesCountVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetUdhaarEntriesCountVariables): QueryRef<GetUdhaarEntriesCountData, GetUdhaarEntriesCountVariables>;
  operationName: string;
}
export const getUdhaarEntriesCountRef: GetUdhaarEntriesCountRef;

export function getUdhaarEntriesCount(vars: GetUdhaarEntriesCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetUdhaarEntriesCountData, GetUdhaarEntriesCountVariables>;
export function getUdhaarEntriesCount(dc: DataConnect, vars: GetUdhaarEntriesCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetUdhaarEntriesCountData, GetUdhaarEntriesCountVariables>;

interface GetExpenseEntriesCountRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetExpenseEntriesCountVariables): QueryRef<GetExpenseEntriesCountData, GetExpenseEntriesCountVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetExpenseEntriesCountVariables): QueryRef<GetExpenseEntriesCountData, GetExpenseEntriesCountVariables>;
  operationName: string;
}
export const getExpenseEntriesCountRef: GetExpenseEntriesCountRef;

export function getExpenseEntriesCount(vars: GetExpenseEntriesCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetExpenseEntriesCountData, GetExpenseEntriesCountVariables>;
export function getExpenseEntriesCount(dc: DataConnect, vars: GetExpenseEntriesCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetExpenseEntriesCountData, GetExpenseEntriesCountVariables>;

interface GetStoreRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetStoreVariables): QueryRef<GetStoreData, GetStoreVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetStoreVariables): QueryRef<GetStoreData, GetStoreVariables>;
  operationName: string;
}
export const getStoreRef: GetStoreRef;

export function getStore(vars: GetStoreVariables): QueryPromise<GetStoreData, GetStoreVariables>;
export function getStore(dc: DataConnect, vars: GetStoreVariables): QueryPromise<GetStoreData, GetStoreVariables>;

interface GetStockAdjustmentsRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetStockAdjustmentsVariables): QueryRef<GetStockAdjustmentsData, GetStockAdjustmentsVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetStockAdjustmentsVariables): QueryRef<GetStockAdjustmentsData, GetStockAdjustmentsVariables>;
  operationName: string;
}
export const getStockAdjustmentsRef: GetStockAdjustmentsRef;

export function getStockAdjustments(vars: GetStockAdjustmentsVariables, options?: ExecuteQueryOptions): QueryPromise<GetStockAdjustmentsData, GetStockAdjustmentsVariables>;
export function getStockAdjustments(dc: DataConnect, vars: GetStockAdjustmentsVariables, options?: ExecuteQueryOptions): QueryPromise<GetStockAdjustmentsData, GetStockAdjustmentsVariables>;

interface GetStockAdjustmentsCountRef {
  /* Allow users to create refs without passing in DataConnect */
  (vars: GetStockAdjustmentsCountVariables): QueryRef<GetStockAdjustmentsCountData, GetStockAdjustmentsCountVariables>;
  /* Allow users to pass in custom DataConnect instances */
  (dc: DataConnect, vars: GetStockAdjustmentsCountVariables): QueryRef<GetStockAdjustmentsCountData, GetStockAdjustmentsCountVariables>;
  operationName: string;
}
export const getStockAdjustmentsCountRef: GetStockAdjustmentsCountRef;

export function getStockAdjustmentsCount(vars: GetStockAdjustmentsCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetStockAdjustmentsCountData, GetStockAdjustmentsCountVariables>;
export function getStockAdjustmentsCount(dc: DataConnect, vars: GetStockAdjustmentsCountVariables, options?: ExecuteQueryOptions): QueryPromise<GetStockAdjustmentsCountData, GetStockAdjustmentsCountVariables>;



