/**
 * E04-S2 — Pure role→permission matrix (no server imports).
 * Safe to import in both client components ('use client') and server pages.
 */

export type AllowedRole = 'cashier' | 'manager' | 'owner' | 'super_admin' | 'admin';

export interface PermissionSet {
  canViewDashboard: boolean;
  canViewSales: boolean;
  canViewItems: boolean;
  canViewUdhaar: boolean;
  canViewExpenses: boolean;
  canViewReports: boolean;
  canViewSettings: boolean;
  canManageStaff: boolean;
  canManageUsers: boolean;       // platform-level user CRUD
  canAccessAdmin: boolean;
  canModifyInventory: boolean;
  canDeleteRecords: boolean;
  canApplyDiscount: boolean;
  canViewProfit: boolean;
  maxDiscountPercent: number;
}

// Map database role strings → normalized roles
const NORMALIZE_ROLE: Record<string, string> = {
  staff: 'staff',
  cashier: 'cashier',
  manager: 'manager',
  owner: 'owner',
  admin: 'admin',
  super_admin: 'super_admin',
};

/**
 * Returns a PermissionSet for the given role. Pure function — no side effects, no imports beyond this file.
 */
export function resolvePermissions(role: string): PermissionSet {
  const normalized = NORMALIZE_ROLE[role] ?? role;

  switch (normalized) {
    case 'admin':
    case 'super_admin':
      return {
        canViewDashboard: true,
        canViewSales: true,
        canViewItems: true,
        canViewUdhaar: true,
        canViewExpenses: true,
        canViewReports: true,
        canViewSettings: true,
        canManageStaff: true,
        canManageUsers: true,
        canAccessAdmin: true,
        canModifyInventory: true,
        canDeleteRecords: true,
        canApplyDiscount: true,
        canViewProfit: true,
        maxDiscountPercent: 100,
      };

    case 'owner': // Full store access — no platform user management
      return {
        canViewDashboard: true,
        canViewSales: true,
        canViewItems: true,
        canViewUdhaar: true,
        canViewExpenses: true,
        canViewReports: true,
        canViewSettings: true,
        canManageStaff: true,
        canManageUsers: false,
        canAccessAdmin: false,
        canModifyInventory: true,
        canDeleteRecords: true,
        canApplyDiscount: true,
        canViewProfit: true,
        maxDiscountPercent: 100,
      };

    case 'manager': // All store views, settings, limited delete/discount
      return {
        canViewDashboard: true,
        canViewSales: true,
        canViewItems: true,
        canViewUdhaar: true,
        canViewExpenses: true,
        canViewReports: true,
        canViewSettings: true,
        canManageStaff: false, // Can view staff list but not add/delete
        canManageUsers: false,
        canAccessAdmin: false,
        canModifyInventory: true,
        canDeleteRecords: true,
        canApplyDiscount: true,
        canViewProfit: true,
        maxDiscountPercent: 25,
      };

    case 'staff':
    case 'cashier': // Restricted — Dashboard + Sales + Items only
      return {
        canViewDashboard: true,
        canViewSales: true,
        canViewItems: true,
        canViewUdhaar: false,
        canViewExpenses: false,
        canViewReports: false,
        canViewSettings: false,
        canManageStaff: false,
        canManageUsers: false,
        canAccessAdmin: false,
        canModifyInventory: true,
        canDeleteRecords: false,
        canApplyDiscount: true,
        canViewProfit: false,
        maxDiscountPercent: 10,
      };

    default: // Unknown role → least privilege (cashier)
      return {
        canViewDashboard: true,
        canViewSales: true,
        canViewItems: true,
        canViewUdhaar: false,
        canViewExpenses: false,
        canViewReports: false,
        canViewSettings: false,
        canManageStaff: false,
        canManageUsers: false,
        canAccessAdmin: false,
        canModifyInventory: true,
        canDeleteRecords: false,
        canApplyDiscount: true,
        canViewProfit: false,
        maxDiscountPercent: 10,
      };
  }
}

/** Quick boolean test for a specific permission key. */
export function hasRolePermission(role: string) {
  const perms = resolvePermissions(role);
  return (key: keyof PermissionSet) => perms[key];
}

/**
 * Merge per-user overrides on top of role defaults.
 * E28-S1: replaces standalone canViewProfit/canDelete flags with matrix-based approach.
 */
export function mergePermissions(
  role: string,
  overrides?: Partial<PermissionSet>,
): PermissionSet {
  return { ...resolvePermissions(role), ...overrides };
}

/** Readable label for the role. */
export const ROLE_LABELS: Record<string, string> = {
  owner: 'Owner',
  manager: 'Manager',
  staff: 'Cashier',
  cashier: 'Cashier',
  admin: 'Platform Admin',
  super_admin: 'Platform Admin',
 };
