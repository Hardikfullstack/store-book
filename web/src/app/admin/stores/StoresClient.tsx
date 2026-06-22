'use client';

import { useState } from 'react';
import { Store, Power, Edit3, Loader2 } from 'lucide-react';
import { toggleStoreStatus, getStoresPaginated } from '@/app/actions';

export default function StoresClient({ initialStores }: { initialStores: any[] }) {
  const [stores, setStores] = useState(initialStores);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(initialStores.length === 20);

  const loadMore = async () => {
    if (loadingMore || !hasMore) return;
    setLoadingMore(true);
    try {
      const lastId = stores[stores.length - 1]?.id;
      const nextBatch = await getStoresPaginated(lastId);
      if (nextBatch.length < 20) {
        setHasMore(false);
      }
      setStores(prev => [...prev, ...nextBatch]);
    } catch (error) {
      console.error("Failed to load more stores:", error);
    } finally {
      setLoadingMore(false);
    }
  };

  const handleToggleStatus = async (storeId: string, currentStatus: boolean) => {
    // In a real app, use optimistic UI here
    await toggleStoreStatus(storeId, !currentStatus);
    setStores(prev => prev.map(s => s.id === storeId ? { ...s, is_active: !currentStatus } : s));
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <Store className="text-purple-500" />
            Manage Stores
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">View and manage all registered platform stores. Loaded in batches to support infinite scale.</p>
        </div>
      </div>

      <div className="glass-card overflow-hidden border border-gray-200 dark:border-gray-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 dark:bg-gray-900/50 border-b border-gray-100 dark:border-gray-800">
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Store Name</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Location</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Status</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
              {stores.map((store) => (
                <tr key={store.id} className="hover:bg-gray-50/30 dark:hover:bg-gray-800/30 transition-colors">
                  <td className="p-4 text-sm text-gray-900 dark:text-gray-100 font-medium">
                    {store.name}
                    <span className="block text-xs text-gray-400 font-mono mt-1">ID: {store.id}</span>
                  </td>
                  <td className="p-4 text-sm text-gray-600 dark:text-gray-400">{store.location || 'N/A'}</td>
                  <td className="p-4">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      store.is_active !== false ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'
                    }`}>
                      {store.is_active !== false ? 'Active' : 'Suspended'}
                    </span>
                  </td>
                  <td className="p-4 flex items-center justify-end gap-2">
                    <button 
                      onClick={() => handleToggleStatus(store.id, store.is_active !== false)}
                      className={`p-2 rounded-lg transition-colors ${store.is_active !== false ? 'text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20' : 'text-emerald-600 hover:bg-emerald-50 dark:hover:bg-emerald-900/20'}`}
                      title={store.is_active !== false ? 'Suspend Store' : 'Activate Store'}
                    >
                      <Power size={18} />
                    </button>
                  </td>
                </tr>
              ))}
              {stores.length === 0 && (
                <tr>
                  <td colSpan={4} className="p-8 text-center text-gray-500 dark:text-gray-400">
                    No stores found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        {hasMore && (
          <div className="p-4 border-t border-gray-100 dark:border-gray-800 flex justify-center">
            <button 
              onClick={loadMore} 
              disabled={loadingMore}
              className="px-6 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-xl text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors flex items-center gap-2"
            >
              {loadingMore ? <Loader2 size={16} className="animate-spin" /> : null}
              Load More
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
