'use client';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, Package, ShoppingCart, Users, Receipt, LogOut, Database, Store, Settings, BadgeCheck, LockKeyhole, History, ShoppingBag, Truck } from 'lucide-react';
import { ThemeToggle } from '@/components/ThemeToggle';
import { useState } from 'react';
import CreateStoreModal from '@/components/CreateStoreModal';
import { resolvePermissions, ROLE_LABELS, hasRolePermission, PermissionSet } from '@/lib/roleMatrix';

// Route → Permission mapping used to build nav items dynamically
const ROUTE_PERMISSIONS = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard, permKey: 'canViewDashboard' },
  { name: 'Purchase History', path: '/purchases', icon: ShoppingBag, permKey: 'canViewItems' },
  { name: 'Suppliers', path: '/suppliers', icon: Truck, permKey: 'canViewItems' },
  { name: 'Sales', path: '/sales', icon: ShoppingCart, permKey: 'canViewSales' },
  { name: 'Items', path: '/items', icon: Package, permKey: 'canViewItems' },
  { name: 'Quotations', path: '/quotations', icon: Receipt, permKey: 'canViewItems' }, // Quotations share Items permission
  { name: 'Udhaar', path: '/udhaar', icon: Users, permKey: 'canViewUdhaar' },
  { name: 'Expenses', path: '/expenses', icon: Receipt, permKey: 'canViewExpenses' },
  { name: 'Reports', path: '/reports', icon: Database, permKey: 'canViewReports' },
  { name: 'Stock Audit', path: '/audit', icon: History, permKey: 'canViewReports' },
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

export default function Sidebar({ session }: { session?: Record<string, unknown> }) {
  const role = (session as { role?: string } | undefined)?.role ?? 'staff';
  const rawStoreId = (session as { storeId?: string } | undefined)?.storeId;
  const currentStoreId = typeof rawStoreId === 'string' ? rawStoreId : '';
  const stores: string[] = Array.isArray(session?.stores) ? session.stores : [];
  const rawStoreDetails = (session as { storeDetails?: { id: string; name: string }[] } | undefined)?.storeDetails;
  const storeList: { id: string; name: string }[] = Array.isArray(rawStoreDetails) && rawStoreDetails.length > 0
    ? rawStoreDetails
    : stores.map((sId, idx) => ({ id: sId, name: `Store ${idx + 1} (${sId.slice(0, 8)}…)` }));

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isSwitching, setIsSwitching] = useState(false);
  const pathname = usePathname();

  // Build nav items based on permission set
  const isPlatformAdmin = role === 'admin' || role === 'super_admin';
  const hasPerm = hasRolePermission(role);
  const filteredNavItems = isPlatformAdmin ? ADMIN_ROUTE_PERMISSIONS.filter((r) => hasPerm(r.permKey as keyof PermissionSet)) : ROUTE_PERMISSIONS.filter((r) => hasPerm(r.permKey as keyof PermissionSet));

  const canSwitchStore = role === 'owner' || role === 'manager' || isPlatformAdmin;

  const handleStoreChange = async (targetValue: string) => {
    if (targetValue === 'NEW') {
      setShowCreateModal(true);
      return;
    }
    if (!targetValue || targetValue === currentStoreId) return;

    setIsSwitching(true);
    try {
      const { persistor } = await import('@/store');
      await persistor.purge();
      if (typeof window !== 'undefined') {
        window.sessionStorage.clear();
      }
      const { switchStore } = await import('@/app/actions');
      await switchStore(targetValue);
      window.location.href = window.location.pathname;
    } catch (err) {
      console.error("Store switch error:", err);
      alert("Failed to switch store: " + (err instanceof Error ? err.message : String(err)));
      setIsSwitching(false);
    }
  };

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

      {/* Store switcher — owners, managers, and admins */}
      {canSwitchStore && (
        <div className="px-4 py-3 border-b border-gray-100 dark:border-gray-800">
          <div className="flex items-center justify-between mb-2">
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider">Current Store</label>
            {isSwitching && (
              <span className="text-xs text-teal-600 dark:text-teal-400 animate-pulse font-medium">Switching…</span>
            )}
          </div>
          <select
            disabled={isSwitching}
            className="w-full bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-gray-700 dark:text-gray-200 text-sm rounded-lg focus:ring-teal-500 focus:border-teal-500 block p-2.5 disabled:opacity-50"
            value={currentStoreId || ''}
            onChange={(e) => handleStoreChange(e.target.value)}
          >
            {storeList.map((st) => (
              <option key={st.id} value={st.id}>{st.name}</option>
            ))}
            {storeList.length === 0 && currentStoreId && (
              <option value={currentStoreId}>My Primary Store</option>
            )}
            {(role === 'owner' || isPlatformAdmin) && (
              <option value="NEW">+ Create New Store</option>
            )}
          </select>
        </div>
      )}

      {/* Navigation */}
      <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
        {filteredNavItems.map((item) => (
          <Link
            key={item.name}
            href={item.path}
            className={`flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 group ${pathname === item.path || item.path !== '/' && pathname.startsWith(item.path)
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
      {!isPlatformAdmin && hasPerm('canManageStaff') && (
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
