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
      };

    case 'manager': // All store views, no user management or admin
      return {
        canViewDashboard: true,
        canViewSales: true,
        canViewItems: true,
        canViewUdhaar: true,
        canViewExpenses: true,
        canViewReports: true,
        canViewSettings: false,
        canManageStaff: false, // Can view staff list but not add/delete
        canManageUsers: false,
        canAccessAdmin: false,
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
      };
  }
}

/** Quick boolean test for a specific permission key. */
export function hasRolePermission(role: string) {
  const perms = resolvePermissions(role);
  return (key: keyof PermissionSet) => perms[key];
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
