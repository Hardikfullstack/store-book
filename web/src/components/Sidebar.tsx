'use client';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, Package, ShoppingCart, Users, Receipt, LogOut, Database, Store, Settings, BadgeCheck, LockKeyhole } from 'lucide-react';
import { ThemeToggle } from '@/components/ThemeToggle';
import { useState } from 'react';
import CreateStoreModal from '@/components/CreateStoreModal';
import { resolvePermissions, AllowedRole, PermissionSet } from '@/lib/permissions';

const ROLE_LABELS: Record<string, string> = {
  owner: 'Owner',
  manager: 'Manager',
  staff: 'Cashier',
  cashier: 'Cashier',
  admin: 'Admin',
  super_admin: 'Platform Admin',
};

// Route → Permission mapping used to build nav items dynamically
const ROUTE_PERMISSIONS = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard, permKey: 'canViewDashboard' },
  { name: 'Sales', path: '/sales', icon: ShoppingCart, permKey: 'canViewSales' },
  { name: 'Items', path: '/items', icon: Package, permKey: 'canViewItems' },
  { name: 'Quotations', path: '/quotations', icon: Receipt, permKey: 'canViewItems' }, // Quotations share Items permission
  { name: 'Udhaar', path: '/udhaar', icon: Users, permKey: 'canViewUdhaar' },
  { name: 'Expenses', path: '/expenses', icon: Receipt, permKey: 'canViewExpenses' },
  { name: 'Reports', path: '/reports', icon: Database, permKey: 'canViewReports' },
  { name: 'Settings', path: '/settings', icon: Settings, permKey: 'canViewSettings' },
];

const ADMIN_ROUTE_PERMISSIONS = [
  { name: 'Admin Dashboard', path: '/admin', icon: LayoutDashboard, permKey: 'canAccessAdmin' },
  { name: 'Manage Stores', path: '/admin/stores', icon: Store, permKey: 'canManageUsers' },
  { name: 'Manage Users', path: '/admin/users', icon: Users, permKey: 'canManageUsers' },
  { name: 'Billing', path: '/admin/billing', icon: Receipt, permKey: 'canManageUsers' },
  { name: 'Platform Settings', path: '/admin/settings', icon: Settings, permKey: 'canAccessAdmin' },
  { name: 'Data Center', path: '/admin/data', icon: Database, permKey: 'canAccessAdmin' },
];

export default function Sidebar({ session }: { session?: any }) {
  const role = session?.role ?? 'staff';
  const rawStoreId = session?.storeId;
  const currentStoreId = typeof rawStoreId === 'string' ? rawStoreId : '';
  const stores: string[] = Array.isArray(session?.stores) ? session.stores : [];
  const [showCreateModal, setShowCreateModal] = useState(false);
  const pathname = usePathname();

  // Resolve permissions from role
  const perms: PermissionSet = resolvePermissions(role);

  // Build nav items based on permission set
  const isPlatformAdmin = role === 'admin' || role === 'super_admin';
  const filteredNavItems = isPlatformAdmin
    ? ADMIN_ROUTE_PERMISSIONS.filter((r) => (perms as any)[r.permKey])
    : ROUTE_PERMISSIONS.filter((r) => (perms as any)[r.permKey]);

  // Quotation route — included via the canManageStaff-like toggle on manager view
  if (!isPlatformAdmin && role === 'manager') {
    filteredNavItems.push({ name: 'Quotations', path: '/quotations', icon: Receipt });
  }

  if (pathname === '/login' || pathname === '/signup') {
    return null;
  }

  const roleLabel = ROLE_LABELS[role] ?? role;
  const isRestrictedRole = ['staff', 'cashier'].includes(role);

  return (
    <aside className="w-64 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 min-h-screen flex flex-col shadow-sm transition-colors duration-200">
      {/* Header */}
      <div className="h-16 flex items-center justify-between px-6 border-b border-gray-100 dark:border-gray-800">
        <div className="text-xl font-bold bg-gradient-to-r from-teal-600 to-teal-400 bg-clip-text text-transparent">
          StoreBook
        </div>
        <ThemeToggle />
      </div>

      {/* Role badge — visible for all non-admin roles */}
      {!isPlatformAdmin && (
        <div className="px-4 py-2 border-b border-gray-100 dark:border-gray-800 flex items-center gap-2">
          {isRestrictedRole ? (
            <>
              <LockKeyhole size={14} className="text-amber-500" />
              <span className="text-xs text-amber-600 dark:text-amber-400 font-medium tracking-wide">{roleLabel} — limited access</span>
            </>
          ) : (
            <>
              <BadgeCheck size={14} className="text-emerald-500" />
              <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">{roleLabel}</span>
            </>
          )}
        </div>
      )}

      {/* Store switcher — only owners */}
      {role === 'owner' && (
        <div className="px-4 py-3 border-b border-gray-100 dark:border-gray-800">
          <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Current Store</label>
          <select
            className="w-full bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-gray-700 dark:text-gray-200 text-sm rounded-lg focus:ring-teal-500 focus:border-teal-500 block p-2.5"
            value={currentStoreId || ''}
            onChange={async (e) => {
              if (e.target.value === 'NEW') {
                setShowCreateModal(true);
              } else {
                const { switchStore } = await import('@/app/actions');
                await switchStore(e.target.value);
                window.location.reload();
              }
            }}
          >
            {stores.map((sId: string, idx: number) => (
              <option key={sId} value={sId}>Store {idx + 1} ({sId.slice(0, 8)}…)</option>
            ))}
            {stores.length === 0 && currentStoreId && (
              <option value={currentStoreId}>My Primary Store</option>
            )}
            <option value="NEW">+ Create New Store</option>
          </select>
        </div>
      )}

      {/* Navigation */}
      <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
        {filteredNavItems.map((item) => (
          <Link
            key={item.name}
            href={item.path}
            className={`flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 group ${
              pathname === item.path || item.path !== '/' && pathname.startsWith(item.path)
                ? 'bg-teal-50 text-teal-700 dark:bg-teal-900/40 dark:text-teal-400 font-medium'
                : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-200'
            }`}
          >
            <item.icon size={20} className={
              pathname === item.path || item.path !== '/' && pathname.startsWith(item.path)
                ? 'text-teal-600 dark:text-teal-400'
                : 'text-gray-400 dark:text-gray-500 group-hover:text-gray-600 dark:group-hover:text-gray-400'
            } />
            <span>{item.name}</span>
          </Link>
        ))}
      </nav>

      {/* Staff management link — only owners and managers (read-only for staff list) */}
      {!isPlatformAdmin && perms.canManageStaff && (
        <div className="px-4 py-2 border-t border-gray-100 dark:border-gray-800">
          <Link
            href="/settings?tab=staff"
            className="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-200 group"
          >
            <Users size={20} className="text-gray-400 dark:text-gray-500 group-hover:text-teal-600" />
            <span>Manage Staff</span>
          </Link>
        </div>
      )}

      {/* Logout */}
      <div className="p-4 border-t border-gray-100 dark:border-gray-800">
        <button
          onClick={async () => {
            const { logout } = await import('@/app/actions');
            await logout();
            window.location.href = '/login';
          }}
          className="flex items-center space-x-3 px-4 py-3 w-full text-left rounded-xl transition-all duration-200 text-gray-600 dark:text-gray-400 hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-600 dark:hover:text-red-400 group"
        >
          <LogOut size={20} className="text-gray-400 dark:text-gray-500 group-hover:text-red-500" />
          <span>Logout</span>
        </button>
      </div>

      {showCreateModal && <CreateStoreModal onClose={() => setShowCreateModal(false)} />}
    </aside>
  );
}
