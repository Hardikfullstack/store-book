'use client';

import { useState } from 'react';
import { Receipt, Search, CreditCard, Play, Edit, ShieldCheck } from 'lucide-react';
import { FormattedAmount } from '@/components/FormattedAmount';

export default function BillingClient({ stores }: { stores: any[] }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [editingStore, setEditingStore] = useState<any>(null);

  const filteredStores = stores.filter(s => 
    (s.name || '').toLowerCase().includes(searchQuery.toLowerCase()) || 
    (s.id || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <Receipt className="text-teal-500" />
            Billing & Subscriptions
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Manage Razorpay and Google Play subscriptions across all stores.</p>
        </div>
      </div>

      <div className="glass-card overflow-hidden border border-gray-200 dark:border-gray-800">
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50">
          <div className="relative w-72">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search size={16} className="text-gray-400" />
            </div>
            <input 
              type="text" 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-white"
              placeholder="Search by store name or ID..."
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 dark:bg-gray-900/50 border-b border-gray-100 dark:border-gray-800">
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Store</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Plan Status</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Platform</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Expiry Date</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
              {filteredStores.map((store) => (
                <tr key={store.id} className="hover:bg-gray-50/30 dark:hover:bg-gray-800/30 transition-colors">
                  <td className="p-4">
                    <div className="font-medium text-gray-900 dark:text-white">{store.name}</div>
                    <div className="text-xs text-gray-500 font-mono mt-0.5">{store.id}</div>
                  </td>
                  <td className="p-4">
                    {store.isPremium ? (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400 border border-purple-200 dark:border-purple-800/50">
                        Premium
                      </span>
                    ) : (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700">
                        Free / Trial
                      </span>
                    )}
                  </td>
                  <td className="p-4">
                    {store.subscriptionPlatform === 'google_play' ? (
                      <span className="flex items-center space-x-1.5 text-sm text-green-600 dark:text-green-400">
                        <Play size={14} />
                        <span>Google Play</span>
                      </span>
                    ) : store.subscriptionPlatform === 'razorpay' ? (
                      <span className="flex items-center space-x-1.5 text-sm text-blue-600 dark:text-blue-400">
                        <CreditCard size={14} />
                        <span>Razorpay</span>
                      </span>
                    ) : (
                      <span className="text-sm text-gray-400">-</span>
                    )}
                  </td>
                  <td className="p-4 text-sm text-gray-600 dark:text-gray-400">
                    {store.subscriptionExpiresAt ? new Date(store.subscriptionExpiresAt).toLocaleDateString() : '-'}
                  </td>
                  <td className="p-4 flex justify-end gap-2">
                    <button 
                      onClick={() => setEditingStore(store)}
                      className="p-2 text-teal-600 hover:bg-teal-50 dark:hover:bg-teal-900/20 rounded-lg transition-colors"
                      title="Manual Plan Override"
                    >
                      <Edit size={16} />
                    </button>
                  </td>
                </tr>
              ))}
              {filteredStores.length === 0 && (
                <tr>
                  <td colSpan={5} className="p-8 text-center text-gray-500 dark:text-gray-400">
                    No stores found matching your search.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Manual Override Modal */}
      {editingStore && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-md p-6 border border-gray-200 dark:border-gray-800">
            <h2 className="text-xl font-bold mb-2 flex items-center text-gray-900 dark:text-white">
              <ShieldCheck className="mr-2 text-teal-500" />
              Manual Plan Override
            </h2>
            <p className="text-sm text-gray-500 mb-6">Modify subscription details directly for <span className="font-bold">{editingStore.name}</span>.</p>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Status</label>
                <select className="w-full p-2.5 border border-gray-200 dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-800 dark:text-white focus:ring-2 focus:ring-teal-500">
                  <option value="true" selected={editingStore.isPremium}>Premium</option>
                  <option value="false" selected={!editingStore.isPremium}>Free Tier</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Platform Override</label>
                <select className="w-full p-2.5 border border-gray-200 dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-800 dark:text-white focus:ring-2 focus:ring-teal-500">
                  <option value="razorpay" selected={editingStore.subscriptionPlatform === 'razorpay'}>Razorpay (Web)</option>
                  <option value="google_play" selected={editingStore.subscriptionPlatform === 'google_play'}>Google Play (App)</option>
                  <option value="manual" selected={!['razorpay','google_play'].includes(editingStore.subscriptionPlatform)}>Manual / Support</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Expiry Date</label>
                <input 
                  type="date" 
                  defaultValue={editingStore.subscriptionExpiresAt ? new Date(editingStore.subscriptionExpiresAt).toISOString().split('T')[0] : ''}
                  className="w-full p-2.5 border border-gray-200 dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-800 dark:text-white focus:ring-2 focus:ring-teal-500"
                />
              </div>

              <div className="pt-4 flex justify-end space-x-3 border-t border-gray-100 dark:border-gray-800">
                <button 
                  onClick={() => setEditingStore(null)}
                  className="px-4 py-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-sm font-medium transition-colors"
                >
                  Cancel
                </button>
                <button 
                  onClick={() => {
                    alert("Super Admin Override applied successfully!");
                    setEditingStore(null);
                  }}
                  className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-bold transition-colors"
                >
                  Apply Override
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
