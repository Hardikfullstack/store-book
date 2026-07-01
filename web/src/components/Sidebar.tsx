'use client';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, Package, ShoppingCart, Users, Receipt, LogOut, Database, Store, Settings } from 'lucide-react';
import { ThemeToggle } from '@/components/ThemeToggle';
import { useState } from 'react';
import CreateStoreModal from '@/components/CreateStoreModal';

export default function Sidebar({ session }: { session?: any }) {
  const role = session?.role;
  const currentStoreId = session?.storeId;
  const stores = session?.stores || [];
  const [showCreateModal, setShowCreateModal] = useState(false);
  const pathname = usePathname();
  const navItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Items', path: '/items', icon: Package },
    { name: 'Quotations', path: '/quotations', icon: Receipt },
    { name: 'Sales', path: '/sales', icon: ShoppingCart },
    { name: 'Udhaar', path: '/udhaar', icon: Users },
    { name: 'Expenses', path: '/expenses', icon: Receipt },
    { name: 'Reports', path: '/reports', icon: Database },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  if (role === 'admin' || role === 'super_admin') {
    navItems.length = 0; // Clear normal store nav items for admin context
    navItems.push(
      { name: 'Admin Dashboard', path: '/admin', icon: LayoutDashboard },
      { name: 'Manage Stores', path: '/admin/stores', icon: Store },
      { name: 'Manage Users', path: '/admin/users', icon: Users },
      { name: 'Billing & Subscriptions', path: '/admin/billing', icon: Receipt },
      { name: 'Platform Settings', path: '/admin/settings', icon: Settings },
      { name: 'Data Center', path: '/admin/data', icon: Database }
    );
  }

  if (pathname === '/login' || pathname === '/signup') {
    return null;
  }

  return (
    <aside className="w-64 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 min-h-screen flex flex-col shadow-sm transition-colors duration-200">
      <div className="h-16 flex items-center justify-between px-6 border-b border-gray-100 dark:border-gray-800">
        <div className="text-xl font-bold bg-gradient-to-r from-teal-600 to-teal-400 bg-clip-text text-transparent">
          StoreBook
        </div>
        <ThemeToggle />
      </div>

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
              <option key={sId} value={sId}>Store {idx + 1} ({sId})</option>
            ))}
            {stores.length === 0 && currentStoreId && <option value={currentStoreId}>My Primary Store ({currentStoreId})</option>}
            <option value="NEW">+ Create New Store</option>
          </select>
        </div>
      )}

      <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
        {navItems.map((item) => (
          <Link
            key={item.name}
            href={item.path}
            className={`flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 group ${
              pathname === item.path
                ? 'bg-teal-50 text-teal-700 dark:bg-teal-900/40 dark:text-teal-400 font-medium'
                : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-200'
            }`}
          >
            <item.icon size={20} className={pathname === item.path ? 'text-teal-600 dark:text-teal-400' : 'text-gray-400 dark:text-gray-500 group-hover:text-gray-600 dark:group-hover:text-gray-400'} />
            <span>{item.name}</span>
          </Link>
        ))}
      </nav>

      {/* Logout / User Info */}
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
