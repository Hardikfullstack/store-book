import { getSession } from '@/lib/session';

/**
 * E04-S2 — Unified permission check for web routes.
 * Role hierarchy:
 *   owner      — full access to all store operations, staff management
 *   manager    — full access except user management (add/delete users)
 *   staff      — can view Dashboard, Sales, Items only. Reports, Settings, Udhaar hidden.
 *   admin/super_admin — platform-level administration (separate nav tree)
 */

export type AllowedRole = 'cashier' | 'manager' | 'owner' | 'super_admin' | 'admin';

// Map DB role strings to our normalized types
const NORMALIZE_ROLE: Record<string, string> = {
  staff: 'staff',
  cashier: 'cashier',
  manager: 'manager',
  owner: 'owner',
  admin: 'admin',
  super_admin: 'super_admin',
};

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

    case 'manager': // All store views except user management
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
      };

    case 'owner': // Full access
      return {
        canViewDashboard: true,
        canViewSales: true,
        canViewItems: true,
        canViewUdhaar: true,
        canViewExpenses: true,
        canViewReports: true,
        canViewSettings: true,
        canManageStaff: true,
        canManageUsers: false, // Only platform admins manage users
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

    default: // Unknown role — treat as cashier (least privilege)
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

/**
 * Check if the current session is allowed to access resource `key`.
 * Returns null (allowed) or { redirectUrl, message } if denied.
 */
export async function assertAccess(key: keyof PermissionSet): Promise<{ redirectUrl?: string; message?: string } | null> {
  const session = await getSession();
  if (!session) return { redirectUrl: '/login', message: 'Please log in to continue.' };

  const perms = resolvePermissions(session.role ?? 'staff');
  if (!perms[key]) {
    return { redirectUrl: '/', message: `Insufficient permissions for this section.` };
  }
  return null;
}

/** For server components — quick boolean test */
export async function hasPermission(key: keyof PermissionSet): Promise<boolean> {
  const denied = await assertAccess(key);
  return denied === null;
}
