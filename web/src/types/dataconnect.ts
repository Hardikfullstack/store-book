/**
 * Shared types derived from the Firebase DataConnect SDK (web/src/dataconnect/index.d.ts).
 * These re-export the element types from query/mutation responses so we avoid duplicating
 * shape definitions and keep a single source of truth.
 */

// ---------------------------------------------------------------------------
// Item — element of SyncItemsData["items"] | GetActiveItemsData["items"]
// ---------------------------------------------------------------------------
export interface DcItem {
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
}

// ---------------------------------------------------------------------------
// Sale — element of SyncSalesData["sales"] | GetActiveSalesData["sales"]
// ---------------------------------------------------------------------------
export interface DcSale {
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
}

// ---------------------------------------------------------------------------
// SaleItem — element of SyncSaleItemsData["saleItemDetails"]
// ---------------------------------------------------------------------------
export interface DcSaleItem {
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
}

// ---------------------------------------------------------------------------
// UdhaarEntry — element of SyncUdhaarsData["udhaarEntries"]
// ---------------------------------------------------------------------------
export interface DcUdhaarEntry {
  id: string;
  storeId: string;
  customerName: string;
  amount: number;
  type: string;
  timestamp: number;
  notes?: string | null;
  saleId?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

// ---------------------------------------------------------------------------
// PurchaseItem — element of SyncPurchaseItemsData["purchaseItemDetails"]
// ---------------------------------------------------------------------------
export interface DcPurchaseItem {
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

// ---------------------------------------------------------------------------
// Purchase — element of ListPurchasesData["purchases"]
// ---------------------------------------------------------------------------
export interface DcPurchase {
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
  purchaseItemDetails_on_purchase?: DcPurchaseItem[];
}

// ---------------------------------------------------------------------------
// User — element of GetUsersPaginatedData["users"] / GetUserData["user"]
// ---------------------------------------------------------------------------
export interface DcUser {
  id: string;
  phoneNumber?: string | null;
  username?: string | null;
  role: string;
  createdAt?: number;
  stores?: string[] | null;
  storeId?: string | null;
  subscriptionPlan?: string | null;
  subscriptionStatus?: string | null;
}

// ---------------------------------------------------------------------------
// Store — element of GetStoresPaginatedData["stores"]
// ---------------------------------------------------------------------------
export interface DcStore {
  id: string;
  name?: string | null;
  isActive?: boolean | null;
  isPremium?: boolean | null;
  subscriptionPlatform?: string | null;
  subscriptionStatus?: string | null;
  subscriptionExpiresAt?: number | null;
}

// ---------------------------------------------------------------------------
// Supplier — element of SyncSuppliersData["suppliers"]
// ---------------------------------------------------------------------------
export interface DcSupplier {
  id: string;
  storeId: string;
  name: string;
  phone?: string | null;
  gstin?: string | null;
  address?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

// ---------------------------------------------------------------------------
// AuditLog — element of GetAdminAuditLogsData["adminAuditLogs"]
// ---------------------------------------------------------------------------
export interface DcAuditLog {
  id: string;
  adminId: string;
  adminUsername?: string | null;
  action: string;
  targetId?: string | null;
  details?: string | null;
  timestamp: number;
}

// ---------------------------------------------------------------------------
// GlobalSetting — element of GetGlobalSettingsData["globalSettings"]
// ---------------------------------------------------------------------------
export interface DcGlobalSetting {
  id: string;
  key: string;
  value: string;
  description?: string | null;
  updatedAt: number;
  updatedBy?: string | null;
}

// ---------------------------------------------------------------------------
// PromoCode — element of GetPromoCodesData["promoCodes"]
// ---------------------------------------------------------------------------
export interface DcPromoCode {
  id: string;
  code: string;
  discountPercent?: number | null;
  discountAmount?: number | null;
  maxUses?: number | null;
  currentUses?: number | null;
  expiresAt?: number | null;
  isActive: boolean;
}

// ---------------------------------------------------------------------------
// Announcement — element of GetAnnouncementsData["announcements"]
// ---------------------------------------------------------------------------
export interface DcAnnouncement {
  id: string;
  title: string;
  message: string;
  type: string;
  isActive: boolean;
  createdAt: number;
}

// ---------------------------------------------------------------------------
// ExpenseEntry — element of SyncExpensesData["expenseEntries"]
// ---------------------------------------------------------------------------
export interface DcExpenseEntry {
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

// ---------------------------------------------------------------------------
// Chart datum used by DashboardCharts aggregations
// ---------------------------------------------------------------------------
export interface ChartDatum {
  name: string;
  value: number;
}
